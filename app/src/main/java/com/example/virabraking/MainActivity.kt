package com.example.virabraking

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var ivEngrenagem: ImageView
    private lateinit var ivPistoes: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inicializarComponentes()
        configurarElementosVisuais()
        iniciarAnimacao()
    }

    private fun inicializarComponentes() {
        ivEngrenagem = findViewById(R.id.ivEngrenagem)
        ivPistoes = findViewById(R.id.ivPistoes)
    }

    private fun configurarElementosVisuais() {
        ivEngrenagem.alpha = OPACIDADE_COMPLETA
        ivPistoes.alpha = OPACIDADE_COMPLETA
    }

    private fun iniciarAnimacao() {
        ivEngrenagem.animate()
            .rotation(ROTACAO_COMPLETA)
            .setDuration(DURACAO_ANIMACAO)
            .withEndAction {
                abrirTelaInicial()
            }
            .start()
    }

    private fun abrirTelaInicial() {
        val intent = Intent(this, Inicio::class.java)
        startActivity(intent)
        finish()
    }

    private companion object {
        private const val OPACIDADE_COMPLETA = 1f
        private const val ROTACAO_COMPLETA = 360f
        private const val DURACAO_ANIMACAO = 2500L
    }
}