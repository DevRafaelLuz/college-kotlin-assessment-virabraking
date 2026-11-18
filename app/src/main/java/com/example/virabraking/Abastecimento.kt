package com.example.virabraking

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Abastecimento : AppCompatActivity() {

    private lateinit var spVeiculo: Spinner
    private lateinit var etLitros: EditText
    private lateinit var etValorLitro: EditText
    private lateinit var etValorTotal: EditText
    private lateinit var etKm: EditText
    private lateinit var etPosto: EditText
    private lateinit var etData: EditText
    private lateinit var llBarrasConsumo: LinearLayout
    private lateinit var tvSemHistoricoConsumo: TextView

    private var veiculos: List<Veiculo> = emptyList()
    private var ultimoCampoPrecoAlterado: EditText? = null
    private var atualizandoValores = false

    private val dataSelecionada = Calendar.getInstance()
    private val formatoDataExibicao = SimpleDateFormat(FORMATO_DATA_EXIBICAO, LOCALE_BRASIL)
    private val formatoDataBanco = SimpleDateFormat(FORMATO_DATA_BANCO, Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abastecimento)

        if (!carregarVeiculos()) {
            return
        }

        inicializarViews()
        configurarEventos()
    }

    private fun carregarVeiculos(): Boolean {
        veiculos = BancoHelper.getInstance(this).listarVeiculos()

        if (veiculos.isNotEmpty()) return true

        exibirMensagem(MENSAGEM_CADASTRAR_VEICULO)
        finish()
        return false
    }

    private fun exibirMensagem(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun inicializarViews() {
        spVeiculo = findViewById(R.id.spVeiculo)
        etLitros = findViewById(R.id.etLitros)
        etValorLitro = findViewById(R.id.etValorLitro)
        etValorTotal = findViewById(R.id.etValorTotal)
        etKm = findViewById(R.id.etKm)
        etPosto = findViewById(R.id.etPosto)
        etData = findViewById(R.id.etData)
        llBarrasConsumo = findViewById(R.id.llBarrasConsumo)
        tvSemHistoricoConsumo = findViewById(R.id.tvSemHistoricoConsumo)
    }

    private fun configurarEventos() {
        configurarBotaoVoltar()
        configurarSeletorVeiculo()
        configurarCampoData()
        configurarCalculoValores()
        configurarBotaoSalvar()
    }

    private fun configurarBotaoVoltar() {
        findViewById<ImageButton>(R.id.iBtnVoltar).setOnClickListener {
            finalizarTela()
        }
    }

    private fun finalizarTela() {
        finish()
        overridePendingTransition(0, 0);
    }

    private fun configurarSeletorVeiculo() {
        val nomesVeiculos = veiculos.map(Veiculo::apelido)
        val adapter = ArrayAdapter(
            this,
            R.layout.item_spinner_tipo_servico,
            nomesVeiculos
        )

        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spVeiculo.adapter = adapter

        spVeiculo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                atualizarDadosDoVeiculo(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        selecionarVeiculoInicial()
    }

    private fun atualizarDadosDoVeiculo(posicao: Int) {
        val veiculo = veiculos.getOrNull(posicao) ?: return

        etKm.setText(veiculo.quilometragem.toString())
        etKm.error = null
        atualizarGraficoConsumo(veiculo.id)
    }

    private fun atualizarGraficoConsumo(veiculoId: Int) {
        val consumos = BancoHelper.getInstance(this).historicoConsumo(
            veiculoId, QUANTIDADE_MESES_HISTORICO
        )

        llBarrasConsumo.removeAllViews()

        if (consumos.isEmpty()) {
            exibirEstadoSemHistorico()
            return
        }

        exibirGraficoConsumo()
        adicionarBarrasConsumo(consumos)
    }

    private fun exibirEstadoSemHistorico() {
        tvSemHistoricoConsumo.visibility = View.VISIBLE
        llBarrasConsumo.visibility = View.GONE
    }

    private fun exibirGraficoConsumo() {
        tvSemHistoricoConsumo.visibility = View.GONE
        llBarrasConsumo.visibility = View.VISIBLE
    }

    private fun adicionarBarrasConsumo(consumos: List<Double>) {
        val maiorConsumo = consumos.maxOrNull() ?: 0.0
        val densidade = resources.displayMetrics.density
        val alturaMaxima = (ALTURA_MAXIMA_BARRA_DP * densidade).toInt()
        val alturaMinima = (ALTURA_MINIMA_BARRA_DP * densidade).toInt()
        val larguraBarra = (LARGURA_BARRA_DP * densidade).toInt()
        val margemInferior = (MARGEM_BARRA_DP * densidade).toInt()

        consumos.forEach { consumo ->
            val altura = calcularAlturaBarra(
                consumo,
                maiorConsumo,
                alturaMaxima,
                alturaMinima
            )

            val coluna = criarColunaConsumo()
            val barra = criarBarraConsumo(larguraBarra, altura, margemInferior)
            val rotulo = criarRotuloConsumo(consumo)

            coluna.addView(barra)
            coluna.addView(rotulo)
            llBarrasConsumo.addView(coluna)
        }
    }

    private fun calcularAlturaBarra(
        consumo: Double,
        maiorConsumo: Double,
        alturaMaxima: Int,
        alturaMinima: Int
    ): Int {
        if (maiorConsumo <= 0.0) return alturaMinima

        return ((consumo / maiorConsumo) * alturaMaxima)
            .toInt()
            .coerceAtLeast(alturaMinima)
    }

    private fun criarColunaConsumo(): LinearLayout {
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

    private fun criarBarraConsumo(largura: Int, altura: Int, margemInferior: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(largura, altura).apply {
                bottomMargin = margemInferior
            }
            setBackgroundResource(R.drawable.barra_mensal_arredondada)
        }
    }

    private fun criarRotuloConsumo(consumo: Double): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = String.format(Locale.US, FORMATO_ROTULO_CONSUMO, consumo)
            textSize = TAMANHO_TEXTO_ROTULO_SP
            setTextColor(COR_TEXTO_ROTULO)
        }
    }

    private fun selecionarVeiculoInicial() {
        val idVeiculo = intent.getIntExtra(
            Constantes.EXTRA_VEICULO_ID,
            Constantes.ID_INVALIDO
        )
        val posicaoVeiculo = veiculos.indexOfFirst { it.id == idVeiculo }
            .takeIf { it >= 0 }
            ?: POSICAO_INICIAL

        spVeiculo.setSelection(posicaoVeiculo)
        atualizarDadosDoVeiculo(posicaoVeiculo)
    }

    private fun configurarCampoData() {
        atualizarDataExibida()
        etData.setOnClickListener { abrirCalendario() }
    }

    private fun atualizarDataExibida() {
        etData.setText(formatoDataExibicao.format(dataSelecionada.time))
    }

    private fun abrirCalendario() {
        val calendario = DatePickerDialog(
            this,
            { _, ano, mes, dia ->
                dataSelecionada.set(ano, mes, dia, 0, 0, 0)
                dataSelecionada.set(Calendar.MILLISECOND, 0)
                atualizarDataExibida()
            },
            dataSelecionada.get(Calendar.YEAR),
            dataSelecionada.get(Calendar.MONTH),
            dataSelecionada.get(Calendar.DAY_OF_MONTH)
        )

        calendario.datePicker.maxDate = System.currentTimeMillis()
        calendario.show()
    }

    private fun configurarCalculoValores() {
        etLitros.doAfterTextChanged {
            if (!atualizandoValores) recalcularValores()
        }

        etValorLitro.doAfterTextChanged {
            if (!atualizandoValores) {
                ultimoCampoPrecoAlterado = etValorLitro
                recalcularValores()
            }
        }

        etValorTotal.doAfterTextChanged {
            if (!atualizandoValores) {
                ultimoCampoPrecoAlterado = etValorTotal
                recalcularValores()
            }
        }
    }

    private fun recalcularValores() {
        atualizandoValores = true

        try {
            val litros = lerNumero(etLitros)

            when (ultimoCampoPrecoAlterado) {
                etValorLitro -> calcularValorTotal(litros)
                etValorTotal -> calcularValorPorLitro(litros)
            }
        } finally {
            atualizandoValores = false
        }
    }

    private fun lerNumero(campo: EditText): Double? {
        return campo.text
            .toString()
            .trim()
            .replace(',', '.')
            .toDoubleOrNull()
    }

    private fun calcularValorTotal(litros: Double?) {
        val valorPorLitro = lerNumero(etValorLitro)
        val valorTotal = if (litros != null && valorPorLitro != null) {
            litros * valorPorLitro
        } else {
            null
        }

        atualizarCampoNumerico(etValorTotal, valorTotal)
    }

    private fun calcularValorPorLitro(litros: Double?) {
        val valorTotal = lerNumero(etValorTotal)
        val valorPorLitro = if (
            litros != null &&
            litros > 0 &&
            valorTotal != null
        ) {
            valorTotal / litros
        } else {
            null
        }

        atualizarCampoNumerico(etValorLitro, valorPorLitro)
    }

    private fun atualizarCampoNumerico(campo: EditText, valor: Double?) {
        campo.setText(valor?.let(::formatarNumero) ?: "")
    }

    private fun formatarNumero(valor: Double): String {
        return String.format(LOCALE_BRASIL, FORMATO_VALOR_MONETARIO, valor)
    }

    private fun configurarBotaoSalvar() {
        findViewById<Button>(R.id.btnSalvarAbastecimento).setOnClickListener {
            salvarAbastecimento()
        }
    }

    private fun salvarAbastecimento() {
        val abastecimento = validarAbastecimento() ?: return

        BancoHelper.getInstance(this).registrarAbastecimento(abastecimento)
        exibirMensagem(MENSAGEM_ABASTECIMENTO_SALVO)
        finalizarTela()
    }

    private fun validarAbastecimento(): RegistroAbastecimento? {
        val veiculo = veiculos.getOrNull(spVeiculo.selectedItemPosition) ?: return null
        val litros = lerNumero(etLitros)
        val valorPorLitro = lerNumero(etValorLitro)
        val valorTotal = lerNumero(etValorTotal)
        val quilometragem = etKm.text.toString().trim().toIntOrNull()
        val posto = etPosto.text.toString().trim()

        if (!validarLitros(litros)) return null
        if (!validarValorPorLitro(valorPorLitro)) return null
        if (!validarValorTotal(valorTotal)) return null
        if (!validarQuilometragem(quilometragem, veiculo.quilometragem)) return null

        return RegistroAbastecimento(
            veiculoId = veiculo.id,
            litros = litros!!,
            valorLitro = valorPorLitro!!,
            valorTotal = valorTotal!!,
            quilometragem = quilometragem!!,
            posto = posto,
            data = formatoDataBanco.format(dataSelecionada.time)
        )
    }

    private fun validarLitros(litros: Double?): Boolean {
        if (litros != null && litros > 0) return true

        etLitros.error = MENSAGEM_LITROS_INVALIDOS
        return false
    }

    private fun validarValorPorLitro(valor: Double?): Boolean {
        if (valor != null && valor > 0) return true

        etValorLitro.error = MENSAGEM_VALOR_LITRO_INVALIDO
        return false
    }

    private fun validarValorTotal(valor: Double?): Boolean {
        if (valor != null && valor > 0) return true

        etValorTotal.error = MENSAGEM_VALOR_TOTAL_INVALIDO
        return false
    }

    private fun validarQuilometragem(quilometragem: Int?, quilometragemAnterior: Int): Boolean {
        if (quilometragem == null || quilometragem < 0) {
            etKm.error = MENSAGEM_QUILOMETRAGEM_INVALIDA
            return false
        }

        if (quilometragem < quilometragemAnterior) {
            etKm.error = "$MENSAGEM_QUILOMETRAGEM_MENOR $quilometragemAnterior"
            return false
        }

        return true
    }

    private companion object {
        const val POSICAO_INICIAL = 0
        const val QUANTIDADE_MESES_HISTORICO = 6

        const val FORMATO_DATA_EXIBICAO = "dd/MM/yyyy"
        const val FORMATO_DATA_BANCO = "yyyy-MM-dd"
        const val FORMATO_VALOR_MONETARIO = "%.2f"
        const val FORMATO_ROTULO_CONSUMO = "%.1f"

        const val ALTURA_MAXIMA_BARRA_DP = 44
        const val ALTURA_MINIMA_BARRA_DP = 14
        const val LARGURA_BARRA_DP = 22
        const val MARGEM_BARRA_DP = 6
        const val TAMANHO_TEXTO_ROTULO_SP = 11f

        const val MENSAGEM_CADASTRAR_VEICULO = "Cadastre um veículo primeiro"
        const val MENSAGEM_ABASTECIMENTO_SALVO = "Abastecimento salvo"
        const val MENSAGEM_LITROS_INVALIDOS = "Informe os litros"
        const val MENSAGEM_VALOR_LITRO_INVALIDO = "Informe um valor por litro válido"
        const val MENSAGEM_VALOR_TOTAL_INVALIDO = "Informe um valor total válido"
        const val MENSAGEM_QUILOMETRAGEM_INVALIDA = "Informe um KM válido"
        const val MENSAGEM_QUILOMETRAGEM_MENOR = "KM menor do que no último registro:"

        val LOCALE_BRASIL: Locale = Locale.forLanguageTag("pt-BR")
        val COR_TEXTO_ROTULO: Int = Color.parseColor("#64748B")
    }
}