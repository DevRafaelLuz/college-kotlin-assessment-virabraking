package com.example.virabraking

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class DetalheRegistro : AppCompatActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_registro)

        servicoId = intent.getIntExtra(Constantes.EXTRA_SERVICO_ID, Constantes.ID_INVALIDO)
        veiculoId = intent.getIntExtra(Constantes.EXTRA_VEICULO_ID, Constantes.ID_INVALIDO)

        if (servicoId == Constantes.ID_INVALIDO) {
            Toast.makeText(this, "Erro: Registro não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        inicializarViews()
        configurarBotaoVoltar()
        carregarDados()
        configurarBotaoExcluir()
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
        val iBtnVoltar = findViewById<ImageButton>(R.id.iBtnVoltar)
        iBtnVoltar.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
    }

    private fun carregarDados() {
        val banco = BancoHelper.getInstance(this)
        val servico = banco.buscarServico(servicoId)

        if (servico == null) {
            Toast.makeText(this, "Erro: Serviço não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val idVeiculoBusca = if (veiculoId != Constantes.ID_INVALIDO) veiculoId else servico.veiculoId
        val veiculo = banco.buscarVeiculo(idVeiculoBusca)

        val localeBR = Locale("pt", "BR")
        val formatoMoeda = NumberFormat.getCurrencyInstance(localeBR)

        val ivComprovante = findViewById<ImageView>(R.id.ivComprovante)
        val arquivoComprovante = servico.caminhoComprovante?.let { java.io.File(it) }

        if (arquivoComprovante != null && arquivoComprovante.exists()) {
            ivComprovante.background = null
            ivComprovante.setImageURI(android.net.Uri.fromFile(arquivoComprovante))
        } else {
            ivComprovante.setImageDrawable(null)
            ivComprovante.setBackgroundResource(R.drawable.bg_branco_arredondado_com_borda)
        }

        tvTipoServico.text = servico.tipo
        tvVeiculo.text = veiculo?.apelido ?: "Veículo não identificado"
        tvPlacaVeiculo.text = if (!veiculo?.placa.isNullOrBlank()) "(${veiculo?.placa})" else ""
        tvData.text = formatarDataParaExibicao(servico.data)
        val kmExibicao = if (servico.quilometragem > 0) servico.quilometragem else (veiculo?.quilometragem ?: 0)
        tvQuilometragem.text = String.format(localeBR, "%,d km", kmExibicao)
        tvValorTotal.text = formatoMoeda.format(servico.valor)
        tvOficina.text = servico.oficina.ifBlank { "Não informada" }
        tvObservacoes.text = servico.observacoes.ifBlank { "Nenhuma observação registrada." }
    }

    private fun formatarDataParaExibicao(dataBanco: String): String {
        return try {
            val formatoBanco = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoBr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val data = formatoBanco.parse(dataBanco)
            if (data != null) formatoBr.format(data) else dataBanco
        } catch (e: Exception) {
            dataBanco
        }
    }

    private fun configurarBotaoExcluir() {
        findViewById<Button>(R.id.btnExcluir).setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Excluir registro")
                .setMessage("Tem certeza que deseja excluir este serviço? Essa ação não pode ser desfeita.")
                .setPositiveButton("Excluir") { _, _ ->
                    excluirRegistro()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun excluirRegistro() {
        val banco = BancoHelper.getInstance(this)
        val servico = banco.buscarServico(servicoId)

        servico?.caminhoComprovante?.let { caminho ->
            val arquivo = java.io.File(caminho)
            if (arquivo.exists()) arquivo.delete()
        }

        val linhasAfetadas = banco.removerServico(servicoId)
        if (linhasAfetadas > 0) {
            Toast.makeText(this, "Registro excluído.", Toast.LENGTH_SHORT).show()
            finish()
            overridePendingTransition(0, 0)
        } else {
            Toast.makeText(this, "Erro ao excluir registro.", Toast.LENGTH_SHORT).show()
        }
    }
}