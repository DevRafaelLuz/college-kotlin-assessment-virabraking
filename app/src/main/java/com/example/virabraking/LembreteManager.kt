package com.example.virabraking

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object LembreteManager {

    private const val FORMATO_DATA = "yyyy-MM-dd"
    private const val FORMATO_DATA_HORA = "yyyy-MM-dd HH:mm"

    private const val HORA_PADRAO = "12:00"
    private const val PREFIXO_LEMBRETE = "Lembrete: "

    fun agendarNotificacao(
        context: Context,
        id: Int,
        titulo: String,
        mensagem: String,
        dataStr: String,
        horaStr: String? = null
    ) {
        val dataAgendada = converterDataParaMillis(dataStr, horaStr)
            ?: return

        if (dataAgendada <= System.currentTimeMillis()) {
            return
        }

        val alarmManager = obterAlarmManager(context)

        val pendingIntent = criarPendingIntent(
            context = context,
            id = id,
            titulo = titulo,
            mensagem = mensagem
        )

        agendarAlarme(
            alarmManager = alarmManager,
            horario = dataAgendada,
            pendingIntent = pendingIntent
        )
    }

    private fun converterDataParaMillis(data: String, hora: String?): Long? {
        val horaEfetiva = hora
            ?.takeIf { it.isNotBlank() }
            ?.trim()
            ?: HORA_PADRAO

        return try {
            val formato = SimpleDateFormat(
                FORMATO_DATA_HORA,
                Locale.getDefault()
            ).apply {
                isLenient = false
            }

            val dataHora = formato.parse("$data $horaEfetiva")
                ?: return null

            Calendar.getInstance().apply {
                time = dataHora
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

        } catch (e: Exception) {
            null
        }
    }

    private fun obterAlarmManager(context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private fun criarPendingIntent(context: Context, id: Int, titulo: String, mensagem: String): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.EXTRA_TITULO, titulo)
            putExtra(NotificationReceiver.EXTRA_MENSAGEM, mensagem)
            putExtra(NotificationReceiver.EXTRA_ID, id)
        }

        return PendingIntent.getBroadcast(
            context,
            id,
            intent,
            obterFlagsPendingIntent()
        )
    }

    private fun agendarAlarme(alarmManager: AlarmManager, horario: Long, pendingIntent: PendingIntent) {
        try {
            if (podeAgendarAlarmeExato(alarmManager)) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, horario, pendingIntent)
            } else {
                agendarAlarmeInexato(alarmManager, horario, pendingIntent)
            }
        } catch (e: SecurityException) {
            agendarAlarmeInexato(alarmManager, horario, pendingIntent)
        }
    }

    private fun podeAgendarAlarmeExato(alarmManager: AlarmManager): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    }

    private fun agendarAlarmeInexato(alarmManager: AlarmManager, horario: Long, pendingIntent: PendingIntent) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, horario, pendingIntent)
    }

    private fun criarPendingIntentParaCancelar(context: Context, id: Int): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java)

        return PendingIntent.getBroadcast(
            context,
            id,
            intent,
            obterFlagsPendingIntent()
        )
    }

    private fun obterFlagsPendingIntent(): Int {
        return PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }

    fun cancelarNotificacao(context: Context, id: Int) {
        val alarmManager = obterAlarmManager(context)

        val pendingIntent = criarPendingIntentParaCancelar(context, id)

        alarmManager.cancel(pendingIntent)
    }

    fun reagendarTodosLembretesFuturos(context: Context) {
        val banco = BancoHelper.getInstance(context)
        val lembretes = banco.listarLembretes()

        val dataHoje = obterDataAtual()

        lembretes
            .filter { lembrete -> lembrete.data >= dataHoje }
            .forEach { lembrete ->
                reagendarLembrete(context, lembrete)
            }
    }

    private fun obterDataAtual(): String {
        return SimpleDateFormat(FORMATO_DATA, Locale.US).format(Date())
    }

    private fun reagendarLembrete(context: Context, lembrete: Lembrete) {
        val mensagem = lembrete.descricao.ifBlank {
            "$PREFIXO_LEMBRETE${lembrete.titulo}"
        }

        agendarNotificacao(
            context = context,
            id = lembrete.id,
            titulo = lembrete.titulo,
            mensagem = mensagem,
            dataStr = lembrete.data,
            horaStr = lembrete.hora
        )
    }
}
