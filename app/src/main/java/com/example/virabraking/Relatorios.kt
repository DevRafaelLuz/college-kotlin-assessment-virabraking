package com.example.virabraking

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.Locale

class Relatorios : AppCompatActivity() {

    private var inicioDoPeriodo: () -> String = ::inicioDoMes

    private val botoesFiltro = mutableListOf<MaterialButton>()

    private lateinit var graficoVeiculos: GraficoRoscaView
    private lateinit var containerLegendaVeiculos: LinearLayout
    private lateinit var containerBarrasMensais: LinearLayout

    private data class OpcaoFiltro(val botao: MaterialButton, val obterInicio: () -> String)

    private data class LinhaRelatorio(val categoria: String, val idTexto: Int, val idBarra: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorios)

        inicializarComponentes()
        configurarTela()
    }

    override fun onResume() {
        super.onResume()
        carregarRelatorio(inicioDoPeriodo())
    }

    private fun inicializarComponentes() {
        graficoVeiculos = findViewById(R.id.graficoVeiculos)
        containerLegendaVeiculos = findViewById(R.id.llLegendaVeiculos)
        containerBarrasMensais = findViewById(R.id.llBarrasMensais)
    }

    private fun configurarTela() {
        configurarNavegacao()
        configurarFiltros()
    }

    private fun configurarNavegacao() {
        configurarDestino(R.id.llInicio, Inicio::class.java)
        configurarDestino(R.id.llVeiculos, Veiculos::class.java)
        configurarDestino(R.id.llAdicionar, RegistrarServico::class.java)
        configurarDestino(R.id.llAlertas, Alertas::class.java)
    }

    private fun configurarDestino(idView: Int, destino: Class<out AppCompatActivity>) {
        findViewById<LinearLayout>(idView).setOnClickListener {
            abrirTela(destino)
        }
    }

    private fun abrirTela(destino: Class<out AppCompatActivity>) {
        startActivity(Intent(this, destino))
        overridePendingTransition(0, 0)
    }

    private fun configurarFiltros() {
        val opcoes = obterOpcoesFiltro()

        botoesFiltro.clear()
        botoesFiltro.addAll(opcoes.map { it.botao })

        opcoes.forEach { opcao ->
            configurarCliqueFiltro(opcao)
        }

        selecionarFiltroInicial(opcoes)
    }

    private fun obterOpcoesFiltro(): List<OpcaoFiltro> {
        return listOf(
            OpcaoFiltro(findViewById(R.id.btnEsteMes), ::inicioDoMes),
            OpcaoFiltro(findViewById(R.id.btnTrimestre), ::inicioDosUltimosTresMeses),
            OpcaoFiltro(findViewById(R.id.btnEsteAno), ::inicioDoAno)
        )
    }

    private fun configurarCliqueFiltro(opcao: OpcaoFiltro) {
        opcao.botao.setOnClickListener {
            inicioDoPeriodo = opcao.obterInicio

            destacarBotao(opcao.botao)
            carregarRelatorio(inicioDoPeriodo())
        }
    }

    private fun selecionarFiltroInicial(opcoes: List<OpcaoFiltro>) {
        opcoes.firstOrNull()?.let { opcao ->
            destacarBotao(opcao.botao)
        }
    }

    private fun destacarBotao(selecionado: MaterialButton) {
        botoesFiltro.forEach { botao ->
            atualizarEstiloBotao(
                botao,
                selecionado = botao == selecionado
            )
        }
    }

    private fun atualizarEstiloBotao(botao: MaterialButton, selecionado: Boolean) {
        val corFundo = if (selecionado) {
            COR_SELECIONADO
        } else {
            COR_FUNDO
        }

        val corBorda = if (selecionado) {
            COR_SELECIONADO
        } else {
            COR_BORDA
        }

        val corTexto = if (selecionado) {
            COR_TEXTO_SELECIONADO
        } else {
            COR_TEXTO_NORMAL
        }

        botao.backgroundTintList = ColorStateList.valueOf(corFundo)
        botao.strokeColor = ColorStateList.valueOf(corBorda)
        botao.setTextColor(corTexto)
    }

    private fun carregarRelatorio(inicio: String) {
        val banco = BancoHelper.getInstance(this)

        val totais = calcularTotais(banco, inicio)

        atualizarRelatorio(totais)
        atualizarGraficoVeiculos(
            banco.totalGastosPorVeiculoDesde(inicio)
        )
        atualizarGraficoMensal(
            banco.tendenciaGastosUltimosMeses(QUANTIDADE_MESES_GRAFICO)
        )
    }

    private fun calcularTotais(banco: BancoHelper, inicio: String): Map<String, Double> {
        val totais = criarTotaisIniciais(
            banco.totalCombustivelDesde(inicio)
        )

        banco.totalServicosPorTipoDesde(inicio).forEach { (tipo, valor) ->
            adicionarTotalServico(totais, tipo, valor)
        }

        return totais
    }

    private fun criarTotaisIniciais(totalCombustivel: Double): MutableMap<String, Double> {
        return linkedMapOf(
            CATEGORIA_COMBUSTIVEL to totalCombustivel,
            CATEGORIA_REVISOES to 0.0,
            CATEGORIA_OLEO to 0.0,
            CATEGORIA_PNEUS to 0.0,
            CATEGORIA_OUTROS to 0.0
        )
    }
    private fun adicionarTotalServico(totais: MutableMap<String, Double>, tipo: String,valor: Double
    ) {
        val categoria = categoriaDoServico(tipo)
        totais[categoria] = (totais[categoria] ?: 0.0) + valor
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
            LinhaRelatorio(
                CATEGORIA_COMBUSTIVEL,
                R.id.tvValorCombustivel,
                R.id.pbCombustivel
            ),
            LinhaRelatorio(
                CATEGORIA_REVISOES,
                R.id.tvValorRevisoes,
                R.id.pbRevisoes
            ),
            LinhaRelatorio(
                CATEGORIA_OLEO,
                R.id.tvValorOleo,
                R.id.pbOleo
            ),
            LinhaRelatorio(
                CATEGORIA_PNEUS,
                R.id.tvValorPneus,
                R.id.pbPneus
            ),
            LinhaRelatorio(
                CATEGORIA_OUTROS,
                R.id.tvValorOutros,
                R.id.pbOutros
            )
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

    private fun atualizarGraficoVeiculos(gastos: List<GastoVeiculo>) {
        containerLegendaVeiculos.removeAllViews()

        val somaTotal = gastos.sumOf { it.total }

        if (somaTotal <= 0.0) {
            exibirGraficoVeiculosVazio()
            return
        }

        val fatias = mutableListOf<Pair<Float, Int>>()

        gastos.forEachIndexed { index, gasto ->
            adicionarItemLegenda(
                gasto = gasto,
                somaTotal = somaTotal,
                cor = CORES_GRAFICO[index % CORES_GRAFICO.size],
                fatias = fatias
            )
        }

        graficoVeiculos.definirDados(fatias)
    }

    private fun exibirGraficoVeiculosVazio() {
        graficoVeiculos.definirDados(emptyList())

        val mensagem = TextView(this).apply {
            text = MENSAGEM_GRAFICO_VAZIO
            textSize = TAMANHO_TEXTO_LEGENDA
            setTextColor(COR_TEXTO_SECUNDARIO)
        }

        containerLegendaVeiculos.addView(mensagem)
    }

    private fun adicionarItemLegenda(
        gasto: GastoVeiculo,
        somaTotal: Double,
        cor: Int,
        fatias: MutableList<Pair<Float, Int>>
    ) {
        val percentual = (gasto.total / somaTotal * PERCENTUAL_MAXIMO).toFloat()

        fatias.add(percentual to cor)

        val linha = criarLinhaLegenda()
        val bolinha = criarBolinhaLegenda(cor)
        val texto = criarTextoLegenda(gasto.apelido, percentual)

        linha.addView(bolinha)
        linha.addView(texto)

        containerLegendaVeiculos.addView(linha)
    }

    private fun criarLinhaLegenda(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            val paddingVertical = dpParaPx(PADDING_LEGENDA_VERTICAL_DP)
            setPadding(0, paddingVertical, 0, paddingVertical)
        }
    }

    private fun criarBolinhaLegenda(cor: Int): View {
        val tamanho = dpParaPx(TAMANHO_BOLINHA_DP)
        val margem = dpParaPx(ESPACAMENTO_LEGENDA_DP)

        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(tamanho, tamanho).apply {
                marginEnd = margem
            }

            setBackgroundResource(R.drawable.bolinha_legenda)
            backgroundTintList = ColorStateList.valueOf(cor)
        }
    }

    private fun criarTextoLegenda(apelido: String, percentual: Float): TextView {
        return TextView(this).apply {
            text = "$apelido (${percentual.toInt()}%)"
            textSize = TAMANHO_TEXTO_LEGENDA
            setTextColor(COR_TEXTO_GRAFICO)
        }
    }

    private fun atualizarGraficoMensal(meses: List<GastoMensal>) {
        containerBarrasMensais.removeAllViews()

        val maiorGasto = meses.maxOfOrNull { it.total } ?: 0.0
        val dimensoes = obterDimensoesGraficoMensal()

        meses.forEach { mes ->
            adicionarColunaMensal(
                mes = mes,
                maiorGasto = maiorGasto,
                dimensoes = dimensoes
            )
        }
    }

    private fun obterDimensoesGraficoMensal(): DimensoesGrafico {
        return DimensoesGrafico(
            alturaMaxima = dpParaPx(ALTURA_MAXIMA_BARRA_DP),
            alturaMinima = dpParaPx(ALTURA_MINIMA_BARRA_DP),
            larguraBarra = dpParaPx(LARGURA_BARRA_DP),
            margemBarra = dpParaPx(MARGEM_BARRA_DP)
        )
    }

    private data class DimensoesGrafico(
        val alturaMaxima: Int,
        val alturaMinima: Int,
        val larguraBarra: Int,
        val margemBarra: Int
    )

    private fun adicionarColunaMensal(mes: GastoMensal, maiorGasto: Double, dimensoes: DimensoesGrafico) {
        val coluna = criarColunaMensal()
        val altura = calcularAlturaBarra(mes.total, maiorGasto, dimensoes)

        val barra = criarBarraMensal(mes.total, altura, dimensoes)
        val rotulo = criarRotuloMensal(mes.rotulo)

        coluna.addView(barra)
        coluna.addView(rotulo)

        containerBarrasMensais.addView(coluna)
    }

    private fun criarColunaMensal(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        }
    }

    private fun calcularAlturaBarra(valor: Double, maiorValor: Double, dimensoes: DimensoesGrafico): Int {
        if (maiorValor <= 0.0 || valor <= 0.0) {
            return dimensoes.alturaMinima
        }

        return ((valor / maiorValor) * dimensoes.alturaMaxima)
            .toInt()
            .coerceAtLeast(dimensoes.alturaMinima)
    }

    private fun criarBarraMensal(valor: Double, altura: Int, dimensoes: DimensoesGrafico): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dimensoes.larguraBarra,
                altura
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dimensoes.margemBarra
            }

            setBackgroundResource(R.drawable.barra_mensal_arredondada)

            alpha = if (valor > 0.0) {
                OPACIDADE_BARRA_COM_DADOS
            } else {
                OPACIDADE_BARRA_VAZIA
            }
        }
    }

    private fun criarRotuloMensal(rotulo: String): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }

            text = rotulo
            textSize = TAMANHO_TEXTO_MES
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(COR_TEXTO_SECUNDARIO)
        }
    }

    private fun dpParaPx(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    private companion object {
        private const val CATEGORIA_COMBUSTIVEL = "Combustível"
        private const val CATEGORIA_REVISOES = "Revisões"
        private const val CATEGORIA_OLEO = "Óleo"
        private const val CATEGORIA_PNEUS = "Pneus"
        private const val CATEGORIA_OUTROS = "Outros"

        private const val PERCENTUAL_MAXIMO = 100
        private const val QUANTIDADE_MESES_GRAFICO = 5

        private const val FORMATO_PERCENTUAL = "%.0f%%"

        private const val MENSAGEM_GRAFICO_VAZIO =
            "Sem gastos no período"

        private const val TAMANHO_TEXTO_LEGENDA = 12f
        private const val TAMANHO_TEXTO_MES = 11f

        private const val TAMANHO_BOLINHA_DP = 8
        private const val ESPACAMENTO_LEGENDA_DP = 8
        private const val PADDING_LEGENDA_VERTICAL_DP = 6

        private const val ALTURA_MAXIMA_BARRA_DP = 65
        private const val ALTURA_MINIMA_BARRA_DP = 14
        private const val LARGURA_BARRA_DP = 12
        private const val MARGEM_BARRA_DP = 8

        private const val OPACIDADE_BARRA_VAZIA = 0.25f
        private const val OPACIDADE_BARRA_COM_DADOS = 1.0f

        private val CORES_GRAFICO = listOf(
            Color.parseColor("#0D9488"),
            Color.parseColor("#10B981"),
            Color.parseColor("#34D399"),
            Color.parseColor("#0F766E")
        )

        private val COR_SELECIONADO = Color.parseColor("#0D9488")
        private val COR_FUNDO = Color.parseColor("#FFFFFF")
        private val COR_BORDA = Color.parseColor("#E2E8F0")
        private val COR_TEXTO_NORMAL = Color.parseColor("#64748B")
        private val COR_TEXTO_SELECIONADO = Color.WHITE
        private val COR_TEXTO_GRAFICO = Color.parseColor("#0F172A")
        private val COR_TEXTO_SECUNDARIO = Color.parseColor("#64748B")
    }
}