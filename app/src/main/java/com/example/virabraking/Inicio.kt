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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

        findViewById<TextView>(R.id.tvVerTudo).setOnClickListener {
            startActivity(Intent(this, Atividades::class.java))
            overridePendingTransition(0, 0)
        }

        findViewById<TextView>(R.id.tvSobre).setOnClickListener {
            mostrarSobre()
        }
    }

    private fun configurarDestino(idView: Int, destino: Class<out Activity>) {
        findViewById<View>(idView).setOnClickListener {
            startActivity(Intent(this, destino))
            overridePendingTransition(0, 0);
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
        atualizarProximaManutencao(banco)
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

    private fun atualizarProximaManutencao(banco: BancoHelper) {
        val formatoBanco = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatoExibicao = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dataHojeStr = formatoBanco.format(Date())

        val proxima = banco.buscarProximaManutencao(dataHojeStr)

        val tvDescricao = findViewById<TextView>(R.id.tvDescricaoManutencao)
        val tvQuando = findViewById<TextView>(R.id.tvQuandoManutencao)
        val container = findViewById<View>(R.id.llProximaManutencao)

        if (proxima != null) {
            tvDescricao.text = if (proxima.veiculoApelido.isNotBlank()) {
                "${proxima.tipo} (${proxima.veiculoApelido})"
            } else {
                proxima.tipo
            }

            val dataHoje = formatoBanco.parse(dataHojeStr)
            val dataManutencao = formatoBanco.parse(proxima.data)

            if (dataHoje != null && dataManutencao != null) {
                val calHoje = Calendar.getInstance().apply {
                    time = dataHoje
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val calManut = Calendar.getInstance().apply {
                    time = dataManutencao
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val diffDias = ((calManut.timeInMillis - calHoje.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                val dataFormatada = formatoExibicao.format(dataManutencao)

                tvQuando.text = when {
                    diffDias == 0 -> "Hoje ($dataFormatada)"
                    diffDias == 1 -> "Amanhã ($dataFormatada)"
                    diffDias > 1 -> "Em $diffDias dias ($dataFormatada)"
                    else -> dataFormatada
                }
            } else {
                tvQuando.text = proxima.data.dataParaExibicao()
            }

            container.setOnClickListener {
                val intent = Intent(this, DetalheRegistro::class.java).apply {
                    putExtra(Constantes.EXTRA_SERVICO_ID, proxima.servicoId)
                    putExtra(Constantes.EXTRA_VEICULO_ID, proxima.veiculoId)
                }
                startActivity(intent)
            }
        } else {
            tvDescricao.text = "Nenhuma pendente"
            tvQuando.text = "Tudo em dia"

            container.setOnClickListener {
                startActivity(Intent(this, RegistrarServico::class.java))
            }
        }
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
            val atividadeView = atividade.paraView(layoutInflater, container)

            if (atividade.origem == "servico") {
                atividadeView.setOnClickListener {
                    abrirDetalhesServico(atividade)
                }
            }

            container.addView(atividadeView)
        }
    }

    private fun exibirMensagemListaVazia(container: LinearLayout) {
        val mensagem = TextView(this).apply {
            text = MENSAGEM_LISTA_VAZIA
            setTextColor(COR_TEXTO_SECUNDARIO)
        }

        container.addView(mensagem)
    }

    private fun abrirDetalhesServico(atividade: Atividade) {
        val intent = Intent(this, DetalheRegistro::class.java).apply {
            putExtra(Constantes.EXTRA_SERVICO_ID, atividade.id)
            putExtra(Constantes.EXTRA_VEICULO_ID, atividade.veiculoId)
        }
        startActivity(intent)
    }

    private companion object {
        const val NOME_ARQUIVO_FOTO = "foto_perfil.jpg"
        const val LIMITE_ATIVIDADES = 3
        const val MENSAGEM_LISTA_VAZIA = "Nenhuma atividade registrada ainda"
        const val COR_TEXTO_SECUNDARIO = 0xFF64748B.toInt()
    }
}