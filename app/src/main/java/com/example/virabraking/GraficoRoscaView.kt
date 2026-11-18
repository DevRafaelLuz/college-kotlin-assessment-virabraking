package com.example.virabraking

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View

class GraficoRoscaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var fatias: List<Pair<Float, Int>> = emptyList()
    private val retanguloGrafico  = RectF()

    private val paintArco = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }

    private val paintFundo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = COR_FUNDO
    }

    private val paintTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COR_TEXTO
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(typeface, Typeface.BOLD)
    }

    fun definirDados(dados: List<Pair<Float, Int>>) {
        fatias = dados
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        configurarDimensoes()

        if (fatias.isEmpty()) {
            desenharGraficoVazio(canvas)
        } else {
            desenharFatias(canvas)
        }

        desenharTextoCentral(canvas)
    }

    private fun configurarDimensoes() {
        val larguraAnel = width * PROPORCAO_LARGURA_ANEL

        paintArco.strokeWidth = larguraAnel
        paintFundo.strokeWidth = larguraAnel

        val margem = larguraAnel / 2f + MARGEM_GRAFICO

        retanguloGrafico.set(
            margem,
            margem,
            width - margem,
            height - margem
        )
    }

    private fun desenharGraficoVazio(canvas: Canvas) {
        canvas.drawArc(
            retanguloGrafico,
            ANGULO_INICIAL,
            ANGULO_COMPLETO,
            false,
            paintFundo
        )
    }

    private fun desenharFatias(canvas: Canvas) {
        var anguloInicial = ANGULO_INICIAL

        fatias.forEach { (percentual, cor) ->
            val anguloVarredura = calcularAngulo(percentual)

            paintArco.color = cor

            canvas.drawArc(
                retanguloGrafico,
                anguloInicial,
                anguloVarredura,
                false,
                paintArco
            )

            anguloInicial += anguloVarredura
        }
    }

    private fun calcularAngulo(percentual: Float): Float {
        return percentual / 100f * ANGULO_COMPLETO
    }

    private fun desenharTextoCentral(canvas: Canvas) {
        paintTexto.textSize = width * PROPORCAO_TAMANHO_TEXTO

        val posicaoVertical = calcularPosicaoVerticalTexto()

        canvas.drawText(
            TEXTO_CENTRAL,
            width / 2f,
            posicaoVertical,
            paintTexto
        )
    }

    private fun calcularPosicaoVerticalTexto(): Float {
        return (height / 2f) - ((paintTexto.descent() + paintTexto.ascent()) / 2f)
    }

    private companion object {
        private const val ANGULO_INICIAL = -90f
        private const val ANGULO_COMPLETO = 360f

        private const val PROPORCAO_LARGURA_ANEL = 0.16f
        private const val PROPORCAO_TAMANHO_TEXTO = 0.12f

        private const val MARGEM_GRAFICO = 4f

        private const val TEXTO_CENTRAL = "Veículos"

        private val COR_FUNDO = Color.parseColor("#E2E8F0")
        private val COR_TEXTO = Color.parseColor("#0F172A")
    }
}