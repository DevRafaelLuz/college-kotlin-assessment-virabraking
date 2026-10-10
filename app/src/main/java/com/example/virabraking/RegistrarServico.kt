package com.example.virabraking

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

        val btnSalvarManutencao = findViewById<Button>(R.id.btnSalvarManutencao)
        btnSalvarManutencao.setOnClickListener {
            salvarServico()
        }

        val btnAbastecer = findViewById<Button>(R.id.btnAbastecer)
        btnAbastecer.setOnClickListener {
            startActivity(Intent(this, Abastecimento::class.java))
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
                formatoEntrada.parse(
                    dataTexto
                )!!
            )
        } catch (e: Exception) {
            null
        }

        if (dataFormatada == null) {
            Toast.makeText(this, "Data inválida (dd/mm/aaaa)", Toast.LENGTH_SHORT).show()
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

        BancoHelper(this).inserirServico(servico)
        Toast.makeText(this, "Serviço salvo", Toast.LENGTH_SHORT).show()
        finish()
    }
}