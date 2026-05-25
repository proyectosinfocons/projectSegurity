package com.project.projectsegurity.model

data class ContactoEmergencia(
    val id: Long,
    val nombre: String,
    val telefono: String,
    val apellido: String,
    val relacion: String,
    val correo: String
)