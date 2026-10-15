package com.example.virabraking

data class RegistroAbastecimento(
    val id: Int = 0,
    val veiculoId: Int,
    val litros: Double,
    val valorLitro: Double,
    val valorTotal: Double,
    val quilometragem: Int,
    val posto: String,
    val data: String
)