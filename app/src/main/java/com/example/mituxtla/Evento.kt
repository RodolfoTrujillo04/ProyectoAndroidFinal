package com.example.mituxtla

import com.google.firebase.Timestamp

data class Evento(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val lugar: String = "",
    val imagen: String = "",
    val fechaInicio: Timestamp? = null,
    val fechaFin: Timestamp? = null
)