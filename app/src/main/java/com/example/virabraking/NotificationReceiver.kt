package com.example.virabraking

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CANAL_ID = "canal_lembretes_virabraking"
        const val CANAL_NOME = "Lembretes e Manutenções"
        const val EXTRA_TITULO = "extra_titulo"
        const val EXTRA_MENSAGEM = "extra_mensagem"
        const val EXTRA_ID = "extra_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra(EXTRA_TITULO) ?: "Lembrete de Manutenção"
        val mensagem = intent.getStringExtra(EXTRA_MENSAGEM) ?: "Você tem um lembrete agendado para hoje!"
        val id = intent.getIntExtra(EXTRA_ID, System.currentTimeMillis().toInt())

        mostrarNotificacao(context, id, titulo, mensagem)
    }

    private fun mostrarNotificacao(context: Context, id: Int, titulo: String, mensagem: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val temPermissao = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!temPermissao) return
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                CANAL_NOME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de lembretes e revisões de veículos"
            }
            notificationManager.createNotificationChannel(canal)
        }

        val intentDestino = Intent(context, Alertas::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intentDestino,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacao = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(id, notificacao)
        } catch (_: SecurityException) {}
    }
}
