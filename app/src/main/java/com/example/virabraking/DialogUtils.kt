package com.example.virabraking

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DialogUtils {

    enum class TipoDialog {
        ALERTA,
        DESTRUICAO,
        INFORMATIVO
    }

    fun exibirDialogoConfirmacao(
        activity: Activity,
        titulo: String,
        mensagem: String,
        textoConfirmar: String = "Confirmar",
        textoCancelar: String = "Cancelar",
        tipo: TipoDialog = TipoDialog.DESTRUICAO,
        onConfirmar: () -> Unit
    ) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_confirmacao, null)

        val ivIcone = view.findViewById<ImageView>(R.id.ivIconeDialog)
        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloDialog)
        val tvMensagem = view.findViewById<TextView>(R.id.tvMensagemDialog)
        val btnCancelar = view.findViewById<MaterialButton>(R.id.btnCancelarDialog)
        val btnConfirmar = view.findViewById<MaterialButton>(R.id.btnConfirmarDialog)

        tvTitulo.text = titulo
        tvMensagem.text = mensagem
        btnConfirmar.text = textoConfirmar
        btnCancelar.text = textoCancelar

        when (tipo) {
            TipoDialog.DESTRUICAO -> {
                ivIcone.setImageResource(R.drawable.vetor_lixeira)
                ivIcone.setColorFilter(Color.parseColor("#EF4444"))
                btnConfirmar.setBackgroundColor(Color.parseColor("#EF4444"))
            }
            TipoDialog.ALERTA -> {
                ivIcone.setImageResource(R.drawable.vetor_relogio_proxima_manutencao)
                ivIcone.setColorFilter(Color.parseColor("#F59E0B"))
                btnConfirmar.setBackgroundColor(Color.parseColor("#0D9488"))
            }
            TipoDialog.INFORMATIVO -> {
                ivIcone.setImageResource(R.drawable.vetor_check)
                ivIcone.setColorFilter(Color.parseColor("#0D9488"))
                btnConfirmar.setBackgroundColor(Color.parseColor("#0D9488"))
            }
        }

        val dialog = AlertDialog.Builder(activity)
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            dialog.dismiss()
            onConfirmar()
        }

        dialog.show()
    }

    fun exibirDialogoAlerta(
        activity: Activity,
        titulo: String,
        mensagem: String,
        textoBotao: String = "OK"
    ) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_confirmacao, null)

        val ivIcone = view.findViewById<ImageView>(R.id.ivIconeDialog)
        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloDialog)
        val tvMensagem = view.findViewById<TextView>(R.id.tvMensagemDialog)
        val btnCancelar = view.findViewById<MaterialButton>(R.id.btnCancelarDialog)
        val btnConfirmar = view.findViewById<MaterialButton>(R.id.btnConfirmarDialog)

        tvTitulo.text = titulo
        tvMensagem.text = mensagem
        btnConfirmar.text = textoBotao

        ivIcone.setImageResource(R.drawable.vetor_caminhao)
        ivIcone.setColorFilter(Color.parseColor("#0D9488"))
        btnConfirmar.setBackgroundColor(Color.parseColor("#0D9488"))

        btnCancelar.visibility = android.view.View.GONE

        val dialog = AlertDialog.Builder(activity)
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnConfirmar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun exibirSeletorData(
        activity: FragmentActivity,
        titulo: String = "Selecionar data",
        dataAtualMillis: Long = System.currentTimeMillis(),
        onDataSelecionada: (dataFormatada: String) -> Unit
    ) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(titulo)
            .setSelection(dataAtualMillis)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val timeZoneUtc = TimeZone.getTimeZone("UTC")
            val calendar = Calendar.getInstance(timeZoneUtc).apply {
                timeInMillis = selection
            }
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                timeZone = timeZoneUtc
            }
            onDataSelecionada(formato.format(calendar.time))
        }

        picker.show(activity.supportFragmentManager, "MATERIAL_DATE_PICKER")
    }

    fun exibirSeletorHora(
        activity: FragmentActivity,
        horaInicial: Int = 12,
        minutoInicial: Int = 0,
        onHoraSelecionada: (horaFormatada: String) -> Unit
    ) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(horaInicial)
            .setMinute(minutoInicial)
            .setTitleText("Selecionar horário")
            .build()

        picker.addOnPositiveButtonClickListener {
            val horaFormatada = String.format(Locale.getDefault(), "%02d:%02d", picker.hour, picker.minute)
            onHoraSelecionada(horaFormatada)
        }

        picker.show(activity.supportFragmentManager, "MATERIAL_TIME_PICKER")
    }

    fun exibirToastCustomizado(context: Context, mensagem: String, sucesso: Boolean = true) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.layout_custom_toast, null)

        val ivIcone = view.findViewById<ImageView>(R.id.ivIconeToast)
        val tvTexto = view.findViewById<TextView>(R.id.tvTextoToast)

        tvTexto.text = mensagem

        if (sucesso) {
            ivIcone.setImageResource(R.drawable.vetor_check)
            ivIcone.setColorFilter(Color.parseColor("#0D9488"))
        } else {
            ivIcone.setImageResource(R.drawable.vetor_lixeira)
            ivIcone.setColorFilter(Color.parseColor("#EF4444"))
        }

        val toast = Toast(context.applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            setView(view)
        }
        toast.show()
    }
}
