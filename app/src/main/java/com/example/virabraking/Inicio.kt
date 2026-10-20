package com.example.virabraking

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Inicio : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

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

        val llAdicionar = findViewById<LinearLayout>(R.id.llAdicionar)
        llAdicionar.setOnClickListener {
            startActivity(Intent(this, RegistrarServico::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        carregarDados()
    }

    private fun carregarDados() {
        val banco = BancoHelper(this)

        findViewById<TextView>(R.id.tvQtdVeiculos).text = "${banco.contarVeiculos()} Ativos"

        val inicioMes = inicioDoMes()
        val gastoMes = banco.totalServicosDesde(inicioMes) + banco.totalCombustivelDesde(inicioMes)
        findViewById<TextView>(R.id.tvGastosMes).text = gastoMes.emReais()

        val container = findViewById<LinearLayout>(R.id.llListaAtividades)
        container.removeAllViews()

        val atividades = banco.listarAtividades(limite = 3)
        if (atividades.isEmpty()) {
            val vazio = TextView(this)
            vazio.text = "Nenhuma atividade registrada ainda"
            vazio.setTextColor(0xFF64748B.toInt())
            container.addView(vazio)
        } else {
            atividades.forEach { container.addView(it.paraView(layoutInflater, container)) }
        }
    }
}