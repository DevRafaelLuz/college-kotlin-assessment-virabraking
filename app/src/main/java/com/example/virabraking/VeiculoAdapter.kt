package com.example.virabraking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color

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

        holder.tvApelido.text = veiculo.apelido
        holder.tvModelo.text = "${veiculo.marca} ${veiculo.modelo}".trim()
        holder.tvPlaca.text = veiculo.placa.ifEmpty { "---" }
        holder.tvKm.text = "${veiculo.quilometragem} km"

        holder.itemView.setOnClickListener {
            aoClicar(veiculo)
        }
        holder.itemView.setOnLongClickListener {
            aoSegurar(veiculo)
            true
        }

        if (veiculo.favorito) {
            holder.ivFavorito.setImageResource(android.R.drawable.btn_star_big_on)
            holder.ivFavorito.setColorFilter(Color.parseColor("#f59e0b"))
        } else {
            holder.ivFavorito.setImageResource(android.R.drawable.btn_star_big_off)
            holder.ivFavorito.setColorFilter(Color.parseColor("#94a3b8"))
        }
        holder.ivFavorito.setOnClickListener {
            aoFavoritar(veiculo)
        }

        val icone = when (veiculo.tipo) {
            "Moto" -> R.drawable.vetor_moto
            "Caminhão" -> R.drawable.vetor_caminhao
            else -> R.drawable.vetor_menu_veiculo
        }
        holder.ivVeiculo.setImageResource(icone)
    }

    override fun getItemCount(): Int = veiculos.size
}