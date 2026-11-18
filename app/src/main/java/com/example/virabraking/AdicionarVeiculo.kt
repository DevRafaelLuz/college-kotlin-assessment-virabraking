package com.example.virabraking

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class AdicionarVeiculo : AppCompatActivity() {

    private var tipoSelecionado = ""
    private var uriImagemSelecionada: Uri? = null

    private lateinit var layoutsTiposVeiculo: List<LinearLayout>
    private lateinit var imagensTiposVeiculo: List<ImageView>
    private lateinit var textosTiposVeiculo: List<TextView>

    private lateinit var flAdicionarFoto: FrameLayout
    private lateinit var llPlaceholderFoto: LinearLayout
    private lateinit var ivPreviewVeiculo: ImageView

    private lateinit var etApelido: EditText
    private lateinit var etMarca: EditText
    private lateinit var etModelo: EditText
    private lateinit var etAno: EditText
    private lateinit var etPlaca: EditText
    private lateinit var etQuilometragem: EditText

    private val seletorDeImagem = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        uri -> uri?.let(::exibirImagemSelecionada)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_veiculo)

        inicializarComponentes()
        configurarEventos()
        atualizarEstiloTipoSelecionado()
    }

    private fun exibirImagemSelecionada(uri: Uri) {
        uriImagemSelecionada = uri
        ivPreviewVeiculo.setImageURI(uri)
        ivPreviewVeiculo.visibility = View.VISIBLE
        llPlaceholderFoto.visibility = View.GONE
    }

    private fun inicializarComponentes() {
        inicializarComponentesFoto()
        inicializarComponentesTipoVeiculo()
        inicializarCamposVeiculo()
    }

    private fun inicializarComponentesFoto() {
        flAdicionarFoto = findViewById(R.id.flAdicionarFoto)
        llPlaceholderFoto = findViewById(R.id.llPlaceholderFoto)
        ivPreviewVeiculo = findViewById(R.id.ivPreviewVeiculo)
    }

    private fun inicializarComponentesTipoVeiculo() {
        layoutsTiposVeiculo = listOf(
            findViewById(R.id.llTipoCarro),
            findViewById(R.id.llTipoMoto),
            findViewById(R.id.llTipoCaminhao)
        )

        imagensTiposVeiculo = listOf(
            findViewById(R.id.ivCarro),
            findViewById(R.id.ivMoto),
            findViewById(R.id.ivCaminhao)
        )

        textosTiposVeiculo = listOf(
            findViewById(R.id.tvCarro),
            findViewById(R.id.tvMoto),
            findViewById(R.id.tvCaminhao)
        )
    }

    private fun inicializarCamposVeiculo() {
        etApelido = findViewById(R.id.etApelido)
        etMarca = findViewById(R.id.etMarca)
        etModelo = findViewById(R.id.etModelo)
        etAno = findViewById(R.id.etAno)
        etPlaca = findViewById(R.id.etPlaca)
        etQuilometragem = findViewById(R.id.etQuilometragem)
    }

    private fun configurarEventos() {
        configurarSelecaoTipoVeiculo()
        configurarBotaoVoltar()
        configurarSelecaoFoto()
        configurarBotaoSalvar()
    }

    private fun configurarSelecaoTipoVeiculo() {
        layoutsTiposVeiculo.forEachIndexed { indice, layout ->
            layout.setOnClickListener {
                selecionarTipoVeiculo(indice)
            }
        }
    }

    private fun selecionarTipoVeiculo(indice: Int) {
        val nomeTipo = textosTiposVeiculo.getOrNull(indice)?.text?.toString() ?: return

        tipoSelecionado = nomeTipo
        atualizarEstiloTipoSelecionado()
    }

    private fun atualizarEstiloTipoSelecionado() {
        layoutsTiposVeiculo.forEachIndexed { indice, layout ->
            val selecionado = textosTiposVeiculo[indice].text.toString() == tipoSelecionado
            atualizarEstiloDoTipo(indice, layout, selecionado)
        }
    }

    private fun atualizarEstiloDoTipo(indice: Int, layout: LinearLayout, selecionado: Boolean) {
        layout.setBackgroundResource(
            if (selecionado) {
                R.drawable.btn_tipo_veiculo
            } else {
                R.drawable.bg_branco_arredondado_com_borda
            }
        )

        val cor = if (selecionado) COR_TIPO_SELECIONADO else COR_TIPO_NAO_SELECIONADO
        imagensTiposVeiculo[indice].setColorFilter(cor)
        textosTiposVeiculo[indice].setTextColor(cor)
    }

    private fun configurarBotaoVoltar() {
        findViewById<ImageButton>(R.id.iBtnVoltar).setOnClickListener {
            finalizarTela()
        }
    }

    private fun configurarSelecaoFoto() {
        flAdicionarFoto.setOnClickListener {
            seletorDeImagem.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
    }

    private fun configurarBotaoSalvar() {
        findViewById<Button>(R.id.btnSalvarVeiculo).setOnClickListener {
            salvarVeiculo()
        }
    }

    private fun salvarVeiculo() {
        if (!validarDadosObrigatorios()) return

        val caminhoFoto = uriImagemSelecionada?.let(::salvarImagemNoArmazenamentoInterno)
        val veiculo = criarVeiculo(caminhoFoto)

        BancoHelper.getInstance(this).inserirVeiculo(veiculo)

        exibirMensagem(MENSAGEM_VEICULO_SALVO)
        finalizarTela()
    }

    private fun validarDadosObrigatorios(): Boolean {
        if (tipoSelecionado.isNotBlank() && obterTexto(etApelido).isNotBlank()) {
            return true
        }

        exibirMensagem(MENSAGEM_TIPO_E_APELIDO_OBRIGATORIOS)
        return false
    }

    private fun criarVeiculo(caminhoFoto: String?): Veiculo {
        return Veiculo(
            tipo = tipoSelecionado,
            apelido = obterTexto(etApelido),
            marca = obterTexto(etMarca),
            modelo = obterTexto(etModelo),
            ano = obterTexto(etAno).toIntOrNull() ?: VALOR_PADRAO_NUMERICO,
            placa = obterTexto(etPlaca),
            quilometragem = obterTexto(etQuilometragem) .toIntOrNull() ?: VALOR_PADRAO_NUMERICO,
            caminhoFoto = caminhoFoto
        )
    }

    private fun obterTexto(campo: EditText): String {
        return campo.text.toString().trim()
    }

    private fun salvarImagemNoArmazenamentoInterno(uri: Uri): String? {
        return try {
            val pastaFotos = obterPastaFotos()

            if (!pastaFotos.exists() && !pastaFotos.mkdirs()) {
                error("Não foi possível criar a pasta de fotos dos veículos")
            }

            val arquivoDestino = File(
                pastaFotos,
                "$PREFIXO_ARQUIVO_FOTO${System.currentTimeMillis()}$EXTENSAO_IMAGEM"
            )

            val fluxoEntrada = contentResolver.openInputStream(uri)
                ?: error("Não foi possível abrir a imagem selecionada")

            fluxoEntrada.use { input ->
                FileOutputStream(arquivoDestino).use { output ->
                    input.copyTo(output)
                }
            }

            arquivoDestino.absolutePath
        } catch (erro: Exception) {
            Log.e(TAG_LOG, ERRO_AO_SALVAR_IMAGEM, erro)
            null
        }
    }

    private fun obterPastaFotos(): File {
        return File(filesDir, NOME_PASTA_FOTOS)
    }

    private fun exibirMensagem(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun finalizarTela() {
        finish()
        overridePendingTransition(0, 0)
    }

    private companion object {
        const val TAG_LOG = "AdicionarVeiculo"
        const val ERRO_AO_SALVAR_IMAGEM = "Erro ao salvar imagem do veículo"

        const val NOME_PASTA_FOTOS = "fotos_veiculos"
        const val PREFIXO_ARQUIVO_FOTO = "veiculo_"
        const val EXTENSAO_IMAGEM = ".jpg"

        const val VALOR_PADRAO_NUMERICO = 0

        const val MENSAGEM_TIPO_E_APELIDO_OBRIGATORIOS = "Selecione o tipo e informe o nome"
        const val MENSAGEM_VEICULO_SALVO = "Veículo salvo"

        val COR_TIPO_SELECIONADO: Int = Color.parseColor("#0d9488")
        val COR_TIPO_NAO_SELECIONADO: Int = Color.parseColor("#64748b")
    }
}