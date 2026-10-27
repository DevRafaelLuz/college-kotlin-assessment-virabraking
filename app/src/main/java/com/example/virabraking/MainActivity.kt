package com.example.virabraking

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ivEngrenagem = findViewById<ImageView>(R.id.ivEngrenagem)
        val ivPistoes = findViewById<ImageView>(R.id.ivPistoes)

        ivEngrenagem.alpha = 1f
        ivPistoes.alpha = 1f

        ivEngrenagem.animate()
            .rotation(360f)
            .setDuration(2500)
            .withEndAction {
                startActivity(Intent(this, Inicio::class.java))
                finish()
            }
            .start()
    }
}