package com.example.virabraking

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class DetalhesVeiculo : AppCompatActivity() {

    private enum class Aba {
        HISTORICO,
        COMBUSTIVEL,
        DOCUMENTOS
    }

    private data class ConteudoAba(
        val titulo: String,
        val atividades: List<Atividade>,
        val mensagemVazia: String
    )

    private var veiculoId = Constantes.ID_INVALIDO
    private var abaAtual = Aba.HISTORICO

    private lateinit var tvNomeVeiculo: TextView
    private lateinit var tvModelo: TextView
    private lateinit var tvAno: TextView
    private lateinit var tvPlaca: TextView
    private lateinit var tvKmVeiculo: TextView
    private lateinit var tvTituloLista: TextView
    private lateinit var llLista: LinearLayout

    private lateinit var botoesAbas: List<Pair<MaterialButton, Aba>>

    companion object {
        private val COR_FUNDO_BOTAO = Color.parseColor("#ffffff")
        private val COR_BORDA_BOTAO = Color.parseColor("#e2e8f0")
        private val COR_TEXTO_SECUNDARIO = Color.parseColor("#64748b")
        private val COR_DESTAQUE = Color.parseColor("#0d9488")
        private const val LIMITE_ATIVIDADES = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_veiculo)

        if (!obterIdVeiculo()) {
            return
        }

        inicializarViews()
        configurarBotaoVoltar()
        configurarAbas()
    }

    override fun onResume() {
        super.onResume()
        if (veiculoId == Constantes.ID_INVALIDO) {
            return
        }

        preencherCabecalho()
        carregarLista()
    }

    private fun obterIdVeiculo(): Boolean {
        veiculoId = intent.getIntExtra(Constantes.EXTRA_VEICULO_ID, Constantes.ID_INVALIDO)

        if (veiculoId != Constantes.ID_INVALIDO) {
            return true
        }

        Toast.makeText(this, "Erro: Veículo não encontrado.", Toast.LENGTH_SHORT).show()

        finish()
        return false
    }

    private fun inicializarViews() {
        tvNomeVeiculo = findViewById(R.id.tvNomeVeiculo)
        tvModelo = findViewById(R.id.tvModelo)
        tvAno = findViewById(R.id.tvAno)
        tvPlaca = findViewById(R.id.tvPlaca)
        tvKmVeiculo = findViewById(R.id.tvKmVeiculo)
        tvTituloLista = findViewById(R.id.tvTituloLista)
        llLista = findViewById(R.id.llLista)
    }

    private fun configurarBotaoVoltar() {
        findViewById<ImageButton>(R.id.iBtnVoltar).setOnClickListener {
            finish()
        }
    }

    private fun configurarAbas() {
        botoesAbas = listOf(
            findViewById<MaterialButton>(R.id.btnHistorico) to Aba.HISTORICO,
            findViewById<MaterialButton>(R.id.btnCombustivel) to Aba.COMBUSTIVEL,
            findViewById<MaterialButton>(R.id.btnDocumentos) to Aba.DOCUMENTOS
        )

        botoesAbas.forEach { (botao, aba) ->
            botao.setOnClickListener {
                selecionarAba(aba)
            }
        }

        selecionarAba(Aba.HISTORICO)
    }

    private fun selecionarAba(aba: Aba) {
        abaAtual = aba

        botoesAbas.forEach { (botao, abaBotao) ->
            atualizarEstiloBotao(
                botao = botao,
                selecionado = abaBotao == aba
            )
        }

        carregarLista()
    }

    private fun atualizarEstiloBotao(botao: MaterialButton, selecionado: Boolean) {
        val corFundo = if (selecionado) {
            COR_DESTAQUE
        } else {
            COR_FUNDO_BOTAO
        }

        val corBorda = if (selecionado) {
            COR_DESTAQUE
        } else {
            COR_BORDA_BOTAO
        }

        val corTexto = if (selecionado) {
            Color.WHITE
        } else {
            COR_TEXTO_SECUNDARIO
        }

        botao.backgroundTintList = ColorStateList.valueOf(corFundo)
        botao.strokeColor = ColorStateList.valueOf(corBorda)
        botao.setTextColor(corTexto)
    }

    private fun carregarLista() {
        val conteudo = obterConteudoAba()

        tvTituloLista.text = conteudo.titulo
        llLista.removeAllViews()

        if (conteudo.atividades.isEmpty()) {
            exibirMensagemVazia(conteudo.mensagemVazia)
            return
        }

        conteudo.atividades.forEach { atividade ->
            adicionarAtividade(atividade)
        }
    }

    private fun obterConteudoAba(): ConteudoAba {
        val banco = BancoHelper.getInstance(this)

        return when (abaAtual) {
            Aba.HISTORICO -> {
                ConteudoAba(
                    titulo = "Histórico de Manutenções",
                    atividades = banco.listarAtividades(veiculoId, LIMITE_ATIVIDADES, abastecimentos = false),
                    mensagemVazia = "Nenhum serviço registrado"
                )
            }

            Aba.COMBUSTIVEL -> {
                val consumoMedio = banco.consumoMedio(veiculoId)

                val titulo = consumoMedio?.let { consumo ->
                    "Abastecimentos • " + String.format(LOCALE_BR, "%.1f km/l", consumo)
                } ?: "Abastecimentos"

                ConteudoAba(
                    titulo = titulo,
                    atividades = banco.listarAtividades(veiculoId, LIMITE_ATIVIDADES, servicos = false),
                    mensagemVazia = "Nenhum abastecimento registrado"
                )
            }

            Aba.DOCUMENTOS -> {
                ConteudoAba(titulo = "Documentos", atividades = emptyList(), mensagemVazia = "Nenhum documento cadastrado")
            }
        }
    }

    private fun exibirMensagemVazia(mensagem: String) {
        val mensagemView = TextView(this).apply {
            text = mensagem
            setTextColor(COR_TEXTO_SECUNDARIO)
        }

        llLista.addView(mensagemView)
    }

    private fun adicionarAtividade(atividade: Atividade) {
        val atividadeView = atividade.paraView(layoutInflater, llLista)

        if (atividade.origem == "servico") {
            atividadeView.setOnClickListener {
                abrirDetalhesServico(atividade)
            }
        }

        llLista.addView(atividadeView)
    }

    private fun abrirDetalhesServico(atividade: Atividade) {
        val intent = Intent(
            this,
            DetalheRegistro::class.java
        ).apply {
            putExtra(
                Constantes.EXTRA_SERVICO_ID,
                atividade.id
            )
            putExtra(
                Constantes.EXTRA_VEICULO_ID,
                atividade.veiculoId
            )
        }

        startActivity(intent)
    }

    private fun preencherCabecalho() {
        val veiculo = BancoHelper.getInstance(this).buscarVeiculo(veiculoId) ?: return

        tvNomeVeiculo.text = veiculo.apelido
        tvModelo.text = "${veiculo.marca} ${veiculo.modelo}".trim()
        tvAno.text = veiculo.ano.toString()
        tvPlaca.text = veiculo.placa
        tvKmVeiculo.text = String.format(LOCALE_BR, "%,d km", veiculo.quilometragem)
    }
}