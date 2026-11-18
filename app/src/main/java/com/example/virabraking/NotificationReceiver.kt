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

    override fun onReceive(context: Context, intent: Intent) {
        val dadosNotificacao = obterDadosNotificacao(intent)

        if (!possuiPermissaoNotificacao(context)) {
            return
        }

        mostrarNotificacao(context = context, dados = dadosNotificacao)
    }

    private fun obterDadosNotificacao(intent: Intent): DadosNotificacao {
        return DadosNotificacao(
            id = intent.getIntExtra(EXTRA_ID, System.currentTimeMillis().toInt()),
            titulo = intent.getStringExtra(EXTRA_TITULO) ?: TITULO_PADRAO,
            mensagem = intent.getStringExtra(EXTRA_MENSAGEM) ?: MENSAGEM_PADRAO
        )
    }

    private fun possuiPermissaoNotificacao(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun mostrarNotificacao(context: Context, dados: DadosNotificacao) {
        val gerenciadorNotificacoes = obterGerenciadorNotificacoes(context)

        criarCanalNotificacao(gerenciadorNotificacoes)

        val notificacao = criarNotificacao(context, dados)

        publicarNotificacao(gerenciadorNotificacoes, dados.id, notificacao)
    }

    private fun obterGerenciadorNotificacoes(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun criarCanalNotificacao(gerenciador: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val canal = NotificationChannel(CANAL_ID, CANAL_NOME,NotificationManager.IMPORTANCE_HIGH).apply {
            description = DESCRICAO_CANAL
        }

        gerenciador.createNotificationChannel(canal)
    }

    private fun criarNotificacao(context: Context, dados: DadosNotificacao) = NotificationCompat.Builder(context, CANAL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(dados.titulo)
        .setContentText(dados.mensagem)
        .setStyle(NotificationCompat.BigTextStyle().bigText(dados.mensagem))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(criarPendingIntent(context, dados.id))
        .build()

    private fun criarPendingIntent(context: Context, id: Int): PendingIntent {
        val intentDestino = Intent(context, Alertas::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val flagsPendingIntent = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return PendingIntent.getActivity(
            context,
            id,
            intentDestino,
            flagsPendingIntent
        )
    }

    private fun publicarNotificacao(gerenciador: NotificationManager, id: Int, notificacao: android.app.Notification) {
        try {
            gerenciador.notify(id, notificacao)
        } catch (e: SecurityException) {
            // A permissão pode ter sido revogada antes da publicação.
        }
    }

    private data class DadosNotificacao(
        val id: Int,
        val titulo: String,
        val mensagem: String
    )

    companion object {
        const val CANAL_ID = "canal_lembretes_virabraking"
        const val CANAL_NOME = "Lembretes e Manutenções"

        const val EXTRA_TITULO = "extra_titulo"
        const val EXTRA_MENSAGEM = "extra_mensagem"
        const val EXTRA_ID = "extra_id"

        private const val TITULO_PADRAO = "Lembrete de Manutenção"

        private const val MENSAGEM_PADRAO = "Você tem um lembrete agendado para hoje!"

        private const val DESCRICAO_CANAL = "Notificações de lembretes e revisões de veículos"
    }
}