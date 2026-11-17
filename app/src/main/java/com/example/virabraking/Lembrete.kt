package com.example.virabraking

data class Lembrete(
    val id: Int = 0,
    val veiculoId: Int? = null,
    val titulo: String,
    val descricao: String = "",
    val data: String, // yyyy-MM-dd
    val hora: String = "12:00", // HH:mm
    val veiculoApelido: String? = null
)
