package com.project.projectsegurity

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import com.project.projectsegurity.model.ReporteItem
import okhttp3.*
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream



import android.app.DatePickerDialog
import java.util.Calendar


class HistorialActivity : AppCompatActivity() {

    private lateinit var spinnerModoReporte: Spinner
    private lateinit var spinnerTipo: Spinner
    private lateinit var spinnerPrioridad: Spinner


    private lateinit var edtFechaInicio: EditText
    private lateinit var edtFechaFin: EditText



    private lateinit var btnFiltrar: Button
    private lateinit var btnExportar: Button
    private lateinit var btnSalir: Button

    private lateinit var listView: ListView

    private val listaReportes = mutableListOf<ReporteItem>()
    private val listaFiltrada = mutableListOf<ReporteItem>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        spinnerModoReporte = findViewById(R.id.spinnerModoReporte)
        spinnerTipo = findViewById(R.id.spinnerTipo)
        spinnerPrioridad = findViewById(R.id.spinnerPrioridad)


        edtFechaInicio =
            findViewById(R.id.edtFechaInicio)

        edtFechaFin =
            findViewById(R.id.edtFechaFin)


        btnFiltrar = findViewById(R.id.btnFiltrar)
        btnExportar = findViewById(R.id.btnExportar)
        btnSalir = findViewById(R.id.btnSalir)

        listView = findViewById(R.id.listReportes)












        edtFechaInicio.setOnClickListener {

            val calendario =
                Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->

                    edtFechaInicio.setText(
                        String.format(
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                        )
                    )
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            ).show()
        }






        edtFechaFin.setOnClickListener {

            val calendario =
                Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->

                    edtFechaFin.setText(
                        String.format(
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                        )
                    )
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            ).show()
        }


        // =========================================================
        // 🔥 TIPO DE REPORTES
        // =========================================================
        spinnerModoReporte.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                "Reportes Totales",
                "Mis Reportes"
            )
        )

        // =========================================================
        // 🔥 TIPO DELITO
        // =========================================================
        spinnerTipo.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                "Todos",
                "ROBO",
                "BOTON DE PANICO"
            )
        )

        // =========================================================
        // 🔥 PRIORIDAD
        // =========================================================
        spinnerPrioridad.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                "Todos",
                "Alto",
                "Medio"
            )
        )

        // =========================================================
// 🔥 DESHABILITAR SPINNER PRIORIDAD
// =========================================================
        spinnerPrioridad.isEnabled = false
        spinnerPrioridad.isClickable = false
        spinnerPrioridad.isFocusable = false

        // =========================================================
        // 🔥 AUTOSELECCIÓN PRIORIDAD
        // =========================================================
        spinnerTipo.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {

                    val tipoSeleccionado =
                        parent.getItemAtPosition(position).toString()

                    when (tipoSeleccionado) {

                        "ROBO" -> {

                            val index =
                                (spinnerPrioridad.adapter as ArrayAdapter<String>)
                                    .getPosition("Medio")

                            spinnerPrioridad.setSelection(index)
                        }

                        "BOTON DE PANICO" -> {

                            val index =
                                (spinnerPrioridad.adapter as ArrayAdapter<String>)
                                    .getPosition("Alto")

                            spinnerPrioridad.setSelection(index)
                        }

                        else -> {

                            val index =
                                (spinnerPrioridad.adapter as ArrayAdapter<String>)
                                    .getPosition("Todos")

                            spinnerPrioridad.setSelection(index)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        // =========================================================
        // 🔥 CARGAR REPORTES TOTALES AL INICIO
        // =========================================================
        obtenerReportesBackend(
            //"http://192.168.18.238:8080/api/reportes"
                        "http://projectsecuritypeople-env.eba-k3djm54f.us-east-2.elasticbeanstalk.com/api/reportes"
        )
        btnFiltrar.setOnClickListener {

            val modoReporte =
                spinnerModoReporte.selectedItem.toString()

            val fechaInicio =
                edtFechaInicio.text.toString()

            val fechaFin =
                edtFechaFin.text.toString()

            val endpoint = when {

                // =====================================================
                // REPORTES TOTALES CON FECHAS
                // =====================================================
                modoReporte == "Reportes Totales" &&
                        fechaInicio.isNotEmpty() &&
                        fechaFin.isNotEmpty() -> {

                    "http://projectsecuritypeople-env.eba-k3djm54f.us-east-2.elasticbeanstalk.com/api/reportes/filtrar" +
                            "?fechaInicio=$fechaInicio" +
                            "&fechaFin=$fechaFin"
                }

                // =====================================================
                // MIS REPORTES CON FECHAS
                // =====================================================
                modoReporte == "Mis Reportes" &&
                        fechaInicio.isNotEmpty() &&
                        fechaFin.isNotEmpty() -> {

                    "http://projectsecuritypeople-env.eba-k3djm54f.us-east-2.elasticbeanstalk.com/api/reportes/mis-reportes/filtrar" +
                            "?fechaInicio=$fechaInicio" +
                            "&fechaFin=$fechaFin"
                }

                // =====================================================
                // TODOS LOS REPORTES
                // =====================================================
                modoReporte == "Reportes Totales" -> {

                    "http://projectsecuritypeople-env.eba-k3djm54f.us-east-2.elasticbeanstalk.com/api/reportes"
                }

                // =====================================================
                // SOLO MIS REPORTES
                // =====================================================
                else -> {

                    "http://projectsecuritypeople-env.eba-k3djm54f.us-east-2.elasticbeanstalk.com/api/reportes/mis-reportes"
                }
            }

            obtenerReportesBackend(endpoint)
        }

        // =========================================================
        // 🔥 BOTÓN EXPORTAR PDF
        // =========================================================
        btnExportar.setOnClickListener {

            val tipo =
                spinnerTipo.selectedItem.toString()

            val listaAExportar = when (tipo) {

                "ROBO" -> {
                    listaReportes.filter {
                        it.tiporeporte == "ROBO"
                    }
                }

                "BOTON DE PANICO" -> {
                    listaReportes.filter {
                        it.tiporeporte == "BOTON DE PANICO"
                    }
                }

                else -> listaReportes
            }

            generarPDF(listaAExportar)
        }

        // =========================================================
        // 🔥 BOTÓN SALIR
        // =========================================================
        btnSalir.setOnClickListener {

            val intent =
                Intent(this, MainMenuActivity::class.java)

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }

        // =========================================================
        // 🔥 CLICK LISTVIEW
        // =========================================================
        listView.setOnItemClickListener { _, _, position, _ ->

            val item = listaFiltrada[position]

            val intent =
                Intent(this, DetalleReporteActivity::class.java)

            intent.putExtra(
                "reporte",
                item.descripcion
            )

            intent.putExtra(
                "reporteId",
                item.id
            )

            startActivity(intent)
        }
    }

    // =========================================================
    // 🔥 OBTENER REPORTES
    // =========================================================
    private fun obtenerReportesBackend(
        urlEndpoint: String
    ) {

        val prefs =
            getSharedPreferences(
                "APP_PREFS",
                MODE_PRIVATE
            )

        val token =
            prefs.getString("TOKEN", null)

        val request =
            Request.Builder()
                .url(urlEndpoint)
                .addHeader(
                    "Authorization",
                    "Bearer $token"
                )
                .build()

        OkHttpClient()
            .newCall(request)
            .enqueue(object : Callback {

                override fun onFailure(
                    call: Call,
                    e: IOException
                ) {

                    runOnUiThread {

                        Toast.makeText(
                            applicationContext,
                            "Error backend",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response
                ) {

                    val body =
                        response.body?.string()

                    try {

                        val jsonArray =
                            JSONArray(body)

                        listaReportes.clear()

                        for (i in 0 until jsonArray.length()) {

                            val obj =
                                jsonArray.getJSONObject(i)

                            listaReportes.add(

                                ReporteItem(
                                    obj.getLong("id"),
                                    obj.getString("descripcion"),
                                    obj.getString("tiporeporte")
                                )
                            )
                        }

                        runOnUiThread {

                            val tipo =
                                spinnerTipo.selectedItem.toString()

                            val filtrados = when (tipo) {

                                "ROBO" -> {
                                    listaReportes.filter {
                                        it.tiporeporte == "ROBO"
                                    }
                                }

                                "BOTON DE PANICO" -> {
                                    listaReportes.filter {
                                        it.tiporeporte == "BOTON DE PANICO"
                                    }
                                }

                                else -> listaReportes
                            }

                            mostrarLista(filtrados)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
    }

    // =========================================================
    // 🔥 MOSTRAR LISTA
    // =========================================================
    private fun mostrarLista(
        lista: List<ReporteItem>
    ) {

        listaFiltrada.clear()
        listaFiltrada.addAll(lista)

        val nombres = lista.map {

            "${it.tiporeporte} - ${it.descripcion}"
        }

        listView.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                nombres
            )
    }

    // =========================================================
    // 🔥 GENERAR PDF
    // =========================================================
    private fun generarPDF(
        lista: List<ReporteItem>
    ) {

        Thread {

            try {

                val fileInterno = File(
                    getExternalFilesDir(
                        Environment.DIRECTORY_DOCUMENTS
                    ),
                    "reportes.pdf"
                )

                val document = Document()

                PdfWriter.getInstance(
                    document,
                    FileOutputStream(fileInterno)
                )

                document.open()

                document.add(
                    Paragraph(
                        "REPORTE DE INCIDENTES\n\n"
                    )
                )

                for (reporte in lista) {

                    document.add(
                        Paragraph(
                            "Tipo: ${reporte.tiporeporte}"
                        )
                    )

                    document.add(
                        Paragraph(
                            "Descripción: ${reporte.descripcion}"
                        )
                    )

                    val imagen =
                        obtenerImagen(reporte.id)

                    if (imagen != null) {

                        val img =
                            Image.getInstance(imagen)

                        img.scaleToFit(
                            400f,
                            400f
                        )

                        document.add(img)

                    } else {

                        document.add(
                            Paragraph("Sin imagen")
                        )
                    }

                    document.add(
                        Paragraph(
                            "\n-----------------\n"
                        )
                    )
                }

                document.close()

                val resolver =
                    contentResolver

                val fileName =
                    "reportes_${System.currentTimeMillis()}.pdf"

                val values =
                    ContentValues().apply {

                        put(
                            MediaStore.MediaColumns.DISPLAY_NAME,
                            fileName
                        )

                        put(
                            MediaStore.MediaColumns.MIME_TYPE,
                            "application/pdf"
                        )

                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_DOWNLOADS
                        )
                    }

                val uri =
                    resolver.insert(
                        MediaStore.Files.getContentUri("external"),
                        values
                    )

                val output: OutputStream? =
                    uri?.let {
                        resolver.openOutputStream(it)
                    }

                fileInterno.inputStream().use { input ->

                    output?.use {
                        input.copyTo(it)
                    }
                }

                runOnUiThread {

                    Toast.makeText(
                        this,
                        "PDF descargado en Descargas ✅",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }.start()
    }

    // =========================================================
    // 🔥 OBTENER IMAGEN
    // =========================================================
    private fun obtenerImagen(
        id: Long
    ): ByteArray? {

        return try {

            val prefs =
                getSharedPreferences(
                    "APP_PREFS",
                    MODE_PRIVATE
                )

            val token =
                prefs.getString("TOKEN", "")

            val request =
                Request.Builder()
                    .url(
                        //"http://192.168.18.238:8080/api/reportes/archivo/$id"
                        "http://projectsecuritypeople-env.eba-k3djm54f.us-east-2.elasticbeanstalk.com/api/reportes/archivo/$id"

                    )
                    .addHeader(
                        "Authorization",
                        "Bearer $token"
                    )
                    .build()

            val response =
                OkHttpClient()
                    .newCall(request)
                    .execute()

            if (
                response.isSuccessful &&
                response.body != null
            ) {

                response.body!!.bytes()

            } else null

        } catch (e: Exception) {
            null
        }
    }
}