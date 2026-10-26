package com.example.virabraking

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ivLogo = findViewById<ImageView>(R.id.ivLogo)
        ivLogo.animate()
            .alpha(1f)
            .rotation(360f)
            .setDuration(2500)
            .withEndAction {
                startActivity(Intent(this, Inicio::class.java))
                finish()
            }
            .start()
    }
}