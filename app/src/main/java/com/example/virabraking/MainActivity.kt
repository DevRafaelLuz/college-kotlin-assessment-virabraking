package com.example.virabraking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvAppVersion = findViewById<TextView>(R.id.tvAppVersion)
        val versionName = BuildConfig.VERSION_NAME
        tvAppVersion.text = "V$versionName"
    }
}