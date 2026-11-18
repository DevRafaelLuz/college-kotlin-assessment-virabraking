package com.example.virabraking

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class DetalhesRegistro : AppCompatActivity() {
    private var servicoId = Constantes.ID_INVALIDO
    private var veiculoId = Constantes.ID_INVALIDO

    private lateinit var tvTipoServico: TextView
    private lateinit var tvVeiculo: TextView
    private lateinit var tvPlacaVeiculo: TextView
    private lateinit var tvData: TextView
    private lateinit var tvQuilometragem: TextView
    private lateinit var tvValorTotal: TextView
    private lateinit var tvOficina: TextView
    private lateinit var tvObservacoes: TextView

    private val formatoMoeda: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(LOCALE_BR)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_registro)

        obterIdentificadores()

        if (!registroValido()) {
            exibirMensagem(MENSAGEM_REGISTRO_NAO_ENCONTRADO)
            finish()
            return
        }

        inicializarViews()
        configurarBotaoVoltar()
        carregarDados()
        configurarBotaoExcluir()
    }

    private fun obterIdentificadores() {
        servicoId = intent.getIntExtra(
            Constantes.EXTRA_SERVICO_ID,
            Constantes.ID_INVALIDO
        )

        veiculoId = intent.getIntExtra(
            Constantes.EXTRA_VEICULO_ID,
            Constantes.ID_INVALIDO
        )
    }

    private fun registroValido(): Boolean {
        return servicoId != Constantes.ID_INVALIDO
    }

    private fun inicializarViews() {
        tvTipoServico = findViewById(R.id.tvTipoServico)
        tvVeiculo = findViewById(R.id.tvVeiculo)
        tvPlacaVeiculo = findViewById(R.id.tvPlacaVeiculo)
        tvData = findViewById(R.id.tvData)
        tvQuilometragem = findViewById(R.id.tvQuilometragem)
        tvValorTotal = findViewById(R.id.tvValorTotal)
        tvOficina = findViewById(R.id.tvOficina)
        tvObservacoes = findViewById(R.id.tvObservacoes)
    }

    private fun configurarBotaoVoltar() {
        findViewById<ImageButton>(R.id.iBtnVoltar).setOnClickListener {
            finalizarTela()
        }
    }

    private fun finalizarTela() {
        finish()
        overridePendingTransition(0, 0)
    }

    private fun carregarDados() {
        val banco = BancoHelper.getInstance(this)
        val servico = banco.buscarServico(servicoId)

        if (servico == null) {
            exibirMensagem(MENSAGEM_SERVICO_NAO_ENCONTRADO)
            finish()
            return
        }

        val veiculo = buscarVeiculo(banco, servico.veiculoId)

        exibirComprovante(servico.caminhoComprovante)
        exibirDadosServico(servico, veiculo)
    }

    private fun exibirMensagem(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun buscarVeiculo(banco: BancoHelper, idVeiculoServico: Int) = banco.buscarVeiculo(
        if (veiculoId != Constantes.ID_INVALIDO) {
            veiculoId
        } else {
            idVeiculoServico
        }
    )

    private fun exibirComprovante(caminhoComprovante: String?) {
        val imagemComprovante = findViewById<ImageView>(R.id.ivComprovante)
        val arquivoComprovante = caminhoComprovante
            ?.let(::File)
            ?.takeIf { it.exists() }

        if (arquivoComprovante != null) {
            imagemComprovante.background = null
            imagemComprovante.setImageURI(Uri.fromFile(arquivoComprovante))
        } else {
            limparComprovante(imagemComprovante)
        }
    }

    private fun limparComprovante(imagemComprovante: ImageView) {
        imagemComprovante.setImageDrawable(null)
        imagemComprovante.setBackgroundResource(R.drawable.bg_branco_arredondado_com_borda)
    }

    private fun exibirDadosServico(servico: Servico, veiculo: Veiculo?) {
        tvTipoServico.text = servico.tipo

        tvVeiculo.text = veiculo?.apelido ?: TEXTO_VEICULO_NAO_IDENTIFICADO

        tvPlacaVeiculo.text = veiculo?.placa?.takeIf { it.isNotBlank() } ?.let { "($it)" } ?: ""

        tvData.text = formatarDataParaExibicao(servico.data)

        val quilometragem = obterQuilometragem(servico.quilometragem, veiculo?.quilometragem)

        tvQuilometragem.text = String.format(LOCALE_BR, FORMATO_QUILOMETRAGEM, quilometragem)

        tvValorTotal.text = formatoMoeda.format(servico.valor)

        tvOficina.text = servico.oficina.ifBlank {
            TEXTO_OFICINA_NAO_INFORMADA
        }

        tvObservacoes.text = servico.observacoes.ifBlank {
            TEXTO_SEM_OBSERVACOES
        }
    }

    private fun formatarDataParaExibicao(dataBanco: String): String {
        return try {
            val formatoBanco = SimpleDateFormat(FORMATO_DATA_BANCO, LOCALE_BR).apply {
                isLenient = false
            }

            val formatoExibicao = SimpleDateFormat(FORMATO_DATA_EXIBICAO, LOCALE_BR)

            val dataFormatada = formatoBanco.parse(dataBanco)

            dataFormatada?.let(formatoExibicao::format) ?: dataBanco
        } catch (e: Exception) {
            dataBanco
        }
    }

    private fun obterQuilometragem(quilometragemServico: Int, quilometragemVeiculo: Int?): Int {
        return if (quilometragemServico > 0) {
            quilometragemServico
        } else {
            quilometragemVeiculo ?: 0
        }
    }

    private fun configurarBotaoExcluir() {
        findViewById<Button>(R.id.btnExcluir).setOnClickListener {
            exibirDialogoExclusao()
        }
    }

    private fun exibirDialogoExclusao() {
        AlertDialog.Builder(this)
            .setTitle(TITULO_EXCLUSAO)
            .setMessage(MENSAGEM_CONFIRMACAO_EXCLUSAO)
            .setPositiveButton(TEXTO_EXCLUIR) { _, _ ->
                excluirRegistro()
            }
            .setNegativeButton(TEXTO_CANCELAR, null)
            .show()
    }

    private fun excluirRegistro() {
        val banco = BancoHelper.getInstance(this)
        val servico = banco.buscarServico(servicoId)

        removerArquivoComprovante(servico?.caminhoComprovante)

        val linhasAfetadas = banco.removerServico(servicoId)

        if (linhasAfetadas > 0) {
            exibirMensagem(MENSAGEM_REGISTRO_EXCLUIDO)
            finalizarTela()
        } else {
            exibirMensagem(MENSAGEM_ERRO_EXCLUSAO)
        }
    }

    private fun removerArquivoComprovante(caminhoComprovante: String?) {
        caminhoComprovante
            ?.let(::File)
            ?.takeIf { it.exists() }
            ?.delete()
    }

    private companion object {
        private val LOCALE_BR = Locale.forLanguageTag("pt-BR")

        private const val FORMATO_DATA_BANCO = "yyyy-MM-dd"
        private const val FORMATO_DATA_EXIBICAO = "dd/MM/yyyy"
        private const val FORMATO_QUILOMETRAGEM = "%,d km"

        private const val TITULO_EXCLUSAO = "Excluir registro"
        private const val MENSAGEM_CONFIRMACAO_EXCLUSAO = "Tem certeza que deseja excluir este serviço? Essa ação não pode ser desfeita."

        private const val TEXTO_EXCLUIR = "Excluir"
        private const val TEXTO_CANCELAR = "Cancelar"

        private const val MENSAGEM_REGISTRO_NAO_ENCONTRADO = "Erro: Registro não encontrado."

        private const val MENSAGEM_SERVICO_NAO_ENCONTRADO = "Erro: Serviço não encontrado."

        private const val MENSAGEM_REGISTRO_EXCLUIDO = "Registro excluído."

        private const val MENSAGEM_ERRO_EXCLUSAO = "Erro ao excluir registro."

        private const val TEXTO_VEICULO_NAO_IDENTIFICADO = "Veículo não identificado"

        private const val TEXTO_OFICINA_NAO_INFORMADA = "Não informada"

        private const val TEXTO_SEM_OBSERVACOES = "Nenhuma observação registrada."
    }
}