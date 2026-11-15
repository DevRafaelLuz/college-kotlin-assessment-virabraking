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
    private val retangulo = RectF()

    private val paintArco = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }

    private val paintFundo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#E2E8F0")
    }

    private val paintTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0F172A")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(typeface, Typeface.BOLD)
    }

    fun definirDados(dados: List<Pair<Float, Int>>) {
        fatias = dados
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val larguraAnel = width * 0.16f
        paintArco.strokeWidth = larguraAnel
        paintFundo.strokeWidth = larguraAnel

        val margem = larguraAnel / 2f + 4f
        retangulo.set(margem, margem, width - margem, height - margem)

        if (fatias.isEmpty()) {
            canvas.drawArc(retangulo, 0f, 360f, false, paintFundo)
        } else {
            var anguloInicial = -90f
            for ((percentual, cor) in fatias) {
                val varredura = (percentual / 100f) * 360f
                paintArco.color = cor
                canvas.drawArc(retangulo, anguloInicial, varredura, false, paintArco)
                anguloInicial += varredura
            }
        }

        paintTexto.textSize = width * 0.12f
        val yTexto = (height / 2f) - ((paintTexto.descent() + paintTexto.ascent()) / 2f)
        canvas.drawText("Veículos", width / 2f, yTexto, paintTexto)
    }
}