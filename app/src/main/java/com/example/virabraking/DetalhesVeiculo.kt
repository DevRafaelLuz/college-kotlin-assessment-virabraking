package com.example.virabraking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class DetalhesVeiculo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_veiculo)

        val iBtnVoltar = findViewById<ImageButton>(R.id.iBtnVoltar)
        iBtnVoltar.setOnClickListener{
            finish()
        }
    }
}