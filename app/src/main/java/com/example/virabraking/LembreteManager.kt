package com.example.virabraking

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object LembreteManager {

    fun agendarNotificacao(
        context: Context,
        id: Int,
        titulo: String,
        mensagem: String,
        dataStr: String,
        horaStr: String? = null
    ) {
        val horaEfetiva = if (horaStr.isNullOrBlank()) "12:00" else horaStr.trim()

        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dataHoraTexto = "$dataStr $horaEfetiva"

        val dataAgendada = try {
            formato.parse(dataHoraTexto)
        } catch (_: Exception) {
            null
        } ?: return

        val calAgendado = Calendar.getInstance().apply {
            time = dataAgendada
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calAgendado.timeInMillis <= System.currentTimeMillis()) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.EXTRA_TITULO, titulo)
            putExtra(NotificationReceiver.EXTRA_MENSAGEM, mensagem)
            putExtra(NotificationReceiver.EXTRA_ID, id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calAgendado.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calAgendado.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calAgendado.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelarNotificacao(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun reagendarTodosLembretesFuturos(context: Context) {
        val banco = BancoHelper.getInstance(context)
        val lembretes = banco.listarLembretes()
        val formatoBanco = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dataHojeStr = formatoBanco.format(java.util.Date())

        lembretes.filter { it.data >= dataHojeStr }.forEach { lembrete ->
            val msgNotif = lembrete.descricao.ifBlank { "Lembrete: ${lembrete.titulo}" }
            agendarNotificacao(
                context = context,
                id = lembrete.id,
                titulo = lembrete.titulo,
                mensagem = msgNotif,
                dataStr = lembrete.data,
                horaStr = lembrete.hora
            )
        }
    }
}
