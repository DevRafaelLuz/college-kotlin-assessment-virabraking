package com.example.virabraking

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Veiculos : AppCompatActivity() {

    private lateinit var rvVeiculos: RecyclerView
    private lateinit var bancoHelper: BancoHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_veiculos)

        inicializarDependencias()
        inicializarViews()
        configurarTela()
    }

    override fun onResume() {
        super.onResume()
        carregarVeiculos()
    }

    private fun inicializarDependencias() {
        bancoHelper = BancoHelper.getInstance(this)
    }

    private fun inicializarViews() {
        rvVeiculos = findViewById(R.id.rvVeiculos)
    }

    private fun configurarTela() {
        configurarNavegacao()
        configurarListaVeiculos()
    }

    private fun configurarNavegacao() {
        configurarClique(R.id.llInicio, Inicio::class.java)
        configurarClique(R.id.llAlertas, Alertas::class.java)
        configurarClique(R.id.llRelatorios, Relatorios::class.java)
        configurarClique(R.id.iBtnAdicionar, AdicionarVeiculo::class.java)
        configurarClique(R.id.llAdicionar, RegistrarServico::class.java)
    }

    private fun configurarClique(viewId: Int, destino: Class<out AppCompatActivity>) {
        findViewById<android.view.View>(viewId).setOnClickListener {
            navegarPara(destino)
        }
    }

    private fun navegarPara(destino: Class<out AppCompatActivity>) {
        startActivity(Intent(this, destino))
        overridePendingTransition(0, 0)
    }

    private fun configurarListaVeiculos() {
        rvVeiculos.layoutManager = LinearLayoutManager(this)
    }

    private fun abrirDetalhesVeiculo(veiculo: Veiculo) {
        val intent = Intent(this, DetalhesVeiculo::class.java).apply {
            putExtra(Constantes.EXTRA_VEICULO_ID, veiculo.id)
        }

        iniciarActivity(intent)
    }

    private fun iniciarActivity(intent: Intent) {
        startActivity(intent)
        aplicarTransicao()
    }

    private fun aplicarTransicao() {
        overridePendingTransition(TRANSICAO_SEM_ANIMACAO, TRANSICAO_SEM_ANIMACAO)
    }

    private fun confirmarRemocao(veiculo: Veiculo) {
        AlertDialog.Builder(this)
            .setTitle(TITULO_REMOVER_VEICULO)
            .setMessage(getString(R.string.confirmar_remocao_veiculo, veiculo.apelido))
            .setPositiveButton(BOTAO_REMOVER) { _, _ ->
                removerVeiculo(veiculo)
            }
            .setNegativeButton(BOTAO_CANCELAR, null)
            .show()
    }

    private fun removerVeiculo(veiculo: Veiculo) {
        bancoHelper.removerVeiculo(veiculo.id)
        carregarVeiculos()
    }

    private fun alternarFavorito(veiculo: Veiculo) {
        val novoEstadoFavorito = !veiculo.favorito

        bancoHelper.definirFavorito(veiculo.id, novoEstadoFavorito)
        carregarVeiculos()
    }

    private fun carregarVeiculos() {
        val veiculos = bancoHelper.listarVeiculos()

        rvVeiculos.adapter = VeiculoAdapter(
            veiculos,
            ::abrirDetalhesVeiculo,
            ::confirmarRemocao,
            ::alternarFavorito
        )
    }

    private companion object {
        private const val TRANSICAO_SEM_ANIMACAO = 0

        private const val TITULO_REMOVER_VEICULO = "Remover veículo"
        private const val BOTAO_REMOVER = "Remover"
        private const val BOTAO_CANCELAR = "Cancelar"
    }
}