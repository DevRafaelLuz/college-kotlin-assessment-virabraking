package com.example.virabraking

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
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

    private val corSelecionada = Color.parseColor("#0d9488")
    private val corNaoSelecionada = Color.parseColor("#64748b")

    private val selecionarFotoLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uriImagemSelecionada = uri
            ivPreviewVeiculo.setImageURI(uri)
            ivPreviewVeiculo.visibility = View.VISIBLE
            llPlaceholderFoto.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_veiculo)

        inicializarComponentes()
        configurarSelecaoTipoVeiculo()
        configurarBotoes()
    }

    private fun inicializarComponentes() {
        flAdicionarFoto = findViewById(R.id.flAdicionarFoto)
        llPlaceholderFoto = findViewById(R.id.llPlaceholderFoto)
        ivPreviewVeiculo = findViewById(R.id.ivPreviewVeiculo)

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

        etApelido = findViewById(R.id.etApelido)
        etMarca = findViewById(R.id.etMarca)
        etModelo = findViewById(R.id.etModelo)
        etAno = findViewById(R.id.etAno)
        etPlaca = findViewById(R.id.etPlaca)
        etQuilometragem = findViewById(R.id.etQuilometragem)
    }

    private fun configurarSelecaoTipoVeiculo() {
        layoutsTiposVeiculo.forEachIndexed { indice, layout ->
            layout.setOnClickListener {
                selecionarTipoVeiculo(indice)
            }
        }

        atualizarEstiloTipoSelecionado()
    }

    private fun selecionarTipoVeiculo(indiceSelecionado: Int) {
        tipoSelecionado = textosTiposVeiculo[indiceSelecionado].text.toString()

        atualizarEstiloTipoSelecionado()
    }

    private fun atualizarEstiloTipoSelecionado() {
        layoutsTiposVeiculo.forEachIndexed { indice, layout ->
            val selecionado = textosTiposVeiculo[indice].text.toString() == tipoSelecionado

            layout.setBackgroundResource(
                if (selecionado) {
                    R.drawable.btn_tipo_veiculo
                } else {
                    R.drawable.bg_branco_arredondado_com_borda
                }
            )

            imagensTiposVeiculo[indice].setColorFilter(
                if (selecionado) corSelecionada else corNaoSelecionada
            )

            textosTiposVeiculo[indice].setTextColor(
                if (selecionado) corSelecionada else corNaoSelecionada
            )
        }
    }

    private fun configurarBotoes() {
        findViewById<ImageButton>(R.id.iBtnVoltar).setOnClickListener {
            finish()
            overridePendingTransition(0, 0);
        }

        flAdicionarFoto.setOnClickListener {
            selecionarFotoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        findViewById<Button>(R.id.btnSalvarVeiculo).setOnClickListener {
            salvarVeiculo()
        }
    }

    private fun salvarVeiculo() {
        val apelido = etApelido.text.toString().trim()

        if (tipoSelecionado.isBlank() || apelido.isBlank()) {
            exibirMensagem("Selecione o tipo e informe o nome")
            return
        }

        val caminhoSalvo = uriImagemSelecionada?.let { salvarImagemNoArmazenamentoInterno(it) }

        val veiculo = Veiculo(
            tipo = tipoSelecionado,
            apelido = apelido,
            marca = etMarca.text.toString().trim(),
            modelo = etModelo.text.toString().trim(),
            ano = etAno.text.toString().toIntOrNull() ?: 0,
            placa = etPlaca.text.toString().trim(),
            quilometragem = etQuilometragem.text.toString().toIntOrNull() ?: 0,
            caminhoFoto = caminhoSalvo
        )

        BancoHelper.getInstance(this).inserirVeiculo(veiculo)

        exibirMensagem("Veículo salvo")
        finish()
    }

    private fun exibirMensagem(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun salvarImagemNoArmazenamentoInterno(uri: Uri): String? {
        return try {
            val pastaFotos = File(filesDir, "fotos_veiculos")
            if (!pastaFotos.exists()) {
                pastaFotos.mkdirs()
            }

            val arquivoDestino = File(pastaFotos, "veiculo_${System.currentTimeMillis()}.jpg")

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(arquivoDestino).use { output ->
                    input.copyTo(output)
                }
            }

            arquivoDestino.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}