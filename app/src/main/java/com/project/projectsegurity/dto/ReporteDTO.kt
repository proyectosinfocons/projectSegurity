package com.project.projectsegurity.dto

// =========================================================
// 🔥 NUEVO MODELO (INICIO CAMBIO)
// 👉 Representa datos reales del backend
// =========================================================
data class ReporteDTO(
    val id: Long,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val fechaRegistro: String,
    val usuarioId: Long?,
    val tipo: String,
    val archivoUrl: String?,
    val tiporeporte: String
)
// =========================================================
// 🔥 FIN CAMBIO
// =========================================================