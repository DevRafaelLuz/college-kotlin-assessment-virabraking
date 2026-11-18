package com.example.virabraking

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
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
        configurarTela()
    }

    override fun onResume() {
        super.onResume()
        carregarResumo()
    }

    private fun inicializarComponente() {
        imagemPerfil = findViewById(R.id.ivPerfil)
    }

    private fun configurarTela() {
        configurarNavegacao()
        configurarFotoPerfil()
    }

    private fun configurarNavegacao() {
        configurarDestino(R.id.llVeiculos, Veiculos::class.java)
        configurarDestino(R.id.llAlertas, Alertas::class.java)
        configurarDestino(R.id.llRelatorios, Relatorios::class.java)
        configurarDestino(R.id.llAdicionar, RegistrarServico::class.java)

        findViewById<TextView>(R.id.tvVerTudo).setOnClickListener {
            navegarPara(Atividades::class.java, aplicarTransicao = true)
        }

        findViewById<TextView>(R.id.tvSobre).setOnClickListener {
            mostrarSobre()
        }
    }

    private fun configurarDestino(idView: Int, destino: Class<out Activity>) {
        findViewById<View>(idView).setOnClickListener {
            navegarPara(destino, aplicarTransicao = true)
        }
    }

    private fun configurarFotoPerfil() {
        carregarFotoPerfil()

        imagemPerfil.setOnClickListener {
            abrirSeletorDeImagem()
        }
    }

    private fun carregarFotoPerfil() {
        val arquivo = obterArquivoFotoPerfil()

        if (arquivo.isFile) {
            atualizarImagemPerfil()
        }
    }

    private fun obterArquivoFotoPerfil(): File {
        return File(filesDir, NOME_ARQUIVO_FOTO)
    }

    private fun abrirSeletorDeImagem() {
        selecionarFoto.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
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

        findViewById<TextView>(R.id.tvQtdVeiculos).text = "$quantidade $TEXTO_VEICULOS_ATIVOS"
    }

    private fun atualizarGastosMensais(banco: BancoHelper) {
        val inicioMes = inicioDoMes()

        val totalServicos = banco.totalServicosDesde(inicioMes)
        val totalCombustivel = banco.totalCombustivelDesde(inicioMes)

        val totalGastos = totalServicos + totalCombustivel

        findViewById<TextView>(R.id.tvGastosMes).text = totalGastos.emReais()
    }

    private fun atualizarProximaManutencao(banco: BancoHelper) {
        val dataHoje = dataHojeParaBanco()
        val proxima = banco.buscarProximaManutencao(dataHoje)

        val tvDescricao = findViewById<TextView>(R.id.tvDescricaoManutencao)
        val tvQuando = findViewById<TextView>(R.id.tvQuandoManutencao)
        val container = findViewById<View>(R.id.llProximaManutencao)

        if (proxima == null) {
            exibirManutencaoVazia(tvDescricao, tvQuando, container)
            return
        }

        exibirDadosManutencao(proxima, tvDescricao, tvQuando)
        configurarCliqueManutencao(container, proxima)
    }

    private fun exibirManutencaoVazia(tvDescricao: TextView, tvQuando: TextView, container: View) {
        tvDescricao.text = TEXTO_NENHUMA_MANUTENCAO
        tvQuando.text = TEXTO_TUDO_EM_DIA

        container.setOnClickListener {
            navegarPara(RegistrarServico::class.java)
        }
    }

    private fun navegarPara(destino: Class<out Activity>, aplicarTransicao: Boolean = false) {
        val intent = Intent(this, destino)
        iniciarActivity(intent, aplicarTransicao)
    }

    private fun exibirDadosManutencao(manutencao: ManutencaoAgendada, tvDescricao: TextView, tvQuando: TextView) {
        tvDescricao.text = formatarDescricaoManutencao(manutencao)
        tvQuando.text = formatarPrazoManutencao(manutencao.data)
    }

    private fun formatarDescricaoManutencao(manutencao: ManutencaoAgendada): String {
        return if (manutencao.veiculoApelido!!.isNotBlank()) {
            "${manutencao.tipo} (${manutencao.veiculoApelido})"
        } else {
            manutencao.tipo
        }
    }

    private fun formatarPrazoManutencao(data: String): String {
        val dataHoje = converterData(dataHojeParaBanco())
        val dataManutencao = converterData(data)

        if (dataHoje == null || dataManutencao == null) {
            return data.dataParaExibicao()
        }

        val diferencaDias = calcularDiferencaEmDias(dataHoje, dataManutencao)

        val dataFormatada = formatarDataParaExibicao(dataManutencao)

        return when (diferencaDias) {
            0 -> "$TEXTO_HOJE ($dataFormatada)"
            1 -> "$TEXTO_AMANHA ($dataFormatada)"
            else -> {
                if (diferencaDias > 1) {
                    "Em $diferencaDias dias ($dataFormatada)"
                } else {
                    dataFormatada
                }
            }
        }
    }

    private fun converterData(data: String): Date? {
        return try {
            SimpleDateFormat(FORMATO_DATA_BANCO, Locale.US).apply {
                isLenient = false
            }.parse(data)
        } catch (e: Exception) {
            null
        }
    }

    private fun dataHojeParaBanco(): String {
        return SimpleDateFormat(FORMATO_DATA_BANCO, Locale.US).format(Date())
    }

    private fun calcularDiferencaEmDias(dataInicial: Date, dataFinal: Date): Int {
        val calendarioInicial = obterDataSemHorario(dataInicial)
        val calendarioFinal = obterDataSemHorario(dataFinal)

        val diferencaMillis = calendarioFinal.timeInMillis - calendarioInicial.timeInMillis

        return (diferencaMillis / MILISSEGUNDOS_POR_DIA).toInt()
    }

    private fun obterDataSemHorario(data: Date): Calendar {
        return Calendar.getInstance().apply {
            time = data
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun formatarDataParaExibicao(data: Date): String {
        return SimpleDateFormat(
            FORMATO_DATA_EXIBICAO,
            Locale.getDefault()
        ).format(data)
    }

    private fun configurarCliqueManutencao(container: View, manutencao: ManutencaoAgendada) {
        container.setOnClickListener {
            abrirDetalhesManutencao(manutencao)
        }
    }

    private fun abrirDetalhesManutencao(manutencao: ManutencaoAgendada) {
        val intent = Intent(this, DetalhesRegistro::class.java).apply {
            putExtra(Constantes.EXTRA_SERVICO_ID, manutencao.servicoId)
            putExtra(Constantes.EXTRA_VEICULO_ID, manutencao.veiculoId)
        }

        iniciarActivity(intent)
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
            adicionarAtividadeNaLista(atividade, container)
        }
    }

    private fun exibirMensagemListaVazia(container: LinearLayout) {
        val mensagem = TextView(this).apply {
            text = MENSAGEM_LISTA_VAZIA
            setTextColor(COR_TEXTO_SECUNDARIO)
        }

        container.addView(mensagem)
    }

    private fun adicionarAtividadeNaLista(atividade: Atividade, container: LinearLayout) {
        val atividadeView = atividade.paraView(layoutInflater, container)

        if (atividade.origem == ORIGEM_SERVICO) {
            atividadeView.setOnClickListener {
                abrirDetalhesServico(atividade)
            }
        }

        container.addView(atividadeView)
    }

    private fun abrirDetalhesServico(atividade: Atividade) {
        val intent = Intent(this, DetalhesRegistro::class.java).apply {
            putExtra(Constantes.EXTRA_SERVICO_ID, atividade.id)
            putExtra(Constantes.EXTRA_VEICULO_ID, atividade.veiculoId)
        }

        iniciarActivity(intent)
    }

    private fun iniciarActivity(intent: Intent, aplicarTransicao: Boolean = false) {
        startActivity(intent)

        if (aplicarTransicao) {
            overridePendingTransition(TRANSICAO_SEM_ANIMACAO, TRANSICAO_SEM_ANIMACAO)
        }
    }

    private fun salvarFotoPerfil(uri: Uri) {
        try {
            copiarImagemParaArquivo(uri)
            atualizarImagemPerfil()

            mostrarMensagem(MENSAGEM_FOTO_ATUALIZADA)

        } catch (e: Exception) {
            mostrarMensagem(MENSAGEM_ERRO_SALVAR_FOTO)
        }
    }

    private fun copiarImagemParaArquivo(uri: Uri) {
        val entrada = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException(MENSAGEM_ERRO_ABRIR_IMAGEM)

        entrada.use { fluxoEntrada ->
            FileOutputStream(obterArquivoFotoPerfil()).use { fluxoSaida ->
                fluxoEntrada.copyTo(fluxoSaida)
            }
        }
    }

    private fun atualizarImagemPerfil() {
        val arquivo = obterArquivoFotoPerfil()

        val bitmap = BitmapFactory.decodeFile(arquivo.absolutePath)
            ?: throw IllegalStateException(MENSAGEM_ERRO_SALVAR_FOTO)

        imagemPerfil.setImageBitmap(bitmap)
    }

    private fun mostrarMensagem(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        private const val NOME_ARQUIVO_FOTO = "foto_perfil.jpg"

        private const val LIMITE_ATIVIDADES = 3

        private const val FORMATO_DATA_BANCO = "yyyy-MM-dd"
        private const val FORMATO_DATA_EXIBICAO = "dd/MM/yyyy"

        private const val MILISSEGUNDOS_POR_DIA = 86_400_000L

        private const val TRANSICAO_SEM_ANIMACAO = 0

        private const val TEXTO_VEICULOS_ATIVOS = "Ativos"
        private const val TEXTO_HOJE = "Hoje"
        private const val TEXTO_AMANHA = "Amanhã"
        private const val TEXTO_NENHUMA_MANUTENCAO = "Nenhuma pendente"
        private const val TEXTO_TUDO_EM_DIA = "Tudo em dia"

        private const val MENSAGEM_LISTA_VAZIA = "Nenhuma atividade registrada ainda"

        private const val MENSAGEM_FOTO_ATUALIZADA = "Foto de perfil atualizada!"

        private const val MENSAGEM_ERRO_ABRIR_IMAGEM = "Não foi possível abrir a imagem."

        private const val MENSAGEM_ERRO_SALVAR_FOTO = "Não foi possível salvar a foto."

        private const val ORIGEM_SERVICO = "servico"

        private const val COR_TEXTO_SECUNDARIO = 0xFF64748B.toInt()
    }
}