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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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

    // 🔥 CAMBIO 1: NUEVO BOTÓN SALIR
    private lateinit var btnSalir: Button

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

        // 🔥 CAMBIO 2: INICIALIZAR BOTÓN
        btnSalir = findViewById(R.id.btnSalir)

        etDescripcion.isEnabled = false
        etDescripcion.isFocusable = false
        etDescripcion.isCursorVisible = false

        checkAndRequestPermissions()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 🔥 CAMBIO 3: EVENTO DEL BOTÓN SALIR
        btnSalir.setOnClickListener {

            val intent = Intent(this, MainMenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }


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

//        btnFoto.setOnClickListener {
//            val opciones = arrayOf("📸 Tomar foto", "🎥 Grabar video")
//            AlertDialog.Builder(this)
//                .setTitle("Seleccionar tipo de captura")
//                .setItems(opciones) { _, which ->
//                    when (which) {
//                        0 -> {
//                            val intent = Intent(this, DetectarActivity::class.java)
//                            detectarActivityLauncher.launch(intent)
//                        }
//                        1 -> {
//                            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
//                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                            videoCaptureLauncher.launch(intent)
//                        }
//                    }
//                }.show()
//        }


        btnFoto.setOnClickListener {
                            val intent = Intent(this, DetectarActivity::class.java)
                            detectarActivityLauncher.launch(intent)
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

            val locationRequest = LocationRequest.create().apply {
                interval = 5000
                fastestInterval = 2000
                priority = Priority.PRIORITY_HIGH_ACCURACY
            }

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {

                    val location = locationResult.lastLocation

                    if (location != null) {

                        ultimaLatitud = location.latitude
                        ultimaLongitud = location.longitude

                        Log.d("GPS", "Lat: $ultimaLatitud - Lon: $ultimaLongitud")

                        val latLng = LatLng(location.latitude, location.longitude)

                        map?.clear()
                        map?.addMarker(MarkerOptions().position(latLng).title("Tu ubicación"))
                        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

                        // 🔥 IMPORTANTE: dejamos de escuchar para ahorrar batería
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )

        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun enviarReporteAlServidor(
        descripcion: String,
        latitud: Double,
        longitud: Double,
        file: File
    ) {

        Thread {
            try {

                /* =========================================================
                   🔥 CAMBIO 1: OBTENER TOKEN JWT
                   Porque el backend ahora usa Spring Security
                ========================================================= */
                val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                val token = prefs.getString("TOKEN", null)

                if (token == null) {
                    runOnUiThread {
                        Toast.makeText(this, "No autenticado", Toast.LENGTH_LONG).show()
                    }
                    return@Thread
                }

                val fileBody = RequestBody.create(
                    "application/octet-stream".toMediaTypeOrNull(),
                    file
                )

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("descripcion", descripcion)
                    .addFormDataPart("latitud", latitud.toString())
                    .addFormDataPart("longitud", longitud.toString())
                    .addFormDataPart("archivo", file.name, fileBody)
                    .build()

                val client = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build()

                /* =========================================================
                   🔥 CAMBIO 2: URL LOCAL + AUTHORIZATION HEADER
                   - localhost en Android = 10.0.2.2 (emulador)
                   - se agrega Bearer token
                ========================================================= */
                val request = Request.Builder()
                    .url(
                        "http://192.168.18.238:8080/api/reportes/guardar"
                         // "https://appalertacomunitaria.com/api/reportes/guardar"
                    )
                    .addHeader("Authorization", "Bearer $token")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this, "Reporte enviado ✅", Toast.LENGTH_LONG).show()

                        // limpiar descripción
                        etDescripcion.setText("")

                        // limpiar texto del archivo
                        tvFotoCapturada.text = ""

                        // ocultar el texto
                        tvFotoCapturada.visibility = View.GONE

                        // limpiar archivo seleccionado
                        ultimoArchivo = null

                    } else {
                        Toast.makeText(this, "Error: ${response.code}", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {

                /* =========================================================
                   🔥 CAMBIO 3: MANEJO DE ERRORES
                   Evita que la app se cierre (tu problema original)
                ========================================================= */
                Log.e("ERROR_ENVIO", e.message ?: "")

                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
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
