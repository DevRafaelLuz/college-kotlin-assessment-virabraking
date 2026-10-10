package com.example.virabraking

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class AdicionarVeiculo : AppCompatActivity() {

    private var tipoSelecionado = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_veiculo)

        val iBtnVoltar = findViewById<ImageButton>(R.id.iBtnVoltar)
        iBtnVoltar.setOnClickListener {
            finish()
        }

        alterarEstiloBotaoSelecionado(obtemIdLinearLayouts(), obtemIdImageViews(), obtemIdTextView())

        val btnSalvarVeiculo = findViewById<Button>(R.id.btnSalvarVeiculo)
        btnSalvarVeiculo.setOnClickListener {
            salvarVeiculo()
        }
    }

    private fun alterarEstiloBotaoSelecionado(tipoVeiculo: List<LinearLayout>, ivVeiculos: List<ImageView>, tvVeiculos: List<TextView>) {
        for (i in tipoVeiculo.indices) {
            tipoVeiculo[i].setOnClickListener {
                for (j in tipoVeiculo.indices) {
                    tipoVeiculo[j].setBackgroundResource(R.drawable.bg_branco_arredondado_com_borda)
                    ivVeiculos[j].setColorFilter(Color.parseColor("#64748b"))
                    tvVeiculos[j].setTextColor(Color.parseColor("#64748b"))
                }

                tipoVeiculo[i].setBackgroundResource(R.drawable.btn_tipo_veiculo)
                ivVeiculos[i].setColorFilter(Color.parseColor("#0d9488"))
                tvVeiculos[i].setTextColor(Color.parseColor("#0d9488"))
                tipoSelecionado = tvVeiculos[i].text.toString()
            }
        }
    }

    private fun obtemIdLinearLayouts(): List<LinearLayout> {
        val llTipoCarro = findViewById<LinearLayout>(R.id.llTipoCarro)
        val llTipoMoto = findViewById<LinearLayout>(R.id.llTipoMoto)
        val llTipoCaminhao = findViewById<LinearLayout>(R.id.llTipoCaminhao)

        return listOf(llTipoCarro, llTipoMoto, llTipoCaminhao)
    }

    private fun obtemIdImageViews(): List<ImageView> {
        val ivCarro = findViewById<ImageView>(R.id.ivCarro)
        val ivMoto = findViewById<ImageView>(R.id.ivMoto)
        val ivCaminhao = findViewById<ImageView>(R.id.ivCaminhao)

        return listOf(ivCarro, ivMoto, ivCaminhao)
    }

    private fun obtemIdTextView(): List<TextView> {
        val tvCarro = findViewById<TextView>(R.id.tvCarro)
        val tvMoto = findViewById<TextView>(R.id.tvMoto)
        val tvCaminhao = findViewById<TextView>(R.id.tvCaminhao)

        return listOf(tvCarro, tvMoto, tvCaminhao)
    }

    private fun salvarVeiculo() {
        val apelido = findViewById<EditText>(R.id.etApelido).text.toString().trim()
        val marca = findViewById<EditText>(R.id.etMarca).text.toString().trim()
        val modelo = findViewById<EditText>(R.id.etModelo).text.toString().trim()
        val ano = findViewById<EditText>(R.id.etAno).text.toString().toIntOrNull() ?: 0
        val placa = findViewById<EditText>(R.id.etPlaca).text.toString().trim()
        val quilometragem = findViewById<EditText>(R.id.etQuilometragem).text.toString().toIntOrNull() ?: 0

        if (tipoSelecionado.isEmpty() || apelido.isEmpty()) {
            Toast.makeText(this, "Selecione o tipo e informe o nome", Toast.LENGTH_SHORT).show()
            return
        }

        val veiculo = Veiculo(
            tipo = tipoSelecionado,
            apelido = apelido,
            marca = marca,
            modelo = modelo,
            ano = ano,
            placa = placa,
            quilometragem = quilometragem
        )

        BancoHelper(this).inserirVeiculo(veiculo)
        Toast.makeText(this, "Veículo salvo", Toast.LENGTH_SHORT).show()
        finish()
    }
}