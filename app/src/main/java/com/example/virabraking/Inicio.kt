package com.example.virabraking

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

    private lateinit var ivPerfil: ImageView

    private val nomeArquivo = "foto_perfil.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        val llVeiculos = findViewById<LinearLayout>(R.id.llVeiculos)
        llVeiculos.setOnClickListener {
            startActivity(Intent(this, Veiculos::class.java))
        }

        val llAlertas = findViewById<LinearLayout>(R.id.llAlertas)
        llAlertas.setOnClickListener {
            startActivity(Intent(this, Alertas::class.java))
        }

        val llRelatorios = findViewById<LinearLayout>(R.id.llRelatorios)
        llRelatorios.setOnClickListener {
            startActivity(Intent(this, Relatorios::class.java))
        }

        val llAdicionar = findViewById<LinearLayout>(R.id.llAdicionar)
        llAdicionar.setOnClickListener {
            startActivity(Intent(this, RegistrarServico::class.java))
        }

        ivPerfil = findViewById(R.id.ivPerfil)

        carregarFotoSalva()

        ivPerfil.setOnClickListener {
            selecionarFoto.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
    }

    private val selecionarFoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        uri: Uri? ->
        if (uri != null) {
            salvarFoto(uri)
        }
    }

    private fun salvarFoto(uri: Uri) {
        try {
            val arquivo = File(filesDir, nomeArquivo)

            contentResolver.openInputStream(uri).use { entrada ->
                if (entrada == null) {
                    Toast.makeText(this, "Não foi possível abrir a imagem.", Toast.LENGTH_SHORT).show()
                    return
                }

                FileOutputStream(arquivo).use { saida ->
                    entrada.copyTo(saida)
                }
            }

            ivPerfil.setImageURI(Uri.fromFile(arquivo))

            Toast.makeText(this, "Foto de perfil salva!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar a foto: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun carregarFotoSalva() {
        val arquivo = File(filesDir, nomeArquivo)

        if (arquivo.exists()) {
            ivPerfil.setImageURI(Uri.fromFile(arquivo))
        }
    }

    override fun onResume() {
        super.onResume()
        carregarDados()
    }

    private fun carregarDados() {
        val banco = BancoHelper(this)

        findViewById<TextView>(R.id.tvQtdVeiculos).text = "${banco.contarVeiculos()} Ativos"

        val inicioMes = inicioDoMes()
        val gastoMes = banco.totalServicosDesde(inicioMes) + banco.totalCombustivelDesde(inicioMes)
        findViewById<TextView>(R.id.tvGastosMes).text = gastoMes.emReais()

        val container = findViewById<LinearLayout>(R.id.llListaAtividades)
        container.removeAllViews()

        val atividades = banco.listarAtividades(limite = 3)
        if (atividades.isEmpty()) {
            val vazio = TextView(this)
            vazio.text = "Nenhuma atividade registrada ainda"
            vazio.setTextColor(0xFF64748B.toInt())
            container.addView(vazio)
        } else {
            atividades.forEach { container.addView(it.paraView(layoutInflater, container)) }
        }
    }
}