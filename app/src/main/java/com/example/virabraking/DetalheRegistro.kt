package com.example.virabraking

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DetalheRegistro : AppCompatActivity() {

    private var servicoId = Constantes.ID_INVALIDO
    private var veiculoId = Constantes.ID_INVALIDO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_registro)

        // Leitura com constantes centralizadas
        servicoId = intent.getIntExtra(Constantes.EXTRA_SERVICO_ID, Constantes.ID_INVALIDO)
        veiculoId = intent.getIntExtra(Constantes.EXTRA_VEICULO_ID, Constantes.ID_INVALIDO)

        // Proteção contra parâmetros ausentes ou corrompidos
        if (servicoId == Constantes.ID_INVALIDO) {
            Toast.makeText(this, "Erro: Registro não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val iBtnVoltar = findViewById<ImageButton>(R.id.iBtnVoltar)
        iBtnVoltar.setOnClickListener {
            finish()
        }

        carregarDetalhesServico()
    }

    private fun carregarDetalhesServico() {
        // Consultar BancoHelper(this) usando servicoId/veiculoId e preencher as views
    }
}