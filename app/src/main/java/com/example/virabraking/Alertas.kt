package com.example.virabraking

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Alertas : AppCompatActivity() {

    private lateinit var listaLembretes: LinearLayout
    private lateinit var mensagemSemLembretes: TextView

    private val solicitadorPermissaoNotificacao = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alertas)

        inicializarViews()
        configurarNavegacao()
        verificarPermissaoNotificacao()
    }

    override fun onResume() {
        super.onResume()

        LembreteManager.reagendarTodosLembretesFuturos(this)
        carregarLembretes()
    }

    private fun inicializarViews() {
        listaLembretes = findViewById(R.id.llListaLembretes)
        mensagemSemLembretes = findViewById(R.id.tvSemLembretes)

        configurarBotaoAdicionar()
    }

    private fun configurarBotaoAdicionar() {
        findViewById<ImageButton>(R.id.iBtnAdicionarLembrete).setOnClickListener {
            exibirDialogNovoLembrete()
        }
    }

    private fun exibirDialogNovoLembrete() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_criar_lembrete, null)

        val campos = inicializarCamposDialog(dialogView)
        val banco = BancoHelper.getInstance(this)
        val veiculos = banco.listarVeiculos()

        configurarSpinnerVeiculos(campos.spinnerVeiculo, veiculos)

        val calendario = Calendar.getInstance()
        val horaSelecionada = criarCampoHora(campos.campoHora)

        configurarSeletorData(campos.campoData, calendario)
        configurarSeletorHora(campos.campoHora)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(BOTAO_SALVAR) { _, _ ->
                salvarNovoLembrete(
                    campos,
                    veiculos,
                    banco,
                    calendario,
                    horaSelecionada
                )
            }
            .setNegativeButton(BOTAO_CANCELAR, null)
            .show()
    }

    private data class CamposLembrete(
        val campoTitulo: EditText,
        val campoDescricao: EditText,
        val spinnerVeiculo: Spinner,
        val campoData: EditText,
        val campoHora: EditText
    )

    private fun inicializarCamposDialog(view: View): CamposLembrete {
        return CamposLembrete(
            campoTitulo = view.findViewById(R.id.etTituloLembrete),
            campoDescricao = view.findViewById(R.id.etDescricaoLembrete),
            spinnerVeiculo = view.findViewById(R.id.spVeiculoLembrete),
            campoData = view.findViewById(R.id.etDataLembrete),
            campoHora = view.findViewById(R.id.etHoraLembrete)
        )
    }

    private fun configurarSpinnerVeiculos(spinner: Spinner, veiculos: List<Veiculo>) {
        val opcoes = listOf(OPCAO_SEM_VEICULO) + veiculos.map { it.apelido }

        val adapter = ArrayAdapter(
            this,
            R.layout.item_spinner_tipo_servico,
            opcoes
        ).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }

        spinner.adapter = adapter
    }

    private fun criarCampoHora(campoHora: EditText): String {
        campoHora.setText(HORA_PADRAO)
        return HORA_PADRAO
    }

    private fun configurarSeletorData(campoData: EditText, calendario: Calendar) {
        campoData.setText(formatarDataExibicao(calendario))

        campoData.setOnClickListener {
            exibirSeletorData(campoData, calendario)
        }
    }

    private fun formatarDataExibicao(calendario: Calendar): String {
        return SimpleDateFormat(
            FORMATO_DATA_EXIBICAO,
            Locale.getDefault()
        ).format(calendario.time)
    }

    private fun exibirSeletorData(campoData: EditText, calendario: Calendar) {
        DatePickerDialog(
            this,
            { _, ano, mes, dia ->
                calendario.set(ano, mes, dia)
                campoData.setText(formatarDataExibicao(calendario))
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun configurarSeletorHora(campoHora: EditText) {
        campoHora.setOnClickListener {
            exibirSeletorHora(campoHora)
        }
    }

    private fun exibirSeletorHora(campoHora: EditText) {
        val partesHora = campoHora.text.toString().split(":")
        val horaAtual = partesHora.getOrNull(0)?.toIntOrNull() ?: 12
        val minutoAtual = partesHora.getOrNull(1)?.toIntOrNull() ?: 0

        TimePickerDialog(
            this,
            { _, hora, minuto ->
                val novaHora = String.format(
                    Locale.US,
                    "%02d:%02d",
                    hora,
                    minuto
                )

                campoHora.setText(novaHora)
            },
            horaAtual,
            minutoAtual,
            true
        ).show()
    }

    private fun salvarNovoLembrete(
        campos: CamposLembrete,
        veiculos: List<Veiculo>,
        banco: BancoHelper,
        calendario: Calendar,
        horaSelecionada: String
    ) {
        val titulo = campos.campoTitulo.text.toString().trim()

        if (!validarTitulo(titulo)) {
            exibirMensagem(TEXTO_TITULO_OBRIGATORIO)
            return
        }

        val lembrete = criarLembrete(
            campos,
            veiculos,
            calendario,
            titulo
        )

        val idSalvo = banco.inserirLembrete(lembrete)

        if (idSalvo == ID_INVALIDO) {
            exibirMensagem(TEXTO_ERRO_CRIAR)
            return
        }

        agendarNotificacao(
            id = idSalvo,
            titulo = titulo,
            descricao = lembrete.descricao,
            data = lembrete.data,
            hora = horaSelecionada
        )

        exibirMensagem(TEXTO_SUCESSO_CRIAR)
        carregarLembretes()
    }

    private fun validarTitulo(titulo: String): Boolean {
        return titulo.isNotBlank()
    }

    private fun criarLembrete(
        campos: CamposLembrete,
        veiculos: List<Veiculo>,
        calendario: Calendar,
        titulo: String
    ): Lembrete {
        val descricao = campos.campoDescricao.text.toString().trim()
        val veiculoId = obterVeiculoIdSelecionado(campos.spinnerVeiculo, veiculos)

        return Lembrete(
            veiculoId = veiculoId,
            titulo = titulo,
            descricao = descricao,
            data = formatarDataBanco(calendario),
            hora = campos.campoHora.text.toString()
        )
    }


    private fun obterVeiculoIdSelecionado(spinner: Spinner, veiculos: List<Veiculo>): Int? {
        val posicaoSelecionada = spinner.selectedItemPosition

        if (posicaoSelecionada <= 0) {
            return null
        }

        return veiculos.getOrNull(posicaoSelecionada - 1)?.id
    }

    private fun formatarDataBanco(calendario: Calendar): String {
        return SimpleDateFormat(FORMATO_DATA_BANCO, Locale.US).format(calendario.time)
    }

    private fun agendarNotificacao(
        id: Long,
        titulo: String,
        descricao: String,
        data: String,
        hora: String
    ) {
        val mensagem = descricao.ifBlank {
            "Lembrete: $titulo"
        }

        LembreteManager.agendarNotificacao(
            context = this,
            id = id.toInt(),
            titulo = titulo,
            mensagem = mensagem,
            dataStr = data,
            horaStr = hora
        )
    }

    private fun exibirMensagem(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun exibirMensagemSemLembretes() {
        mensagemSemLembretes.visibility = View.VISIBLE
    }

    private fun ocultarMensagemSemLembretes() {
        mensagemSemLembretes.visibility = View.GONE
    }

    private fun adicionarItemLembrete(lembrete: Lembrete) {
        val itemView = layoutInflater.inflate(
            R.layout.item_lembrete,
            listaLembretes,
            false
        )

        configurarDadosLembrete(itemView, lembrete)
        configurarBotaoExcluir(itemView, lembrete)

        listaLembretes.addView(itemView)
    }

    private fun configurarDadosLembrete(itemView: View, lembrete: Lembrete) {
        val titulo = itemView.findViewById<TextView>(R.id.tvLembreteTitulo)
        val dataHora = itemView.findViewById<TextView>(R.id.tvLembreteDataHora)
        val descricao = itemView.findViewById<TextView>(R.id.tvLembreteDescricao)

        titulo.text = formatarTituloLembrete(lembrete)
        dataHora.text = formatarDataHoraLembrete(lembrete)

        configurarDescricao(descricao, lembrete.descricao)
    }

    private fun formatarTituloLembrete(lembrete: Lembrete): String {
        val complementoVeiculo = lembrete.veiculoApelido
            ?.takeIf { it.isNotBlank() }
            ?.let { " • $it" }
            ?: ""

        return "${lembrete.titulo}$complementoVeiculo"
    }

    private fun formatarDataHoraLembrete(lembrete: Lembrete): String {
        return "${lembrete.data.dataParaExibicao()} às ${lembrete.hora}"
    }

    private fun configurarDescricao(textView: TextView, descricao: String) {
        if (descricao.isBlank()) {
            textView.visibility = View.GONE
            return
        }

        textView.text = descricao
        textView.visibility = View.VISIBLE
    }

    private fun configurarBotaoExcluir(itemView: View, lembrete: Lembrete) {
        itemView.findViewById<ImageButton>(R.id.iBtnExcluirLembrete).setOnClickListener {
            confirmarExclusaoLembrete(lembrete)
        }
    }

    private fun confirmarExclusaoLembrete(lembrete: Lembrete) {
        AlertDialog.Builder(this)
            .setTitle(TITULO_DIALOG_EXCLUSAO)
            .setMessage("Deseja remover o lembrete \"${lembrete.titulo}\"?")
            .setPositiveButton(BOTAO_REMOVER) { _, _ ->
                excluirLembrete(lembrete)
            }
            .setNegativeButton(BOTAO_CANCELAR, null)
            .show()
    }

    private fun excluirLembrete(lembrete: Lembrete) {
        BancoHelper.getInstance(this).removerLembrete(lembrete.id)

        LembreteManager.cancelarNotificacao(this, lembrete.id)

        carregarLembretes()
    }

    private fun carregarLembretes() {
        val lembretes = BancoHelper.getInstance(this).listarLembretes()

        listaLembretes.removeAllViews()

        if (lembretes.isEmpty()) {
            exibirMensagemSemLembretes()
            return
        }

        ocultarMensagemSemLembretes()

        lembretes.forEach { lembrete ->
            adicionarItemLembrete(lembrete)
        }
    }

    private fun configurarNavegacao() {
        configurarDestino(R.id.llInicio, Inicio::class.java)
        configurarDestino(R.id.llVeiculos, Veiculos::class.java)
        configurarDestino(R.id.llAdicionar, RegistrarServico::class.java)
        configurarDestino(R.id.llRelatorios, Relatorios::class.java)
    }

    private fun configurarDestino(idView: Int, destino: Class<out AppCompatActivity>) {
        findViewById<LinearLayout>(idView).setOnClickListener {
            startActivity(Intent(this, destino))
            overridePendingTransition(0, 0)
        }
    }

    private fun verificarPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permissaoConcedida = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissaoConcedida) {
            solicitadorPermissaoNotificacao.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    private companion object {
        private const val HORA_PADRAO = "12:00"

        private const val FORMATO_DATA_EXIBICAO = "dd/MM/yyyy"
        private const val FORMATO_DATA_BANCO = "yyyy-MM-dd"

        private const val OPCAO_SEM_VEICULO = "Nenhum veículo vinculado"

        private const val TITULO_DIALOG_EXCLUSAO = "Remover Lembrete"
        private const val BOTAO_REMOVER = "Remover"
        private const val BOTAO_CANCELAR = "Cancelar"
        private const val BOTAO_SALVAR = "Salvar"

        private const val TEXTO_TITULO_OBRIGATORIO = "Informe o título do lembrete"

        private const val TEXTO_SUCESSO_CRIAR = "Lembrete criado com sucesso!"

        private const val TEXTO_ERRO_CRIAR = "Erro ao criar lembrete"

        private const val ID_INVALIDO = -1L
    }
}