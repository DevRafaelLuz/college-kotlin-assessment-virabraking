package com.example.virabraking

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

fun AppCompatActivity.mostrarSobre() {
    val view = layoutInflater.inflate(R.layout.dialog_sobre, null)
    view.findViewById<TextView>(R.id.tvVersaoSobre).text = "Versão ${BuildConfig.VERSION_NAME}"

    val dialog = AlertDialog.Builder(this)
        .setView(view)
        .create()

    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    view.findViewById<MaterialButton>(R.id.btnFecharSobre).setOnClickListener {
        dialog.dismiss()
    }

    dialog.show()
}
