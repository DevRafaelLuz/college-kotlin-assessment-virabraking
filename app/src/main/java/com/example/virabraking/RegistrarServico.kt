package com.example.virabraking

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RegistrarServico : AppCompatActivity() {

    private lateinit var spTipoServico: Spinner
    private lateinit var spVeiculo: Spinner
    private lateinit var etData: EditText
    private lateinit var etQuilometragem: EditText
    private lateinit var etValor: EditText
    private lateinit var etOficina: EditText
    private lateinit var etObservacoes: EditText

    private lateinit var layoutComprovante: LinearLayout
    private lateinit var imagemComprovante: ImageView

    private var veiculos = listOf<Veiculo>()
    private var caminhoComprovanteSelecionado: String? = null

    private val selecionarComprovanteLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                processarComprovanteSelecionado(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_servico)

        inicializarViews()
        configurarTela()
    }

    private fun inicializarViews() {
        spTipoServico = findViewById(R.id.sTipoServico)
        spVeiculo = findViewById(R.id.spVeiculo)

        etData = findViewById(R.id.etData)
        etQuilometragem = findViewById(R.id.etQuilometragem)
        etValor = findViewById(R.id.etValor)
        etOficina = findViewById(R.id.etOficina)
        etObservacoes = findViewById(R.id.etObservacoes)

        layoutComprovante = findViewById(R.id.llComprovante)
        imagemComprovante = findViewById(R.id.ivComprovante)
    }

    private fun configurarTela() {
        configurarNavegacao()
        carregarVeiculos()
        configurarTiposServico()
        configurarSeletorData()
        configurarBotoes()
    }

    private fun configurarNavegacao() {
        configurarDestino(R.id.llInicio, Inicio::class.java)
        configurarDestino(R.id.llVeiculos, Veiculos::class.java)
        configurarDestino(R.id.llAlertas, Alertas::class.java)
        configurarDestino(R.id.llRelatorios, Relatorios::class.java)
    }

    private fun configurarDestino(idView: Int, destino: Class<out AppCompatActivity>) {
        findViewById<LinearLayout>(idView).setOnClickListener {
            abrirTela(destino)
        }
    }

    private fun abrirTela(destino: Class<out AppCompatActivity>) {
        startActivity(Intent(this, destino))
        overridePendingTransition(0, 0)
    }

    private fun carregarVeiculos() {
        veiculos = BancoHelper.getInstance(this).listarVeiculos()

        spVeiculo.adapter = criarAdapter(
            veiculos.map { it.apelido }
        )

        selecionarVeiculoInicial()
        configurarAtualizacaoQuilometragem()
    }

    private fun criarAdapter(itens: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(this, R.layout.item_spinner_tipo_servico, itens).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
    }

    private fun selecionarVeiculoInicial() {
        val idInicial = intent.getIntExtra(
            Constantes.EXTRA_VEICULO_ID,
            Constantes.ID_INVALIDO
        )

        val posicaoInicial = veiculos.indexOfFirst { veiculo ->
            veiculo.id == idInicial
        }

        if (posicaoInicial >= 0) {
            spVeiculo.setSelection(posicaoInicial)
        }
    }

    private fun configurarAtualizacaoQuilometragem() {
        if (veiculos.isEmpty()) return

        spVeiculo.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    atualizarQuilometragemVeiculoSelecionado(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

        atualizarQuilometragemVeiculoSelecionado(
            spVeiculo.selectedItemPosition
        )
    }

    private fun atualizarQuilometragemVeiculoSelecionado(posicao: Int) {
        val veiculo = veiculos.getOrNull(posicao) ?: return

        etQuilometragem.setText(veiculo.quilometragem.toString())
        etQuilometragem.error = null
    }

    private fun configurarTiposServico() {
        val opcoes = listOf("Selecione") + TipoServico.entries.map { it.descricao }

        spTipoServico.adapter = criarAdapter(opcoes)
    }

    private fun configurarSeletorData() {
        etData.setOnClickListener {
            exibirSeletorData()
        }
    }

    private fun exibirSeletorData() {
        DialogUtils.exibirSeletorData(
            activity = this,
            titulo = "Data da manutenção"
        ) { dataFormatada ->
            val partes = dataFormatada.split("-")
            if (partes.size == 3) {
                preencherDataSelecionada(partes[0].toInt(), partes[1].toInt() - 1, partes[2].toInt())
            }
        }
    }

    private fun preencherDataSelecionada(
        ano: Int,
        mes: Int,
        dia: Int
    ) {
        etData.setText(
            String.format(
                Locale.getDefault(),
                "%02d/%02d/%04d",
                dia,
                mes + 1,
                ano
            )
        )
    }

    private fun configurarBotoes() {
        findViewById<Button>(R.id.btnSalvarManutencao)
            .setOnClickListener {
                salvarServico()
            }

        findViewById<Button>(R.id.btnAbastecer)
            .setOnClickListener {
                abrirAbastecimento()
            }

        configurarSelecaoComprovante()
    }

    private fun salvarServico() {
        if (!validarVeiculos()) return

        val tipoServico = obterTipoServicoSelecionado()

        if (tipoServico == null) {
            exibirMensagem(MENSAGEM_TIPO_NAO_SELECIONADO)
            return
        }

        val data = converterDataParaFormatoBanco(
            etData.text.toString()
        )

        if (data == null) {
            exibirMensagem(MENSAGEM_DATA_INVALIDA)
            return
        }

        val servico = criarServico(tipoServico, data)
        val resultado = inserirServico(servico)

        if (resultado == RESULTADO_FALHA) {
            exibirMensagem(MENSAGEM_ERRO_SALVAMENTO)
            return
        }

        processarLembreteSeNecessario(servico, tipoServico)

        exibirMensagem(MENSAGEM_SERVICO_SALVO)
        finish()
    }

    private fun validarVeiculos(): Boolean {
        if (veiculos.isNotEmpty()) return true

        exibirMensagem(MENSAGEM_VEICULO_NAO_CADASTRADO)
        return false
    }

    private fun obterTipoServicoSelecionado(): TipoServico? {
        val posicao = spTipoServico.selectedItemPosition

        if (posicao <= 0) return null

        return TipoServico.entries.getOrNull(posicao - 1)
    }

    private fun converterDataParaFormatoBanco(dataTexto: String ): String? {
        val formatoEntrada = SimpleDateFormat(FORMATO_DATA_EXIBICAO, Locale.getDefault()).apply {
            isLenient = false
        }

        return try {
            val data = formatoEntrada.parse(dataTexto) ?: return null

            SimpleDateFormat(FORMATO_DATA_BANCO, Locale.getDefault()).format(data)
        } catch (e: ParseException) {
            null
        }
    }

    private fun criarServico(tipoServico: TipoServico, data: String): Servico {
        val veiculo = veiculos[spVeiculo.selectedItemPosition]

        val quilometragem = etQuilometragem.text.toString()
            .toIntOrNull()
            ?: veiculo.quilometragem

        val valor = etValor.text.toString()
            .replace(",", ".")
            .toDoubleOrNull()
            ?: 0.0

        return Servico(
            veiculoId = veiculo.id,
            tipo = tipoServico.descricao,
            data = data,
            quilometragem = quilometragem,
            valor = valor,
            oficina = etOficina.text.toString().trim(),
            observacoes = etObservacoes.text.toString().trim(),
            caminhoComprovante = caminhoComprovanteSelecionado
        )
    }

    private fun inserirServico(servico: Servico): Long {
        return BancoHelper.getInstance(this).inserirServico(servico)
    }

    private fun processarLembreteSeNecessario(servico: Servico, tipoServico: TipoServico) {
        if (!deveCriarLembrete(servico.data)) return

        criarLembrete(servico, tipoServico)
    }

    private fun deveCriarLembrete(data: String): Boolean {
        val dataHoje = SimpleDateFormat(FORMATO_DATA_BANCO, Locale.US).format(Date())

        return data >= dataHoje
    }

    private fun criarLembrete(servico: Servico, tipoServico: TipoServico) {
        val veiculo = veiculos.getOrNull(spVeiculo.selectedItemPosition)

        val apelidoVeiculo = veiculo?.apelido.orEmpty()

        val titulo = String.format(
            Locale.getDefault(),
            MENSAGEM_LEMBRETE_MANUTENCAO,
            tipoServico.descricao
        )

        val descricao = obterDescricaoLembrete(apelidoVeiculo)

        val lembrete = Lembrete(
            veiculoId = servico.veiculoId,
            titulo = titulo,
            descricao = descricao,
            data = servico.data,
            hora = HORA_LEMBRETE,
            veiculoApelido = apelidoVeiculo
        )

        val idLembrete = BancoHelper.getInstance(this)
            .inserirLembrete(lembrete)

        agendarNotificacao(
            id = idLembrete.toInt(),
            titulo = titulo,
            mensagem = descricao,
            data = servico.data
        )
    }

    private fun obterDescricaoLembrete(apelidoVeiculo: String): String {
        return if (apelidoVeiculo.isNotBlank()) {
            String.format(Locale.getDefault(), DESCRICAO_LEMBRETE, apelidoVeiculo)
        } else {
            DESCRICAO_LEMBRETE_SEM_VEICULO
        }
    }

    private fun agendarNotificacao(id: Int, titulo: String, mensagem: String, data: String) {
        LembreteManager.agendarNotificacao(
            context = this,
            id = id,
            titulo = titulo,
            mensagem = mensagem,
            dataStr = data,
            horaStr = HORA_LEMBRETE
        )
    }

    private fun exibirMensagem(mensagem: String) {
        val sucesso = !mensagem.contains("Erro", ignoreCase = true)
        DialogUtils.exibirToastCustomizado(this, mensagem, sucesso)
    }

    private fun abrirAbastecimento() {
        val intentAbastecimento = Intent(this, Abastecimento::class.java)

        veiculos.getOrNull(spVeiculo.selectedItemPosition)?.let { veiculo ->
            intentAbastecimento.putExtra(
                Constantes.EXTRA_VEICULO_ID,
                veiculo.id
            )
        }

        startActivity(intentAbastecimento)
        overridePendingTransition(0, 0)
    }

    private fun configurarSelecaoComprovante() {
        layoutComprovante.setOnClickListener {
            abrirSeletorComprovante()
        }

        imagemComprovante.setOnClickListener {
            abrirSeletorComprovante()
        }
    }

    private fun abrirSeletorComprovante() {
        selecionarComprovanteLauncher.launch(TIPO_IMAGEM)
    }

    private fun processarComprovanteSelecionado(uri: Uri) {
        caminhoComprovanteSelecionado =
            copiarImagemParaArmazenamentoInterno(uri)

        if (caminhoComprovanteSelecionado != null) {
            exibirComprovanteSelecionado()
            exibirMensagem(MENSAGEM_COMPROVANTE_ANEXADO)
        } else {
            exibirMensagem(MENSAGEM_ERRO_COMPROVANTE)
        }
    }

    private fun copiarImagemParaArmazenamentoInterno(uri: Uri): String? {
        return try {
            val pasta = obterPastaComprovantes()
            val arquivoDestino = criarArquivoComprovante(pasta)

            val fluxoEntrada = contentResolver.openInputStream(uri)
                ?: return null

            fluxoEntrada.use { input ->
                arquivoDestino.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            arquivoDestino.absolutePath

        } catch (e: Exception) {
            null
        }
    }

    private fun obterPastaComprovantes(): File {
        return File(filesDir, PASTA_COMPROVANTES).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    private fun criarArquivoComprovante(pasta: File): File {
        val nomeArquivo = buildString {
            append(PREFIXO_COMPROVANTE)
            append(System.currentTimeMillis())
            append(EXTENSAO_COMPROVANTE)
        }

        return File(pasta, nomeArquivo)
    }

    private fun exibirComprovanteSelecionado() {
        val caminho = caminhoComprovanteSelecionado ?: return

        layoutComprovante.visibility = View.GONE
        imagemComprovante.visibility = View.VISIBLE

        imagemComprovante.setImageURI(
            Uri.fromFile(File(caminho))
        )
    }

    private companion object {
        const val RESULTADO_FALHA = -1L

        private const val FORMATO_DATA_EXIBICAO = "dd/MM/yyyy"
        private const val FORMATO_DATA_BANCO = "yyyy-MM-dd"

        private const val HORA_LEMBRETE = "12:00"

        private const val PASTA_COMPROVANTES = "comprovantes"
        private const val PREFIXO_COMPROVANTE = "comp_"
        private const val EXTENSAO_COMPROVANTE = ".jpg"

        private const val TIPO_IMAGEM = "image/*"

        private const val MENSAGEM_COMPROVANTE_ANEXADO = "Comprovante anexado"

        private const val MENSAGEM_ERRO_COMPROVANTE = "Erro ao anexar comprovante"

        private const val MENSAGEM_VEICULO_NAO_CADASTRADO = "Cadastre um veículo primeiro"

        private const val MENSAGEM_TIPO_NAO_SELECIONADO = "Selecione o tipo de serviço"

        private const val MENSAGEM_DATA_INVALIDA = "Selecione uma data válida para o serviço"

        private const val MENSAGEM_ERRO_SALVAMENTO = "Erro ao salvar o serviço"

        private const val MENSAGEM_SERVICO_SALVO = "Serviço salvo"

        private const val MENSAGEM_LEMBRETE_MANUTENCAO = "Manutenção: %s"

        private const val DESCRICAO_LEMBRETE = "Lembrete de manutenção para %s"

        private const val DESCRICAO_LEMBRETE_SEM_VEICULO = "Lembrete de manutenção agendado para hoje"
    }
}