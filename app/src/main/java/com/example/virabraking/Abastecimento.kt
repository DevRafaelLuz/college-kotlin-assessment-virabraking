package com.example.virabraking

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
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

    private var veiculos: List<Veiculo> = emptyList()
    private var ultimoCampoPrecoAlterado: EditText? = null
    private var atualizandoValores = false
    private val dataSelecionada = Calendar.getInstance()
    private val localeBrasil = Locale.forLanguageTag("pt-BR")
    private val formatoDataExibicao = SimpleDateFormat("dd/MM/yyyy", localeBrasil)
    private val formatoDataBanco = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abastecimento)

        if (!carregarVeiculos()) {
            return
        }

        inicializarViews()
        configurarBotaoVoltar()
        configurarSeletorVeiculo()
        configurarCampoData()
        configurarCalculoValores()
        configurarBotaoSalvar()
    }

    private fun carregarVeiculos(): Boolean {
        veiculos = BancoHelper.getInstance(this).listarVeiculos()

        if (veiculos.isNotEmpty()) {
            return true
        }

        exibirMensagem("Cadastre um veículo primeiro")
        finish()
        return false
    }

    private fun exibirMensagem(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT ).show()
    }

    private fun inicializarViews() {
        spVeiculo = findViewById(R.id.spVeiculo)
        etLitros = findViewById(R.id.etLitros)
        etValorLitro = findViewById(R.id.etValorLitro)
        etValorTotal = findViewById(R.id.etValorTotal)
        etKm = findViewById(R.id.etKm)
        etPosto = findViewById(R.id.etPosto)
        etData = findViewById(R.id.etData)
    }

    private fun configurarBotaoVoltar() {
        findViewById<ImageButton>(R.id.iBtnVoltar).setOnClickListener {
            finish()
        }
    }
    private fun configurarSeletorVeiculo() {
        val nomesVeiculos = veiculos.map { it.apelido }

        val adapter = ArrayAdapter(this, R.layout.item_spinner_tipo_servico, nomesVeiculos)

        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)

        spVeiculo.adapter = adapter
        selecionarVeiculoInicial()
        configurarAtualizacaoQuilometragem()
    }

    private fun selecionarVeiculoInicial() {
        val idVeiculo = intent.getIntExtra(Constantes.EXTRA_VEICULO_ID, Constantes.ID_INVALIDO)

        val posicao = veiculos.indexOfFirst {
            it.id == idVeiculo
        }

        if (posicao >= 0) {
            spVeiculo.setSelection(posicao)
        }
    }

    private fun configurarAtualizacaoQuilometragem() {
        spVeiculo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val quilometragem = veiculos[position].quilometragem

                etKm.hint = "Mín. $quilometragem"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun configurarCampoData() {
        atualizarDataExibida()

        etData.setOnClickListener {
            abrirCalendario()
        }
    }

    private fun atualizarDataExibida() {
        etData.setText(formatoDataExibicao.format(dataSelecionada.time))
    }

    private fun abrirCalendario() {
        val calendario = DatePickerDialog(this, {
                _, ano, mes, dia -> dataSelecionada.set(ano, mes, dia, 0, 0, 0)
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
            if (!atualizandoValores) {
                recalcularValores()
            }
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
        val valorLitro = lerNumero(etValorLitro)

        val total = if (
            litros != null &&
            valorLitro != null
        ) {
            litros * valorLitro
        } else {
            null
        }

        atualizarCampoNumerico(etValorTotal, total)
    }

    private fun atualizarCampoNumerico(campo: EditText, valor: Double?) {
        campo.setText(valor?.let { formatarNumero(it) } ?: "")
    }

    private fun formatarNumero(valor: Double): String {
        return String.format(localeBrasil, "%.2f", valor)
    }

    private fun calcularValorPorLitro(litros: Double?) {
        val total = lerNumero(etValorTotal)

        val valorLitro = if (
            litros != null &&
            litros > 0 &&
            total != null
        ) {
            total / litros
        } else {
            null
        }

        atualizarCampoNumerico(etValorLitro, valorLitro)
    }

    private fun configurarBotaoSalvar() {
        findViewById<Button>(R.id.btnSalvarAbastecimento).setOnClickListener {
            salvarAbastecimento()
        }
    }

    private fun salvarAbastecimento() {
        val abastecimento = validarAbastecimento() ?: return

        BancoHelper.getInstance(this).registrarAbastecimento(abastecimento)

        exibirMensagem("Abastecimento salvo")
        finish()
    }

    private fun validarAbastecimento(): RegistroAbastecimento? {
        val veiculo = veiculos.getOrNull(spVeiculo.selectedItemPosition) ?: return null

        val litros = lerNumero(etLitros)
        val valorLitro = lerNumero(etValorLitro)
        val valorTotal = lerNumero(etValorTotal)
        val quilometragem = etKm.text.toString().trim().toIntOrNull()

        val posto = etPosto.text.toString().trim()

        if (litros == null || litros <= 0) {
            etLitros.error = "Informe os litros"
            return null
        }

        if (valorLitro == null || valorLitro <= 0) {
            etValorLitro.error = "Informe um valor por litro válido"
            return null
        }

        if (valorTotal == null || valorTotal <= 0) {
            etValorTotal.error = "Informe um valor total válido"
            return null
        }

        if (quilometragem == null || quilometragem < 0) {
            etKm.error = "Informe um KM válido"
            return null
        }

        if (quilometragem < veiculo.quilometragem) {
            etKm.error = "O KM não pode ser menor que ${veiculo.quilometragem}"
            return null
        }

        return RegistroAbastecimento(
            veiculoId = veiculo.id,
            litros = litros,
            valorLitro = valorLitro,
            valorTotal = valorTotal,
            quilometragem = quilometragem,
            posto = posto,
            data = formatoDataBanco.format(dataSelecionada.time)
        )
    }
}