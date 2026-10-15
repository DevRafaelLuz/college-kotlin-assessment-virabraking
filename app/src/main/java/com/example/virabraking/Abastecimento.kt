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

    private var veiculos = listOf<Veiculo>()
    private var ultimoPreco: EditText? = null   // qual preço o usuário digitou por último
    private var atualizando = false             // evita loop entre os TextWatchers

    private val dataSelecionada: Calendar = Calendar.getInstance()
    private val localeBr: Locale = Locale.forLanguageTag("pt-BR")
    private val formatoExibicao = SimpleDateFormat("dd/MM/yyyy", localeBr)
    private val formatoBanco = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abastecimento)

        val iBtnVoltar = findViewById<ImageButton>(R.id.iBtnVoltar)
        iBtnVoltar.setOnClickListener {
            finish()
        }

        veiculos = BancoHelper(this).listarVeiculos()
        if (veiculos.isEmpty()) {
            Toast.makeText(this, "Cadastre um veículo primeiro", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        spVeiculo = findViewById(R.id.spVeiculo)
        etLitros = findViewById(R.id.etLitros)
        etValorLitro = findViewById(R.id.etValorLitro)
        etValorTotal = findViewById(R.id.etValorTotal)
        etKm = findViewById(R.id.etKm)
        etPosto = findViewById(R.id.etPosto)
        etData = findViewById(R.id.etData)

        configurarSeletorVeiculo()

        etData.setText(formatoExibicao.format(dataSelecionada.time))
        etData.setOnClickListener { abrirCalendario() }

        configurarCalculo()

        val btnSalvar = findViewById<Button>(R.id.btnSalvarAbastecimento)
        btnSalvar.setOnClickListener {
            salvarAbastecimento()
        }
    }

    private fun configurarSeletorVeiculo() {
        val adapter = ArrayAdapter(this, R.layout.item_spinner_tipo_servico, veiculos.map { it.apelido })
        adapter.setDropDownViewResource(R.layout.item_spinner_tipo_servico)
        spVeiculo.adapter = adapter

        // Se a tela foi aberta já com um veículo escolhido, deixa ele selecionado
        val idInicial = intent.getIntExtra("veiculo_id", -1)
        val posicaoInicial = veiculos.indexOfFirst { it.id == idInicial }
        if (posicaoInicial >= 0) {
            spVeiculo.setSelection(posicaoInicial)
        }

        // O hint de KM mostra o mínimo do veículo escolhido
        spVeiculo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                etKm.hint = "Mín. ${veiculos[position].quilometragem}"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun abrirCalendario() {
        val dialog = DatePickerDialog(
            this,
            { _, ano, mes, dia ->
                dataSelecionada.set(ano, mes, dia)
                etData.setText(formatoExibicao.format(dataSelecionada.time))
            },
            dataSelecionada.get(Calendar.YEAR),
            dataSelecionada.get(Calendar.MONTH),
            dataSelecionada.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.maxDate = System.currentTimeMillis()
        dialog.show()
    }

    // Litros é a âncora. O preço digitado por último define o que é calculado:
    // valor/litro digitado -> calcula o total; total digitado -> calcula o valor/litro
    private fun configurarCalculo() {
        etLitros.doAfterTextChanged {
            if (!atualizando) recalcular()
        }
        etValorLitro.doAfterTextChanged {
            if (!atualizando) {
                ultimoPreco = etValorLitro
                recalcular()
            }
        }
        etValorTotal.doAfterTextChanged {
            if (!atualizando) {
                ultimoPreco = etValorTotal
                recalcular()
            }
        }
    }

    private fun recalcular() {
        val litros = lerNumero(etLitros)
        atualizando = true

        if (ultimoPreco == etValorLitro) {
            val valorLitro = lerNumero(etValorLitro)
            if (litros != null && valorLitro != null) {
                etValorTotal.setText(formatar(litros * valorLitro))
            } else {
                etValorTotal.setText("")
            }
        } else if (ultimoPreco == etValorTotal) {
            val total = lerNumero(etValorTotal)
            if (litros != null && litros > 0 && total != null) {
                etValorLitro.setText(formatar(total / litros))
            } else {
                etValorLitro.setText("")
            }
        }

        atualizando = false
    }

    private fun lerNumero(campo: EditText): Double? {
        return campo.text.toString().trim().replace(',', '.').toDoubleOrNull()
    }

    private fun formatar(valor: Double): String {
        return String.format(localeBr, "%.2f", valor)
    }

    private fun salvarAbastecimento() {
        val veiculo = veiculos[spVeiculo.selectedItemPosition]
        val litros = lerNumero(etLitros)
        val valorLitro = lerNumero(etValorLitro)
        val valorTotal = lerNumero(etValorTotal)
        val km = etKm.text.toString().trim().toIntOrNull()
        val posto = etPosto.text.toString().trim()

        if (litros == null || litros <= 0) {
            etLitros.error = "Informe os litros"
            return
        }
        if (valorLitro == null || valorLitro <= 0 || valorTotal == null || valorTotal <= 0) {
            Toast.makeText(this, "Informe o valor por litro ou o valor total", Toast.LENGTH_SHORT).show()
            return
        }
        if (km == null) {
            etKm.error = "Informe o KM atual"
            return
        }
        if (km < veiculo.quilometragem) {
            etKm.error = "O KM não pode ser menor que ${veiculo.quilometragem}"
            return
        }

        val abastecimento = RegistroAbastecimento(
            veiculoId = veiculo.id,
            litros = litros,
            valorLitro = valorLitro,
            valorTotal = valorTotal,
            quilometragem = km,
            posto = posto,
            data = formatoBanco.format(dataSelecionada.time)
        )

        BancoHelper(this).registrarAbastecimento(abastecimento)
        Toast.makeText(this, "Abastecimento salvo", Toast.LENGTH_SHORT).show()
        finish()
    }
}