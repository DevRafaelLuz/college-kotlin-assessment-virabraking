package com.example.virabraking

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class Inicio : AppCompatActivity() {

    private lateinit var imagemPerfil: ImageView

    private val selecionarFoto = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let(::salvarFotoPerfil)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        inicializarComponente()
        configurarNavegacao()
        configurarFotoPerfil()
    }

    override fun onResume() {
        super.onResume()
        carregarResumo()
    }

    private fun inicializarComponente() {
        imagemPerfil = findViewById(R.id.ivPerfil)
    }

    private fun configurarNavegacao() {
        configurarDestino(R.id.llVeiculos, Veiculos::class.java)
        configurarDestino(R.id.llAlertas, Alertas::class.java)
        configurarDestino(R.id.llRelatorios, Relatorios::class.java)
        configurarDestino(R.id.llAdicionar, RegistrarServico::class.java)

        findViewById<TextView>(R.id.tvSobre).setOnClickListener {
            mostrarSobre()
        }
    }

    private fun configurarDestino(idView: Int, destino: Class<out Activity>) {
        findViewById<View>(idView).setOnClickListener {
            startActivity(Intent(this, destino))
        }
    }

    private fun configurarFotoPerfil() {
        carregarFotoPerfil()

        imagemPerfil.setOnClickListener {
            selecionarFoto.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
    }

    private fun carregarFotoPerfil() {
        val arquivo = File(filesDir, NOME_ARQUIVO_FOTO)

        if (arquivo.isFile) {
            imagemPerfil.setImageURI(Uri.fromFile(arquivo))
        }
    }

    private fun salvarFotoPerfil(uri: Uri) {
        imagemPerfil.setImageURI(uri)

        try {
            val arquivo = File(filesDir, NOME_ARQUIVO_FOTO)

            val entrada = contentResolver.openInputStream(uri)
                ?: run {
                    mostrarMensagem("Não foi possível abrir a imagem.")
                    return
                }

            entrada.use { fluxoEntrada ->
                FileOutputStream(arquivo).use { fluxoSaida ->
                    fluxoEntrada.copyTo(fluxoSaida)
                }
            }

            imagemPerfil.setImageURI(Uri.fromFile(arquivo))

            mostrarMensagem("Foto de perfil atualizada!")

        } catch (e: Exception) {
            mostrarMensagem("Não foi possível salvar a foto.")
        }
    }

    private fun mostrarMensagem(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun carregarResumo() {
        val banco = BancoHelper.getInstance(this)

        atualizarQuantidadeVeiculos(banco)
        atualizarGastosMensais(banco)
        atualizarListaAtividades(banco)
    }

    private fun atualizarQuantidadeVeiculos(banco: BancoHelper) {
        val quantidade = banco.contarVeiculos()

        findViewById<TextView>(R.id.tvQtdVeiculos).text = "$quantidade Ativos"
    }

    private fun atualizarGastosMensais(banco: BancoHelper) {
        val inicioMes = inicioDoMes()

        val totalGastos = banco.totalServicosDesde(inicioMes) + banco.totalCombustivelDesde(inicioMes)

        findViewById<TextView>(R.id.tvGastosMes).text = totalGastos.emReais()
    }

    private fun atualizarListaAtividades(banco: BancoHelper) {
        val container = findViewById<LinearLayout>(R.id.llListaAtividades)

        container.removeAllViews()

        val atividades = banco.listarAtividades(limite = LIMITE_ATIVIDADES)

        if (atividades.isEmpty()) {
            exibirMensagemListaVazia(container)
            return
        }

        atividades.forEach { atividade ->
            container.addView(atividade.paraView(layoutInflater, container))
        }
    }

    private fun exibirMensagemListaVazia(container: LinearLayout) {
        val mensagem = TextView(this).apply {
            text = MENSAGEM_LISTA_VAZIA
            setTextColor(COR_TEXTO_SECUNDARIO)
        }

        container.addView(mensagem)
    }

    private companion object {
        const val NOME_ARQUIVO_FOTO = "foto_perfil.jpg"
        const val LIMITE_ATIVIDADES = 3
        const val MENSAGEM_LISTA_VAZIA = "Nenhuma atividade registrada ainda"
        const val COR_TEXTO_SECUNDARIO = 0xFF64748B.toInt()
    }
}