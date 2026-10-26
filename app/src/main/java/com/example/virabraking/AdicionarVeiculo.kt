package com.example.virabraking

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AdicionarVeiculo : AppCompatActivity() {

    private var tipoSelecionado = ""

    private lateinit var layoutsTiposVeiculo: List<LinearLayout>
    private lateinit var imagensTiposVeiculo: List<ImageView>
    private lateinit var textosTiposVeiculo: List<TextView>

    private lateinit var etApelido: EditText
    private lateinit var etMarca: EditText
    private lateinit var etModelo: EditText
    private lateinit var etAno: EditText
    private lateinit var etPlaca: EditText
    private lateinit var etQuilometragem: EditText

    private val corSelecionada = Color.parseColor("#0d9488")
    private val corNaoSelecionada = Color.parseColor("#64748b")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_veiculo)

        inicializarComponentes()
        configurarSelecaoTipoVeiculo()
        configurarBotoes()
    }

    private fun inicializarComponentes() {
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

        val veiculo = criarVeiculo(apelido)

        BancoHelper.getInstance(this).inserirVeiculo(veiculo)

        exibirMensagem("Veículo salvo")
        finish()
    }

    private fun exibirMensagem(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun criarVeiculo(apelido: String): Veiculo {
        return Veiculo(
            tipo = tipoSelecionado,
            apelido = apelido,
            marca = etMarca.text.toString().trim(),
            modelo = etModelo.text.toString().trim(),
            ano = etAno.text.toString().toIntOrNull() ?: 0,
            placa = etPlaca.text.toString().trim(),
            quilometragem = etQuilometragem.text.toString().toIntOrNull() ?: 0
        )
    }
}