package com.example.virabraking

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.EditText
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegistrarServico : AppCompatActivity() {

    private var veiculos = listOf<Veiculo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_servico)

        val llInicio = findViewById<LinearLayout>(R.id.llInicio)
        llInicio.setOnClickListener {
            startActivity(Intent(this, Inicio::class.java))
        }

        val llVeiculos = findViewById<LinearLayout>(R.id.llVeiculos)
        llVeiculos.setOnClickListener {
            startActivity(Intent(this, Veiculos::class.java))
        }

        val llAlertas = findViewById<LinearLayout>(R.id.llAlertas)
        llAlertas.setOnClickListener {
            startActivity(Intent(this, Alertas::class.java))
        }

        val llRelatorios = findViewById<LinearLayout>(R.id.llRelatorios)
        llRelatorios.setOnClickListener {
            startActivity(Intent(this, Relatorios::class.java))
        }

        val sTipoServico = findViewById<Spinner>(R.id.sTipoServico)

        val itensTipoServico = mutableListOf<TipoServico>()
        TipoServico.entries.forEach(itensTipoServico::add)

        val tipoServico = mutableListOf("Selecione")
        tipoServico.addAll(itensTipoServico.map { it.descricao })

        val adapter = ArrayAdapter(this, R.layout.item_spinner_tipo_servico, tipoServico)
        adapter.setDropDownViewResource(R.layout.item_spinner_tipo_servico)
        sTipoServico.adapter = adapter

        val spVeiculo = findViewById<Spinner>(R.id.spVeiculo)
        veiculos = BancoHelper(this).listarVeiculos()

        val adapterVeiculo =
            ArrayAdapter(this, R.layout.item_spinner_tipo_servico, veiculos.map { it.apelido })
        adapterVeiculo.setDropDownViewResource(R.layout.item_spinner_tipo_servico)
        spVeiculo.adapter = adapterVeiculo

        // Se a tela for iniciada com um veículo já definido, seleciona-o no Spinner
        val idInicial = intent.getIntExtra(Constantes.EXTRA_VEICULO_ID, Constantes.ID_INVALIDO)
        val posicaoInicial = veiculos.indexOfFirst { it.id == idInicial }
        if (posicaoInicial >= 0) {
            spVeiculo.setSelection(posicaoInicial)
        }

        // Seletor de data: evita problemas com o teclado e garante o formato dd/MM/yyyy
        val etData = findViewById<EditText>(R.id.etData)
        etData.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, ano, mes, dia ->
                    etData.setText(String.format(Locale.US, "%02d/%02d/%04d", dia, mes + 1, ano))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val btnSalvarManutencao = findViewById<Button>(R.id.btnSalvarManutencao)
        btnSalvarManutencao.setOnClickListener {
            salvarServico()
        }

        val btnAbastecer = findViewById<Button>(R.id.btnAbastecer)
        btnAbastecer.setOnClickListener {
            val intent = Intent(this, Abastecimento::class.java)
            if (veiculos.isNotEmpty()) {
                intent.putExtra(Constantes.EXTRA_VEICULO_ID, veiculos[spVeiculo.selectedItemPosition].id)
            }
            startActivity(intent)
        }
    }

    private fun salvarServico() {
        val posVeiculo = findViewById<Spinner>(R.id.spVeiculo).selectedItemPosition
        val posTipo = findViewById<Spinner>(R.id.sTipoServico).selectedItemPosition
        val dataTexto = findViewById<EditText>(R.id.etData).text.toString().trim()
        val quilometragem =
            findViewById<EditText>(R.id.etQuilometragem).text.toString().toIntOrNull() ?: 0
        val valor =
            findViewById<EditText>(R.id.etValor).text.toString().replace(",", ".").toDoubleOrNull()
                ?: 0.0
        val oficina = findViewById<EditText>(R.id.etOficina).text.toString().trim()
        val observacoes = findViewById<EditText>(R.id.etObservacoes).text.toString().trim()

        if (veiculos.isEmpty()) {
            Toast.makeText(this, "Cadastre um veículo primeiro", Toast.LENGTH_SHORT).show()
            return
        }

        if (posTipo == 0) {
            Toast.makeText(this, "Selecione o tipo de serviço", Toast.LENGTH_SHORT).show()
            return
        }

        val formatoEntrada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formatoEntrada.isLenient = false

        val dataFormatada = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                formatoEntrada.parse(dataTexto)!!
            )
        } catch (e: Exception) {
            null
        }

        if (dataFormatada == null) {
            Toast.makeText(this, "Selecione a data do serviço", Toast.LENGTH_SHORT).show()
            return
        }

        val servico = Servico(
            veiculoId = veiculos[posVeiculo].id,
            tipo = TipoServico.entries[posTipo - 1].descricao,
            data = dataFormatada,
            quilometragem = quilometragem,
            valor = valor,
            oficina = oficina,
            observacoes = observacoes
        )

        val resultado = BancoHelper(this).inserirServico(servico)
        if (resultado == -1L) {
            Toast.makeText(this, "Erro ao salvar o serviço", Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(this, "Serviço salvo", Toast.LENGTH_SHORT).show()
        finish()
    }
}