package com.example.virabraking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout

class Veiculos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_veiculos)

        val llInicio = findViewById<LinearLayout>(R.id.llInicio)
        llInicio.setOnClickListener{
            startActivity(Intent(this, Inicio::class.java))
        }
    }
}