package com.example.virabraking

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton

class Relatorios : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorios)

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

        val llAdicionar = findViewById<LinearLayout>(R.id.llAdicionar)
        llAdicionar.setOnClickListener {
            startActivity(Intent(this, RegistrarServico::class.java))
        }

        alterarEstiloBotaoSelecionado(obtemListaBotoes())
    }

    private fun alterarEstiloBotaoSelecionado(listaBotoes: List<MaterialButton>) {
        listaBotoes.forEach { botao ->
            botao.setOnClickListener {
                listaBotoes.forEach { botoes ->
                    botoes.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                    botoes.strokeColor = ColorStateList.valueOf(Color.parseColor("#e2e8f0"))
                    botoes.setTextColor(Color.parseColor("#64748b"))
                }

                botao.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0d9488"))
                botao.strokeColor = ColorStateList.valueOf(Color.parseColor("#0d9488"))
                botao.setTextColor(Color.parseColor("#ffffff"))
            }
        }
    }

    private fun obtemListaBotoes(): List<MaterialButton> {
        val btnEsteMes = findViewById<MaterialButton>(R.id.btnEsteMes)
        val btnTrimestre = findViewById<MaterialButton>(R.id.btnTrimestre)
        val btnEsteAno = findViewById<MaterialButton>(R.id.btnEsteAno)

        return listOf(btnEsteMes, btnTrimestre, btnEsteAno)
    }
}