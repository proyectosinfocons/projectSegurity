package com.project.projectsegurity

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import com.project.projectsegurity.model.ReporteItem

// 🔥🔥🔥 INICIO CAMBIO: IMPORTS PARA EXPORTAR PDF
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
// 🔥🔥🔥 FIN CAMBIO

class HistorialActivity : AppCompatActivity() {

    private lateinit var spinnerTipo: Spinner
    private lateinit var spinnerPrioridad: Spinner
    private lateinit var btnFiltrar: Button
    private lateinit var btnExportar: Button
    private lateinit var listView: ListView

    private lateinit var btnSalir: Button

    private val listaReportes = mutableListOf<ReporteItem>()
    private val listaFiltrada = mutableListOf<ReporteItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        spinnerTipo = findViewById(R.id.spinnerTipo)
        spinnerPrioridad = findViewById(R.id.spinnerPrioridad)
        btnFiltrar = findViewById(R.id.btnFiltrar)
        btnExportar = findViewById(R.id.btnExportar)
        listView = findViewById(R.id.listReportes)

        // 🔥 CAMBIO INICIO
        btnSalir = findViewById(R.id.btnSalir)
// 🔥 CAMBIO FIN


        spinnerTipo.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Todos", "ROBO", "BOTON DE PANICO")
        )

        spinnerPrioridad.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Todos", "Alto", "Medio")
        )

        // =========================================================
        // 🔥🔥🔥 INICIO CAMBIO: AUTOSELECCIÓN DE PRIORIDAD
        // =========================================================
        spinnerTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {

                val tipoSeleccionado = parent.getItemAtPosition(position).toString()

                when (tipoSeleccionado) {

                    "ROBO" -> {
                        val index = (spinnerPrioridad.adapter as ArrayAdapter<String>)
                            .getPosition("Medio")
                        spinnerPrioridad.setSelection(index)
                    }

                    "BOTON DE PANICO" -> {
                        val index = (spinnerPrioridad.adapter as ArrayAdapter<String>)
                            .getPosition("Alto")
                        spinnerPrioridad.setSelection(index)
                    }

                    else -> {
                        val index = (spinnerPrioridad.adapter as ArrayAdapter<String>)
                            .getPosition("Todos")
                        spinnerPrioridad.setSelection(index)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        // =========================================================
        // 🔥🔥🔥 FIN CAMBIO
        // =========================================================

        obtenerReportesBackend()

        btnFiltrar.setOnClickListener {

            val tipo = spinnerTipo.selectedItem.toString()

            val filtrados = when (tipo) {
                "ROBO" -> listaReportes.filter { it.tiporeporte == "ROBO" }
                "BOTON DE PANICO" -> listaReportes.filter { it.tiporeporte == "BOTON DE PANICO" }
                else -> listaReportes
            }

            mostrarLista(filtrados)
        }

        // 🔥 CAMBIO INICIO
        btnSalir.setOnClickListener {

            val intent = Intent(this, MainMenuActivity::class.java)

            // 🔥 IMPORTANTE: limpia el historial para que no regrese con "back"
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
// 🔥 CAMBIO FIN


        listView.setOnItemClickListener { _, _, position, _ ->

            val item = listaFiltrada[position]

            val intent = Intent(this, DetalleReporteActivity::class.java)

            intent.putExtra("reporte", item.descripcion)
            intent.putExtra("reporteId", item.id)

            startActivity(intent)
        }

        // =========================================================
        // 🔥🔥🔥 INICIO CAMBIO: BOTÓN EXPORTAR (AGREGADO)
        // =========================================================
        btnExportar.setOnClickListener {

            val tipo = spinnerTipo.selectedItem.toString()

            val listaAExportar = when (tipo) {
                "ROBO" -> listaReportes.filter { it.tiporeporte == "ROBO" }
                "BOTON DE PANICO" -> listaReportes.filter { it.tiporeporte == "BOTON DE PANICO" }
                else -> listaReportes
            }

            generarPDF(listaAExportar)
        }
        // =========================================================
        // 🔥🔥🔥 FIN CAMBIO
        // =========================================================
    }

    private fun obtenerReportesBackend() {

        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val token = prefs.getString("TOKEN", null)

        val request = Request.Builder()
            .url("http://192.168.18.238:8080/api/reportes/mis-reportes")
            .addHeader("Authorization", "Bearer $token")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error backend", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val body = response.body?.string()

                try {
                    val jsonArray = JSONArray(body)

                    listaReportes.clear()

                    for (i in 0 until jsonArray.length()) {

                        val obj = jsonArray.getJSONObject(i)

                        listaReportes.add(
                            ReporteItem(
                                obj.getLong("id"),
                                obj.getString("descripcion"),
                                obj.getString("tiporeporte")
                            )
                        )
                    }

                    runOnUiThread {
                        mostrarLista(listaReportes)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun mostrarLista(lista: List<ReporteItem>) {

        listaFiltrada.clear()
        listaFiltrada.addAll(lista)

        val nombres = lista.map {
            "${it.tiporeporte} - ${it.descripcion}"
        }

        listView.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            nombres
        )
    }

    // =========================================================
    // 🔥🔥🔥 INICIO CAMBIO: GENERAR PDF
    // =========================================================
    private fun generarPDF(lista: List<ReporteItem>) {

        Thread {
            try {

                val fileInterno = File(
                    getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "reportes.pdf"
                )

                val document = Document()
                PdfWriter.getInstance(document, FileOutputStream(fileInterno))
                document.open()

                document.add(Paragraph("REPORTE DE INCIDENTES\n\n"))

                for (reporte in lista) {

                    document.add(Paragraph("Tipo: ${reporte.tiporeporte}"))
                    document.add(Paragraph("Descripción: ${reporte.descripcion}"))

                    val imagen = obtenerImagen(reporte.id)

                    if (imagen != null) {
                        val img = Image.getInstance(imagen)
                        img.scaleToFit(400f, 400f)
                        document.add(img)
                    } else {
                        document.add(Paragraph("Sin imagen"))
                    }

                    document.add(Paragraph("\n-----------------\n"))
                }

                document.close()

                val resolver = contentResolver
                val fileName = "reportes_${System.currentTimeMillis()}.pdf"

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)

                val output: OutputStream? = uri?.let { resolver.openOutputStream(it) }

                fileInterno.inputStream().use { input ->
                    output?.use { input.copyTo(it) }
                }

                runOnUiThread {
                    Toast.makeText(this, "PDF descargado en Descargas ✅", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
    // =========================================================
    // 🔥🔥🔥 FIN CAMBIO
    // =========================================================

    // =========================================================
    // 🔥🔥🔥 INICIO CAMBIO: OBTENER IMAGEN
    // =========================================================
    private fun obtenerImagen(id: Long): ByteArray? {

        return try {
            val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
            val token = prefs.getString("TOKEN", "")

            val request = Request.Builder()
                .url("http://192.168.18.238:8080/api/reportes/archivo/$id")
                .addHeader("Authorization", "Bearer $token")
                .build()

            val response = OkHttpClient().newCall(request).execute()

            if (response.isSuccessful && response.body != null) {
                response.body!!.bytes()
            } else null

        } catch (e: Exception) {
            null
        }
    }
    // =========================================================
    // 🔥🔥🔥 FIN CAMBIO
    // =========================================================
}