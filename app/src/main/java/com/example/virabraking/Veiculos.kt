package com.example.virabraking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog

class Veiculos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_veiculos)

        val llInicio = findViewById<LinearLayout>(R.id.llInicio)
        llInicio.setOnClickListener{
            startActivity(Intent(this, Inicio::class.java))
        }

        val llAlertas = findViewById<LinearLayout>(R.id.llAlertas)
        llAlertas.setOnClickListener{
            startActivity(Intent(this, Alertas::class.java))
        }

        val llRelatorios = findViewById<LinearLayout>(R.id.llRelatorios)
        llRelatorios.setOnClickListener {
            startActivity(Intent(this, Relatorios::class.java))
        }

        val iBtnAdicionar = findViewById<ImageButton>(R.id.iBtnAdicionar)
        iBtnAdicionar.setOnClickListener {
            startActivity(Intent(this, AdicionarVeiculo::class.java))
        }

        val llAdicionar = findViewById<LinearLayout>(R.id.llAdicionar)
        llAdicionar.setOnClickListener {
            startActivity(Intent(this, RegistrarServico::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        carregarVeiculos()
    }

    private fun carregarVeiculos() {
        val rvVeiculos = findViewById<RecyclerView>(R.id.rvVeiculos)
        rvVeiculos.layoutManager = LinearLayoutManager(this)
        rvVeiculos.adapter = VeiculoAdapter(
            BancoHelper(this).listarVeiculos(),
            { veiculo ->
                val intent = Intent(this, DetalhesVeiculo::class.java)
                intent.putExtra("veiculoId", veiculo.id)
                startActivity(intent)
            },
            { veiculo -> confirmarRemocao(veiculo) }
        )
    }

    private fun confirmarRemocao(veiculo: Veiculo) {
        AlertDialog.Builder(this)
            .setTitle("Remover veículo")
            .setMessage("Remover ${veiculo.apelido} e todos os serviços dele?")
            .setPositiveButton("Remover") { _, _ ->
                BancoHelper(this).removerVeiculo(veiculo.id)
                carregarVeiculos()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}