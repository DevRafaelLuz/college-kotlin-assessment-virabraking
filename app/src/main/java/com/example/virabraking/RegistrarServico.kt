package com.example.virabraking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner

class RegistrarServico : AppCompatActivity() {
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

        val btnAbastecer = findViewById<Button>(R.id.btnAbastecer)
        btnAbastecer.setOnClickListener {
            startActivity(Intent(this, Abastecimento::class.java))
        }
    }
}