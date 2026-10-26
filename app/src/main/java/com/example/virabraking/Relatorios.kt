package com.example.virabraking

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.Locale

class Relatorios : AppCompatActivity() {

    private var inicioDoPeriodo: () -> String = ::inicioDoMes

    private val botoesFiltro = mutableListOf<MaterialButton>()

    private data class OpcaoFiltro(val botao: MaterialButton, val obterInicio: () -> String)

    private data class LinhaRelatorio(val categoria: String, val idTexto: Int, val idBarra: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorios)

        configurarNavegacao()
        configurarFiltros()
    }

    override fun onResume() {
        super.onResume()
        carregarRelatorio(inicioDoPeriodo())
    }

    private fun configurarNavegacao() {
        configurarDestino(R.id.llInicio, Inicio::class.java)
        configurarDestino(R.id.llVeiculos, Veiculos::class.java)
        configurarDestino(R.id.llAdicionar, RegistrarServico::class.java)
        configurarDestino(R.id.llAlertas, Alertas::class.java)
    }

    private fun configurarDestino(idView: Int, destino: Class<out AppCompatActivity>) {
        findViewById<LinearLayout>(idView).setOnClickListener {
            startActivity(Intent(this, destino))
        }
    }

    private fun configurarFiltros() {
        val opcoes = listOf(
            OpcaoFiltro(findViewById(R.id.btnEsteMes), ::inicioDoMes ),
            OpcaoFiltro(findViewById(R.id.btnTrimestre), ::inicioDosUltimosTresMeses),
            OpcaoFiltro(findViewById(R.id.btnEsteAno), ::inicioDoAno)
        )

        botoesFiltro.clear()
        botoesFiltro.addAll(opcoes.map { it.botao })

        opcoes.forEach { opcao ->
            opcao.botao.setOnClickListener {
                inicioDoPeriodo = opcao.obterInicio
                destacarBotao(opcao.botao)
                carregarRelatorio(inicioDoPeriodo())
            }
        }

        destacarBotao(opcoes.first().botao)
    }

    private fun destacarBotao(selecionado: MaterialButton) {
        botoesFiltro.forEach { botao -> val selecionadoAtual = botao == selecionado

            botao.backgroundTintList = ColorStateList.valueOf(
                if (selecionadoAtual) COR_SELECIONADO else COR_FUNDO
            )

            botao.strokeColor = ColorStateList.valueOf(
                if (selecionadoAtual) COR_SELECIONADO else COR_BORDA
            )

            botao.setTextColor(
                if (selecionadoAtual) COR_TEXTO_SELECIONADO
                else COR_TEXTO_NORMAL
            )
        }
    }

    private fun carregarRelatorio(inicio: String) {
        val banco = BancoHelper.getInstance(this)

        val totais = calcularTotais(banco, inicio)
        atualizarRelatorio(totais)
    }

    private fun calcularTotais(banco: BancoHelper, inicio: String): Map<String, Double> {
        val totais = linkedMapOf(
            CATEGORIA_COMBUSTIVEL to banco.totalCombustivelDesde(inicio),
            CATEGORIA_REVISOES to 0.0,
            CATEGORIA_OLEO to 0.0,
            CATEGORIA_PNEUS to 0.0,
            CATEGORIA_OUTROS to 0.0
        )

        banco.totalServicosPorTipoDesde(inicio).forEach { (tipo, valor) ->
            val categoria = categoriaDoServico(tipo)
            totais[categoria] = (totais[categoria] ?: 0.0) + valor
        }

        return totais
    }

    private fun atualizarRelatorio(totais: Map<String, Double>) {
        val linhas = obterLinhasRelatorio()
        val maiorValor = totais.values.maxOrNull() ?: 0.0

        linhas.forEach { linha ->
            val valor = totais[linha.categoria] ?: 0.0

            atualizarValor(linha.idTexto, valor)
            atualizarBarra(linha.idBarra, valor, maiorValor)
        }
    }

    private fun obterLinhasRelatorio(): List<LinhaRelatorio> {
        return listOf(
            LinhaRelatorio(CATEGORIA_COMBUSTIVEL, R.id.tvValorCombustivel, R.id.pbCombustivel),
            LinhaRelatorio(CATEGORIA_REVISOES, R.id.tvValorRevisoes, R.id.pbRevisoes),
            LinhaRelatorio(CATEGORIA_OLEO, R.id.tvValorOleo, R.id.pbOleo),
            LinhaRelatorio(CATEGORIA_PNEUS, R.id.tvValorPneus, R.id.pbPneus),
            LinhaRelatorio(CATEGORIA_OUTROS, R.id.tvValorOutros, R.id.pbOutros)
        )
    }

    private fun atualizarValor(idTexto: Int, valor: Double) {
        findViewById<TextView>(idTexto).text = valor.emReais()
    }

    private fun atualizarBarra(idBarra: Int, valor: Double, maiorValor: Double) {
        val progresso = if (maiorValor > 0.0) {
            (valor / maiorValor * PERCENTUAL_MAXIMO).toInt()
        } else {
            0
        }

        findViewById<ProgressBar>(idBarra).progress = progresso
    }

    private fun categoriaDoServico(tipo: String): String {
        val tipoNormalizado = tipo.lowercase(Locale.forLanguageTag("pt-BR"))

        return when {
            tipoNormalizado.contains("óleo") || tipoNormalizado.contains("oleo") -> CATEGORIA_OLEO
            tipoNormalizado.contains("pneu") -> CATEGORIA_PNEUS
            tipoNormalizado.contains("revis") -> CATEGORIA_REVISOES

            else -> CATEGORIA_OUTROS
        }
    }

    private companion object {
        const val CATEGORIA_COMBUSTIVEL = "Combustível"
        const val CATEGORIA_REVISOES = "Revisões"
        const val CATEGORIA_OLEO = "Óleo"
        const val CATEGORIA_PNEUS = "Pneus"
        const val CATEGORIA_OUTROS = "Outros"

        const val PERCENTUAL_MAXIMO = 100

        val COR_SELECIONADO: Int = Color.parseColor("#0D9488")
        val COR_FUNDO: Int = Color.parseColor("#FFFFFF")
        val COR_BORDA: Int = Color.parseColor("#E2E8F0")
        val COR_TEXTO_NORMAL: Int = Color.parseColor("#64748B")
        val COR_TEXTO_SELECIONADO: Int = Color.parseColor("#FFFFFF")
    }
}