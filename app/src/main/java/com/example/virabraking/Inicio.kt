package com.example.virabraking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout

class Inicio : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        val llVeiculos = findViewById<LinearLayout>(R.id.llVeiculos)
        llVeiculos.setOnClickListener{
            startActivity(Intent(this, Veiculos::class.java))
        }
    }
}