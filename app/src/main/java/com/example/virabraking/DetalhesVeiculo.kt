package com.example.virabraking

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.google.android.material.button.MaterialButton

class DetalhesVeiculo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_veiculo)

        val iBtnVoltar = findViewById<ImageButton>(R.id.iBtnVoltar)
        iBtnVoltar.setOnClickListener{
            finish()
        }

        alterarEstiloBotaoSelecionado(obtemListaBotoes())
    }

    private fun alterarEstiloBotaoSelecionado(listaBotoes: List<MaterialButton>) {
        listaBotoes.forEach {
            botao -> botao.setOnClickListener {
                listaBotoes.forEach {
                    botoes -> botoes.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))
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
        val btnHistorico = findViewById<MaterialButton>(R.id.btnHistorico)
        val btnCombustivel = findViewById<MaterialButton>(R.id.btnCombustivel)
        val btnDocumentos = findViewById<MaterialButton>(R.id.btnDocumentos)

        return listOf(btnHistorico, btnCombustivel, btnDocumentos)
    }
}