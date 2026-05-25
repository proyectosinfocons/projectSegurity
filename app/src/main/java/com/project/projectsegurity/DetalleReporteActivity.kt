package com.project.projectsegurity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.InputStream

class DetalleReporteActivity : AppCompatActivity() {

    private lateinit var tvDescripcion: TextView
    private lateinit var imgReporte: ImageView
    private lateinit var tvSinImagen: TextView

    private lateinit var btnSalir: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle)

        tvDescripcion = findViewById(R.id.tvDescripcion)
        imgReporte = findViewById(R.id.imgReporte)
        tvSinImagen = findViewById(R.id.tvSinImagen)

        // =========================================================
        // 🔥 CAMBIO: INICIALIZAR BOTÓN
        // =========================================================
        btnSalir = findViewById(R.id.btnSalir)
        // =========================================================


        val reporte = intent.getStringExtra("reporte")
        val reporteId = intent.getLongExtra("reporteId", -1)

        tvDescripcion.text = reporte ?: "Sin descripción"

        if (reporteId != -1L) {
            cargarImagen(reporteId)
        } else {
            mostrarSinImagen()
        }

        // =========================================================
        // 🔥🔥🔥 INICIO CAMBIO: ACCIÓN BOTÓN SALIR
        // =========================================================
        btnSalir.setOnClickListener {

            val intent = Intent(this, HistorialActivity::class.java)

            // 🔥 limpia historial para evitar volver atrás
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
        // =========================================================
        // 🔥🔥🔥 FIN CAMBIO
        // =========================================================
    }

    // =========================================================
    // 🔥 CARGAR IMAGEN CON TOKEN
    // =========================================================
    private fun cargarImagen(id: Long) {

        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)

        // =========================================================
        // 🔥 CAMBIO: OBTENER TOKEN
        // =========================================================
        val token = prefs.getString("TOKEN", null)

        val request = Request.Builder()
            .url("http://192.168.18.238:8080/api/reportes/archivo/$id")

            // =========================================================
            // 🔥 CAMBIO: AGREGAR TOKEN
            // =========================================================
            .addHeader("Authorization", "Bearer $token")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: java.io.IOException) {
                runOnUiThread { mostrarSinImagen() }
            }

            override fun onResponse(call: Call, response: Response) {

                if (!response.isSuccessful || response.body == null) {
                    runOnUiThread { mostrarSinImagen() }
                    return
                }

                try {
                    val inputStream: InputStream = response.body!!.byteStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    runOnUiThread {

                        if (bitmap != null) {
                            imgReporte.setImageBitmap(bitmap)
                            imgReporte.visibility = View.VISIBLE
                            tvSinImagen.visibility = View.GONE
                        } else {
                            mostrarSinImagen()
                        }
                    }

                } catch (e: Exception) {
                    runOnUiThread { mostrarSinImagen() }
                }
            }
        })
    }

    // =========================================================
    // 🔥 MOSTRAR TEXTO SI NO HAY IMAGEN
    // =========================================================
    private fun mostrarSinImagen() {
        imgReporte.visibility = View.GONE
        tvSinImagen.visibility = View.VISIBLE
    }
}