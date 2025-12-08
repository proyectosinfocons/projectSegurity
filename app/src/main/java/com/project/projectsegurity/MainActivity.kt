package com.project.projectsegurity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.project.projectsegurity.detector.ObjectDetector
import com.project.projectsegurity.detector.VideoAnalyzer
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.logging.HttpLoggingInterceptor
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var etDescripcion: EditText
    private lateinit var btnFoto: Button
    private lateinit var tvFotoCapturada: TextView
    private lateinit var btnRegistrar: Button
    private var map: GoogleMap? = null

    private var ultimaLatitud: Double? = null
    private var ultimaLongitud: Double? = null
    private var ultimoArchivo: File? = null

    private val objectDetector by lazy { ObjectDetector(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        etDescripcion = findViewById(R.id.etDescripcion)
        btnFoto = findViewById(R.id.btnFoto)
        tvFotoCapturada = findViewById(R.id.tvFotoCapturada)
        btnRegistrar = findViewById(R.id.btnRegistrar)

        etDescripcion.isEnabled = false
        etDescripcion.isFocusable = false
        etDescripcion.isCursorVisible = false

        checkAndRequestPermissions()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 🔁 Restaurar estado después de rotar
        if (savedInstanceState != null) {
            val textoGuardado = savedInstanceState.getString("tvFotoCapturada_text", "")
            val visibleGuardado = savedInstanceState.getBoolean("tvFotoCapturada_visible", false)
            if (textoGuardado.isNotEmpty()) {
                tvFotoCapturada.text = textoGuardado
                tvFotoCapturada.visibility = if (visibleGuardado) View.VISIBLE else View.GONE
            }

            val pathArchivo = savedInstanceState.getString("ultimoArchivoPath")
            if (!pathArchivo.isNullOrEmpty()) {
                val file = File(pathArchivo)
                if (file.exists()) {
                    ultimoArchivo = file
                }
            }
        }

        btnFoto.setOnClickListener {
            val opciones = arrayOf("📸 Tomar foto", "🎥 Grabar video")
            AlertDialog.Builder(this)
                .setTitle("Seleccionar tipo de captura")
                .setItems(opciones) { _, which ->
                    when (which) {
                        0 -> {
                            val intent = Intent(this, DetectarActivity::class.java)
                            detectarActivityLauncher.launch(intent)
                        }
                        1 -> {
                            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            videoCaptureLauncher.launch(intent)
                        }
                    }
                }.show()
        }

        btnRegistrar.setOnClickListener {
            val descripcion = etDescripcion.text.toString().trim()
            if (descripcion.isEmpty()) {
                etDescripcion.error = "Por favor, capture una detección antes de registrar"
            } else if (ultimaLatitud == null || ultimaLongitud == null) {
                Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
            } else if (ultimoArchivo == null) {
                Toast.makeText(this, "Archivo no disponible", Toast.LENGTH_SHORT).show()
            } else {
                enviarReporteAlServidor(descripcion, ultimaLatitud!!, ultimaLongitud!!, ultimoArchivo!!)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permisos = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val faltantes = permisos.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (faltantes.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, faltantes.toTypedArray(), 100)
        }
    }

    private val detectarActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val fileName = result.data?.getStringExtra("file_name")
                val filePath = result.data?.getStringExtra("file_path")
                val descripcionAuto = result.data?.getStringExtra("descripcion_auto")

                if (!fileName.isNullOrEmpty()) {
                    tvFotoCapturada.text = "📸 Archivo generado: $fileName"
                    tvFotoCapturada.visibility = View.VISIBLE
                }

                if (!descripcionAuto.isNullOrEmpty()) {
                    etDescripcion.setText(descripcionAuto)
                }

                if (!filePath.isNullOrEmpty()) {
                    val file = File(filePath)
                    if (file.exists()) ultimoArchivo = file
                }
            }
        }

    private val videoCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                if (result.resultCode == Activity.RESULT_OK) {
                    val videoUri: Uri? = result.data?.data
                    if (videoUri != null) {
                        val videoFile = uriToFile(videoUri)
                        if (videoFile != null) {
                            tvFotoCapturada.text = "🎥 Video capturado: ${videoFile.name}"
                            tvFotoCapturada.visibility = View.VISIBLE
                            ultimoArchivo = videoFile

                            lifecycleScope.launch {
                                val analyzer = VideoAnalyzer(this@MainActivity, objectDetector)
                                val results = analyzer.analyzeVideo(videoFile)
                                val delitoDetectado = results.find {
                                    it.label == "robo"
                                }

                                if (delitoDetectado != null) {
                                    etDescripcion.setText(
                                        "Posible ${delitoDetectado.label} detectado con ${
                                            "%.2f".format(delitoDetectado.confidence * 100)
                                        }% de confianza"
                                    )
                                } else {
                                    etDescripcion.setText("No se detectaron actos delictivos en el video.")
                                }
                            }
                        } else {
                            Toast.makeText(this, "⚠️ No se pudo procesar el video", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error procesando video", e)
                Toast.makeText(this, "Error procesando video: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("No se pudo abrir InputStream del URI: $uri")

            val tempFile = File.createTempFile("video_temp", ".mp4", cacheDir)
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }

            Log.i("MainActivity", "Video copiado a: ${tempFile.absolutePath}")
            tempFile
        } catch (e: SecurityException) {
            Log.e("MainActivity", "Permiso denegado al leer URI: $uri", e)
            runOnUiThread {
                Toast.makeText(this, "No se pudo acceder al video (permiso denegado)", Toast.LENGTH_LONG).show()
            }
            null
        } catch (e: Exception) {
            Log.e("MainActivity", "Error convirtiendo URI a archivo", e)
            runOnUiThread {
                Toast.makeText(this, "Error al procesar el video: ${e.message}", Toast.LENGTH_LONG).show()
            }
            null
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
        }
        enableMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map?.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let {
                    ultimaLatitud = it.latitude
                    ultimaLongitud = it.longitude
                    val latLng = LatLng(it.latitude, it.longitude)
                    map?.addMarker(MarkerOptions().position(latLng).title("Tu ubicación"))
                    map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                }
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun enviarReporteAlServidor(descripcion: String, latitud: Double, longitud: Double, tempFile: File) {
        Thread {
            try {
                val fileType = if (tempFile.extension.lowercase() == "mp4") "video/mp4" else "image/jpeg"

                val fileBody = object : RequestBody() {
                    override fun contentType() = fileType.toMediaTypeOrNull()
                    override fun writeTo(sink: BufferedSink) {
                        FileInputStream(tempFile).use { input ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                sink.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                }

                val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("descripcion", descripcion)
                    .addFormDataPart("latitud", latitud.toString())
                    .addFormDataPart("longitud", longitud.toString())
                    .addFormDataPart("archivo", tempFile.name, fileBody)
                    .build()

                val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

                val client = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(180, TimeUnit.SECONDS)
                    .readTimeout(180, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build()

                val request = Request.Builder()
                    .url("http://projectsecuritypeople-env.eba-h4uxw7uz.us-east-1.elasticbeanstalk.com/api/reportes/guardar")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this, "✅ Reporte enviado correctamente", Toast.LENGTH_LONG).show()
                        tvFotoCapturada.text = ""
                        etDescripcion.setText("")
                    } else {
                        Toast.makeText(this, "❌ Error al enviar: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("UPLOAD_ERROR", "Error enviando reporte", e)
                runOnUiThread {
                    Toast.makeText(this, "⚠️ Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("tvFotoCapturada_text", tvFotoCapturada.text.toString())
        outState.putBoolean("tvFotoCapturada_visible", tvFotoCapturada.visibility == View.VISIBLE)
        outState.putString("ultimoArchivoPath", ultimoArchivo?.absolutePath)
    }
}
