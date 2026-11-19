package com.example.virabraking

import android.content.Intent
import android.os.Bundle
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
        configurarClique(R.id.llAdicionar, RegistrarServico::class.java)

        findViewById<android.view.View>(R.id.iBtnAdicionar).setOnClickListener {
            if (bancoHelper.contarVeiculos() >= Constantes.LIMITE_MAXIMO_VEICULOS) {
                exibirAlertaLimiteVeiculos()
            } else {
                navegarPara(AdicionarVeiculo::class.java)
            }
        }
    }

    private fun exibirAlertaLimiteVeiculos() {
        DialogUtils.exibirDialogoAlerta(
            activity = this,
            titulo = TITULO_LIMITE_VEICULOS,
            mensagem = MENSAGEM_LIMITE_VEICULOS,
            textoBotao = BOTAO_OK
        )
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
        DialogUtils.exibirDialogoConfirmacao(
            activity = this,
            titulo = TITULO_REMOVER_VEICULO,
            mensagem = getString(R.string.confirmar_remocao_veiculo, veiculo.apelido),
            textoConfirmar = BOTAO_REMOVER,
            textoCancelar = BOTAO_CANCELAR,
            tipo = DialogUtils.TipoDialog.DESTRUICAO,
            onConfirmar = { removerVeiculo(veiculo) }
        )
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

        private const val TITULO_LIMITE_VEICULOS = "Limite de veículos"
        private const val MENSAGEM_LIMITE_VEICULOS = "Você atingiu o limite máximo de ${Constantes.LIMITE_MAXIMO_VEICULOS} veículos. Remova um veículo existente para poder cadastrar outro."
        private const val BOTAO_OK = "OK"
    }
}