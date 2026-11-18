package com.example.virabraking

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Atividades : AppCompatActivity() {
    private lateinit var containerAtividades: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_atividades)

        inicializarComponentes()
        configurarNavegacao()
    }

    override fun onResume() {
        super.onResume()
        carregarAtividades()
    }

    private fun inicializarComponentes() {
        containerAtividades = findViewById(R.id.llTodasAtividades)
    }

    private fun configurarNavegacao() {
        configurarBotaoVoltar()
    }

    private fun configurarBotaoVoltar() {
        findViewById<ImageButton>(R.id.iBtnVoltar).setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
    }

    private fun carregarAtividades() {
        containerAtividades.removeAllViews()

        val atividades = BancoHelper.getInstance(this).listarAtividades(limite = null)

        if (atividades.isEmpty()) {
            exibirListaVazia()
            return
        }

        atividades.forEach { atividade ->
            adicionarAtividade(atividade)
        }
    }

    private fun exibirListaVazia() {
        val mensagem = criarMensagemListaVazia()
        containerAtividades.addView(mensagem)
    }

    private fun criarMensagemListaVazia(): TextView {
        return TextView(this).apply {
            text = TEXTO_LISTA_VAZIA
            textSize = TAMANHO_TEXTO_VAZIO
            setTextColor(COR_TEXTO_VAZIO)
            setPadding(0, ESPACAMENTO_LISTA_VAZIA, 0, 0)
        }
    }

    private fun adicionarAtividade(atividade: Atividade) {
        val atividadeView = atividade.paraView(layoutInflater, containerAtividades)

        configurarCliqueAtividade(atividadeView, atividade)

        containerAtividades.addView(atividadeView)
    }

    private fun configurarCliqueAtividade(atividadeView: android.view.View, atividade: Atividade) {
        if (atividade.origem == ORIGEM_SERVICO) {
            atividadeView.setOnClickListener {
                abrirDetalhesServico(atividade)
            }
        }
    }

    private fun abrirDetalhesServico(atividade: Atividade) {
        val intent = criarIntentDetalhesServico(atividade)
        startActivity(intent)
    }

    private fun criarIntentDetalhesServico(atividade: Atividade): Intent {
        return Intent(this, DetalhesRegistro::class.java).apply {
            putExtra(Constantes.EXTRA_SERVICO_ID, atividade.id)
            putExtra(Constantes.EXTRA_VEICULO_ID, atividade.veiculoId)
        }
    }

    private companion object {
        private const val ORIGEM_SERVICO = "servico"

        private const val TEXTO_LISTA_VAZIA = "Nenhuma atividade registrada ainda"

        private const val TAMANHO_TEXTO_VAZIO = 14f
        private const val ESPACAMENTO_LISTA_VAZIA = 24

        private const val COR_TEXTO_VAZIO = 0xFF64748B.toInt()
    }
}
