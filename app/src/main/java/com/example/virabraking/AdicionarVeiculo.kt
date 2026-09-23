package com.example.virabraking

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class AdicionarVeiculo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_veiculo)

        val iBtnVoltar = findViewById<ImageButton>(R.id.iBtnVoltar)
        iBtnVoltar.setOnClickListener {
            finish()
        }

        alterarEstiloBotaoSelecionado(obtemIdLinearLayouts(), obtemIdImageViews(), obtemIdTextView())
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
}