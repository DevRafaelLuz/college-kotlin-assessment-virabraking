package com.example.virabraking

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton

class DetalhesVeiculo : AppCompatActivity() {

    private enum class Aba { HISTORICO, COMBUSTIVEL, DOCUMENTOS }

    private var veiculoId = Constantes.ID_INVALIDO
    private var abaAtual = Aba.HISTORICO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_veiculo)

        veiculoId = intent.getIntExtra(Constantes.EXTRA_VEICULO_ID, Constantes.ID_INVALIDO)

        if (veiculoId == Constantes.ID_INVALIDO) {
            Toast.makeText(this, "Erro: Veículo não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val iBtnVoltar = findViewById<ImageButton>(R.id.iBtnVoltar)
        iBtnVoltar.setOnClickListener {
            finish()
        }

        val abas = listOf(
            findViewById<MaterialButton>(R.id.btnHistorico) to Aba.HISTORICO,
            findViewById<MaterialButton>(R.id.btnCombustivel) to Aba.COMBUSTIVEL,
            findViewById<MaterialButton>(R.id.btnDocumentos) to Aba.DOCUMENTOS
        )
        val botoes = abas.map { it.first }

        abas.forEach { (botao, aba) ->
            botao.setOnClickListener {
                destacarBotao(botoes, botao)
                abaAtual = aba
                carregarLista()
            }
        }
        destacarBotao(botoes, botoes[0])
    }

    override fun onResume() {
        super.onResume()
        preencherCabecalho()
        carregarLista()
    }

    private fun preencherCabecalho() {
        val veiculo = BancoHelper.getInstance(this).buscarVeiculo(veiculoId) ?: return

        findViewById<TextView>(R.id.tvNomeVeiculo).text = veiculo.apelido
        findViewById<TextView>(R.id.tvModelo).text = "${veiculo.marca} ${veiculo.modelo}".trim()
        findViewById<TextView>(R.id.tvAno).text = veiculo.ano.toString()
        findViewById<TextView>(R.id.tvPlaca).text = veiculo.placa
        findViewById<TextView>(R.id.tvKmVeiculo).text =
            String.format(LOCALE_BR, "%,d km", veiculo.quilometragem)
    }

    private fun carregarLista() {
        val banco = BancoHelper.getInstance(this)
        val container = findViewById<LinearLayout>(R.id.llLista)
        val tvTitulo = findViewById<TextView>(R.id.tvTituloLista)
        container.removeAllViews()

        val itens: List<Atividade>
        val mensagemVazia: String

        when (abaAtual) {
            Aba.HISTORICO -> {
                tvTitulo.text = "Histórico de Manutenções"
                itens = banco.listarAtividades(veiculoId, 100, abastecimentos = false)
                mensagemVazia = "Nenhum serviço registrado"
            }
            Aba.COMBUSTIVEL -> {
                val consumo = banco.consumoMedio(veiculoId)
                tvTitulo.text = if (consumo != null) {
                    "Abastecimentos • " + String.format(LOCALE_BR, "%.1f km/l", consumo)
                } else {
                    "Abastecimentos"
                }
                itens = banco.listarAtividades(veiculoId, 100, servicos = false)
                mensagemVazia = "Nenhum abastecimento registrado"
            }
            Aba.DOCUMENTOS -> {
                tvTitulo.text = "Documentos"
                itens = emptyList()
                mensagemVazia = "Nenhum documento cadastrado"
            }
        }

        if (itens.isEmpty()) {
            val vazio = TextView(this)
            vazio.text = mensagemVazia
            vazio.setTextColor(Color.parseColor("#64748b"))
            container.addView(vazio)
            return
        }

        itens.forEach { atividade ->
            val view = atividade.paraView(layoutInflater, container)
            if (atividade.origem == "servico") {
                view.setOnClickListener {
                    val intent = Intent(this, DetalheRegistro::class.java).apply {
                        putExtra(Constantes.EXTRA_SERVICO_ID, atividade.id)
                        putExtra(Constantes.EXTRA_VEICULO_ID, atividade.veiculoId)
                    }
                    startActivity(intent)
                }
            }
            container.addView(view)
        }
    }

    private fun destacarBotao(listaBotoes: List<MaterialButton>, selecionado: MaterialButton) {
        listaBotoes.forEach { botoes ->
            botoes.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))
            botoes.strokeColor = ColorStateList.valueOf(Color.parseColor("#e2e8f0"))
            botoes.setTextColor(Color.parseColor("#64748b"))
        }

        selecionado.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0d9488"))
        selecionado.strokeColor = ColorStateList.valueOf(Color.parseColor("#0d9488"))
        selecionado.setTextColor(Color.parseColor("#ffffff"))
    }
}