package com.example.virabraking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvAppVersion = findViewById<TextView>(R.id.tvAppVersion)
        val versionName = BuildConfig.VERSION_NAME
        tvAppVersion.text = "V$versionName"

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