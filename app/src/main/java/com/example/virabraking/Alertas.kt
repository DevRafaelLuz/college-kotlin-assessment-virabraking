package com.example.virabraking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout

class Alertas : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alertas)

        val llInicio = findViewById<LinearLayout>(R.id.llInicio)
        llInicio.setOnClickListener {
            startActivity(Intent(this, Inicio::class.java))
        }

        val llVeiculos = findViewById<LinearLayout>(R.id.llVeiculos)
        llVeiculos.setOnClickListener {
            startActivity(Intent(this, Veiculos::class.java))
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
}