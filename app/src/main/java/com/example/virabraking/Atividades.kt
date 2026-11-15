package com.example.virabraking

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Atividades : AppCompatActivity() {
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_atividades)

        inicializarComponentes()
        configurarNavegacao()
    }

    override fun onResume() {
        super.onResume()
        carregarAtividades()
    }

    private fun inicializarComponentes() {
        container = findViewById(R.id.llTodasAtividades)
    }

    private fun configurarNavegacao() {
        findViewById<ImageButton>(R.id.iBtnVoltar)
            .setOnClickListener {
                finish()
                overridePendingTransition(0, 0)
            }
    }

    private fun carregarAtividades() {
        val banco = BancoHelper.getInstance(this)

        container.removeAllViews()

        val atividades = banco.listarAtividades(
            limite = null
        )

        if (atividades.isEmpty()) {
            exibirListaVazia()
            return
        }

        atividades.forEach { atividade ->
            container.addView(
                atividade.paraView(layoutInflater, container)
            )
        }
    }

    private fun exibirListaVazia() {
        val mensagem = TextView(this).apply {
            text = "Nenhuma atividade registrada ainda"
            textSize = 14f
            setTextColor(0xFF64748B.toInt())
            setPadding(0, 24, 0, 0)
        }

        container.addView(mensagem)
    }
}
