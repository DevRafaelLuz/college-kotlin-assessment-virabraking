package com.example.virabraking

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.button.MaterialButton

class Relatorios : AppCompatActivity() {

    // Filtro escolhido no momento (começa em "Este Mês")
    private var inicioDoPeriodo: () -> String = ::inicioDoMes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorios)

        val llInicio = findViewById<LinearLayout>(R.id.llInicio)
        llInicio.setOnClickListener {
            startActivity(Intent(this, Inicio::class.java))
        }

        val llVeiculos = findViewById<LinearLayout>(R.id.llVeiculos)
        llVeiculos.setOnClickListener {
            startActivity(Intent(this, Veiculos::class.java))
        }

        val llAlertas = findViewById<LinearLayout>(R.id.llAlertas)
        llAlertas.setOnClickListener {
            startActivity(Intent(this, Alertas::class.java))
        }

        val llAdicionar = findViewById<LinearLayout>(R.id.llAdicionar)
        llAdicionar.setOnClickListener {
            startActivity(Intent(this, RegistrarServico::class.java))
        }

        configurarFiltros()
    }

    override fun onResume() {
        super.onResume()
        carregarRelatorio(inicioDoPeriodo())
    }

    private fun configurarFiltros() {
        val filtros = listOf<Pair<MaterialButton, () -> String>>(
            findViewById<MaterialButton>(R.id.btnEsteMes) to ::inicioDoMes,
            findViewById<MaterialButton>(R.id.btnTrimestre) to ::inicioDosUltimosTresMeses,
            findViewById<MaterialButton>(R.id.btnEsteAno) to ::inicioDoAno
        )
        val botoes = filtros.map { it.first }

        filtros.forEach { (botao, inicio) ->
            botao.setOnClickListener {
                destacarBotao(botoes, botao)
                inicioDoPeriodo = inicio
                carregarRelatorio(inicio())
            }
        }

        destacarBotao(botoes, botoes[0])
    }

    private fun destacarBotao(listaBotoes: List<MaterialButton>, selecionado: MaterialButton) {
        listaBotoes.forEach { botoes ->
            botoes.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))
            botoes.strokeColor = ColorStateList.valueOf(Color.parseColor("#e2e8f0"))
            botoes.setTextColor(Color.parseColor("#64748b"))
        }

        selecionado.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0d9488"))
        selecionado.strokeColor = ColorStateList.valueOf(Color.parseColor("#0d9488"))
        selecionado.setTextColor(Color.parseColor("#ffffff"))
    }

    private fun carregarRelatorio(inicio: String) {
        val banco = BancoHelper(this)

        val totais = linkedMapOf(
            "Combustível" to banco.totalCombustivelDesde(inicio),
            "Revisões" to 0.0,
            "Óleo" to 0.0,
            "Pneus" to 0.0,
            "Outros" to 0.0
        )
        banco.totalServicosPorTipoDesde(inicio).forEach { (tipo, valor) ->
            val categoria = categoriaDoServico(tipo)
            totais[categoria] = (totais[categoria] ?: 0.0) + valor
        }

        val linhas = listOf(
            Triple("Combustível", R.id.tvValorCombustivel, R.id.pbCombustivel),
            Triple("Revisões", R.id.tvValorRevisoes, R.id.pbRevisoes),
            Triple("Óleo", R.id.tvValorOleo, R.id.pbOleo),
            Triple("Pneus", R.id.tvValorPneus, R.id.pbPneus),
            Triple("Outros", R.id.tvValorOutros, R.id.pbOutros)
        )

        val maior = totais.values.maxOrNull() ?: 0.0
        linhas.forEach { (nome, idTexto, idBarra) ->
            val valor = totais[nome] ?: 0.0
            findViewById<TextView>(idTexto).text = valor.emReais()
            findViewById<ProgressBar>(idBarra).progress =
                if (maior > 0) (valor / maior * 100).toInt() else 0
        }
    }

    // Agrupa o tipo de serviço (texto livre) nas categorias da tela
    private fun categoriaDoServico(tipo: String): String {
        val t = tipo.lowercase(LOCALE_BR)
        return when {
            t.contains("óleo") || t.contains("oleo") -> "Óleo"
            t.contains("pneu") -> "Pneus"
            t.contains("revis") -> "Revisões"
            else -> "Outros"
        }
    }
}