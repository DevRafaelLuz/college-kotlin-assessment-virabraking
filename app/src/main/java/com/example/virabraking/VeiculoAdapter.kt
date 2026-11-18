package com.example.virabraking

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class VeiculoAdapter(
    private val veiculos: List<Veiculo>,
    private val aoClicar: (Veiculo) -> Unit,
    private val aoSegurar: (Veiculo) -> Unit,
    private val aoFavoritar: (Veiculo) -> Unit
) : RecyclerView.Adapter<VeiculoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvApelido: TextView = view.findViewById(R.id.tvApelido)
        val tvModelo: TextView = view.findViewById(R.id.tvModelo)
        val tvPlaca: TextView = view.findViewById(R.id.tvPlaca)
        val tvKm: TextView = view.findViewById(R.id.tvKm)
        val ivVeiculo: ImageView = view.findViewById(R.id.ivVeiculo)
        val ivFavorito: ImageView = view.findViewById(R.id.ivFavorito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_veiculo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val veiculo = veiculos[position]

        configurarInformacoes(holder, veiculo)
        configurarEventos(holder, veiculo)
        configurarFavorito(holder, veiculo)
        configurarImagem(holder, veiculo)
    }

    override fun getItemCount(): Int = veiculos.size

    private fun configurarInformacoes(holder: ViewHolder, veiculo: Veiculo) {
        holder.tvApelido.text = veiculo.apelido

        holder.tvModelo.text = "${veiculo.marca} ${veiculo.modelo}".trim()

        holder.tvPlaca.text = veiculo.placa.ifEmpty {
            TEXTO_PLACA_NAO_INFORMADA
        }

        holder.tvKm.text = "${veiculo.quilometragem} km"
    }

    private fun configurarEventos(holder: ViewHolder, veiculo: Veiculo) {
        holder.itemView.setOnClickListener {
            aoClicar(veiculo)
        }

        holder.itemView.setOnLongClickListener {
            aoSegurar(veiculo)
            true
        }

        holder.ivFavorito.setOnClickListener {
            aoFavoritar(veiculo)
        }
    }

    private fun configurarFavorito(holder: ViewHolder, veiculo: Veiculo) {
        val iconeFavorito = if (veiculo.favorito) {
            android.R.drawable.btn_star_big_on
        } else {
            android.R.drawable.btn_star_big_off
        }

        val corFavorito = if (veiculo.favorito) {
            COR_FAVORITO
        } else {
            COR_NAO_FAVORITO
        }

        holder.ivFavorito.setImageResource(iconeFavorito)
        holder.ivFavorito.setColorFilter(corFavorito)
    }

    private fun configurarImagem(holder: ViewHolder, veiculo: Veiculo) {
        val arquivoFoto = veiculo.caminhoFoto
            ?.let(::File)
            ?.takeIf { it.exists() }

        val fotoCarregada = arquivoFoto?.let {
            carregarFotoPersonalizada(holder.ivVeiculo, it)
        } ?: false

        if (!fotoCarregada) {
            exibirImagemPadrao(holder, veiculo)
        }
    }

    private fun carregarFotoPersonalizada(imageView: ImageView, arquivoFoto: File): Boolean {
        return try {
            imageView.clearColorFilter()
            imageView.imageTintList = null
            imageView.setPadding(0, 0, 0, 0)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.clipToOutline = true
            imageView.setImageURI(Uri.fromFile(arquivoFoto))

            true
        } catch (e: Exception) {
            false
        }
    }

    private fun exibirImagemPadrao(holder: ViewHolder, veiculo: Veiculo) {
        val imageView = holder.ivVeiculo

        imageView.clipToOutline = false
        imageView.setPadding(
            obterPaddingPadrao(holder),
            obterPaddingPadrao(holder),
            obterPaddingPadrao(holder),
            obterPaddingPadrao(holder)
        )

        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imageView.setColorFilter(COR_IMAGEM_PADRAO)
        imageView.setImageResource(obterIconeVeiculo(veiculo.tipo))
    }

    private fun obterPaddingPadrao(holder: ViewHolder): Int {
        return (PADDING_IMAGEM_DP * holder.itemView.context.resources.displayMetrics.density).toInt()
    }

    private fun obterIconeVeiculo(tipo: String): Int {
        return when (tipo) {
            TIPO_MOTO -> R.drawable.vetor_moto
            TIPO_CAMINHAO -> R.drawable.vetor_caminhao
            else -> R.drawable.vetor_menu_veiculo
        }
    }

    private companion object {
        private const val TEXTO_PLACA_NAO_INFORMADA = "---"

        private const val TIPO_MOTO = "Moto"
        private const val TIPO_CAMINHAO = "Caminhão"

        private const val PADDING_IMAGEM_DP = 16

        private val COR_FAVORITO = Color.parseColor("#f59e0b")
        private val COR_NAO_FAVORITO = Color.parseColor("#94a3b8")
        private val COR_IMAGEM_PADRAO = Color.parseColor("#0d9488")
    }
}