package com.example.virabraking

data class Servico(
    val id: Int = 0,
    val veiculoId: Int,
    val tipo: String,
    val data: String,
    val quilometragem: Int,
    val valor: Double,
    val oficina: String,
    val observacoes: String
)