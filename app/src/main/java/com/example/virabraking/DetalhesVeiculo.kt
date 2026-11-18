package com.example.virabraking

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.io.File

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

    private lateinit var ivFotoVeiculo: ImageView
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

        private const val MENSAGEM_ERRO_VEICULO = "Erro: Veículo não encontrado."
        private const val TITULO_HISTORICO = "Histórico de Manutenções"
        private const val TITULO_COMBUSTIVEL = "Abastecimentos"
        private const val TITULO_DOCUMENTOS = "Documentos"
        private const val MENSAGEM_HISTORICO_VAZIO = "Nenhum serviço registrado"
        private const val MENSAGEM_COMBUSTIVEL_VAZIO = "Nenhum abastecimento registrado"
        private const val MENSAGEM_DOCUMENTOS_VAZIO = "Nenhum documento cadastrado"
        private const val ORIGEM_SERVICO = "servico"
        private const val FORMATO_CONSUMO = "%.1f km/l"
        private const val FORMATO_QUILOMETRAGEM = "%,d km"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_veiculo)

        if (!obterIdVeiculo()) {
            return
        }

        inicializarViews()
        configurarTela()
    }

    override fun onResume() {
        super.onResume()
        if (veiculoId == Constantes.ID_INVALIDO) {
            return
        }

        atualizarInformacoesVeiculo()
        carregarLista()
    }

    private fun obterIdVeiculo(): Boolean {
        veiculoId = intent.getIntExtra(Constantes.EXTRA_VEICULO_ID, Constantes.ID_INVALIDO)

        if (veiculoId != Constantes.ID_INVALIDO) {
            return true
        }

        mostrarMensagem(MENSAGEM_ERRO_VEICULO)
        finish()

        return false
    }

    private fun mostrarMensagem(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun inicializarViews() {
        tvNomeVeiculo = findViewById(R.id.tvNomeVeiculo)
        tvModelo = findViewById(R.id.tvModelo)
        tvAno = findViewById(R.id.tvAno)
        tvPlaca = findViewById(R.id.tvPlaca)
        tvKmVeiculo = findViewById(R.id.tvKmVeiculo)
        tvTituloLista = findViewById(R.id.tvTituloLista)
        llLista = findViewById(R.id.llLista)
        ivFotoVeiculo = findViewById(R.id.ivFotoVeiculo)
    }

    private fun configurarTela() {
        configurarBotaoVoltar()
        configurarAbas()
    }

    private fun configurarBotaoVoltar() {
        findViewById<ImageButton>(R.id.iBtnVoltar).setOnClickListener {
            finalizarTela()
        }
    }

    private fun finalizarTela() {
        finish()
        overridePendingTransition(0, 0)
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

        atualizarEstiloAbas()
        carregarLista()
    }

    private fun atualizarEstiloAbas() {
        botoesAbas.forEach { (botao, aba) ->
            atualizarEstiloBotao(botao = botao, selecionado = aba == abaAtual)
        }
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
            Aba.HISTORICO -> obterHistorico(banco)
            Aba.COMBUSTIVEL -> obterAbastecimentos(banco)
            Aba.DOCUMENTOS -> obterDocumentos()
        }
    }

    private fun obterHistorico(banco: BancoHelper): ConteudoAba {
        return ConteudoAba(
            titulo = TITULO_HISTORICO,
            atividades = banco.listarAtividades(veiculoId, LIMITE_ATIVIDADES, abastecimentos = false),
            mensagemVazia = MENSAGEM_HISTORICO_VAZIO
        )
    }

    private fun obterAbastecimentos(banco: BancoHelper): ConteudoAba {
        return ConteudoAba(
            titulo = obterTituloAbastecimentos(banco),
            atividades = banco.listarAtividades(veiculoId, LIMITE_ATIVIDADES, servicos = false),
            mensagemVazia = MENSAGEM_COMBUSTIVEL_VAZIO
        )
    }

    private fun obterTituloAbastecimentos(banco: BancoHelper): String {
        val consumoMedio = banco.consumoMedio(veiculoId)
            ?: return TITULO_COMBUSTIVEL

        val consumoFormatado = String.format(LOCALE_BR, FORMATO_CONSUMO, consumoMedio)

        return "$TITULO_COMBUSTIVEL • $consumoFormatado"
    }

    private fun obterDocumentos(): ConteudoAba {
        return ConteudoAba(
            titulo = TITULO_DOCUMENTOS,
            atividades = emptyList(),
            mensagemVazia = MENSAGEM_DOCUMENTOS_VAZIO
        )
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

        configurarCliqueAtividade(atividadeView, atividade)

        llLista.addView(atividadeView)
    }

    private fun configurarCliqueAtividade(atividadeView: android.view.View, atividade: Atividade) {
        if (atividade.origem == ORIGEM_SERVICO) {
            atividadeView.setOnClickListener {
                abrirDetalhesServico(atividade)
            }
        }
    }

    private fun abrirDetalhesServico(atividade: Atividade) {
        val intent = Intent(this, DetalhesRegistro::class.java).apply {
            putExtra(Constantes.EXTRA_SERVICO_ID, atividade.id)

            putExtra(Constantes.EXTRA_VEICULO_ID, atividade.veiculoId)
        }

        startActivity(intent)
    }

    private fun atualizarInformacoesVeiculo() {
        val veiculo = BancoHelper.getInstance(this)
            .buscarVeiculo(veiculoId)
            ?: return

        preencherDadosVeiculo(veiculo)
        carregarFotoVeiculo(veiculo.caminhoFoto)
    }

    private fun preencherDadosVeiculo(veiculo: Veiculo) {
        tvNomeVeiculo.text = veiculo.apelido

        tvModelo.text = "${veiculo.marca} ${veiculo.modelo}".trim()

        tvAno.text = veiculo.ano.toString()

        tvPlaca.text = veiculo.placa

        tvKmVeiculo.text = String.format(LOCALE_BR, FORMATO_QUILOMETRAGEM, veiculo.quilometragem)
    }

    private fun carregarFotoVeiculo(caminhoFoto: String?) {
        val arquivoFoto = caminhoFoto?.let(::File)

        if (arquivoFoto != null && arquivoFoto.exists()) {
            ivFotoVeiculo.setImageURI(Uri.fromFile(arquivoFoto))
        } else {
            ivFotoVeiculo.setImageDrawable(null)
        }
    }
}