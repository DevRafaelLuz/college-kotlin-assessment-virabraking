package com.example.virabraking

data class Veiculo(
    val id: Int = 0,
    val tipo: String,
    val apelido: String,
    val marca: String,
    val modelo: String,
    val ano: Int,
    val placa: String,
    val quilometragem: Int
)