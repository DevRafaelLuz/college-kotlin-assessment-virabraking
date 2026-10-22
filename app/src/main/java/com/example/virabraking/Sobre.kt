package com.example.virabraking

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.mostrarSobre() {
    val view = layoutInflater.inflate(R.layout.dialog_sobre, null)
    view.findViewById<TextView>(R.id.tvVersaoSobre).text = "Versão ${BuildConfig.VERSION_NAME}"

    AlertDialog.Builder(this)
        .setView(view)
        .setPositiveButton("Fechar", null)
        .show().window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#f8fafc")))
}