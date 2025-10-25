package com.project.projectsegurity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var etDescripcion: EditText
    private lateinit var btnFoto: Button
    private lateinit var tvFotoCapturada: TextView
    private lateinit var btnRegistrar: Button
    private var map: GoogleMap? = null

    // Variables para conservar estado
    private var savedDescripcion: String? = null
    private var savedFotoTexto: String? = null
    private var savedFotoVisible: Boolean = false
    private var ultimaLatitud: Double? = null
    private var ultimaLongitud: Double? = null
    private var ultimoArchivo: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        etDescripcion = findViewById(R.id.etDescripcion)
        btnFoto = findViewById(R.id.btnFoto)
        tvFotoCapturada = findViewById(R.id.tvFotoCapturada)
        btnRegistrar = findViewById(R.id.btnRegistrar)

        // Bloquear edición manual
        etDescripcion.isEnabled = false
        etDescripcion.isFocusable = false
        etDescripcion.isCursorVisible = false

        // Restaurar estado
        if (savedInstanceState != null) {
            savedDescripcion = savedInstanceState.getString("descripcion_text")
            savedFotoTexto = savedInstanceState.getString("foto_text")
            savedFotoVisible = savedInstanceState.getBoolean("foto_visible", false)

            etDescripcion.setText(savedDescripcion)
            tvFotoCapturada.text = savedFotoTexto
            tvFotoCapturada.visibility = if (savedFotoVisible) View.VISIBLE else View.GONE
        }

        // Mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Abrir cámara
        btnFoto.setOnClickListener {
            val intent = Intent(this, DetectarActivity::class.java)
            detectarActivityLauncher.launch(intent)
        }

        // Registrar (envío de datos al servidor)
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

    // Guardar estado
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("descripcion_text", etDescripcion.text.toString())
        outState.putString("foto_text", tvFotoCapturada.text.toString())
        outState.putBoolean("foto_visible", tvFotoCapturada.visibility == View.VISIBLE)
    }

    // Recibir resultado desde DetectarActivity
    private val detectarActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val fileName = result.data?.getStringExtra("file_name")
                val filePath = result.data?.getStringExtra("file_path")
                val descripcionAuto = result.data?.getStringExtra("descripcion_auto")

                if (!fileName.isNullOrEmpty()) {
                     val mensaje =
                        "📸 Archivo generado correctamente:\n$fileName"
                    tvFotoCapturada.text = mensaje
                    tvFotoCapturada.visibility = View.VISIBLE
                }

                if (!descripcionAuto.isNullOrEmpty()) {
                    etDescripcion.setText(descripcionAuto)
                }

                // Guardar referencia al archivo real
                if (!filePath.isNullOrEmpty()) {
                    val file = File(filePath)
                    if (file.exists()) ultimoArchivo = file
                }
            }
        }

    // Mapa
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
        }
        enableMyLocation()
    }

    // Habilitar ubicación
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

    // 🚀 Enviar reporte al backend con OkHttp
    private fun enviarReporteAlServidor(descripcion: String, latitud: Double, longitud: Double, tempFile: File) {
        Thread {
            try {
                val fileBody: RequestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())

                val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("descripcion", descripcion)
                    .addFormDataPart("latitud", latitud.toString())
                    .addFormDataPart("longitud", longitud.toString())
                    .addFormDataPart("archivo", tempFile.name, fileBody)
                    .build()

                val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
                val client = OkHttpClient.Builder().addInterceptor(logging).build()

                // ⚠️ Asegúrate de usar la IP de tu PC o servidor real
                val request = Request.Builder()
                    //.url("http://192.168.18.238:8086/api/reportes/guardar")
                    .url("http://projectsecuritypeople-env-1.eba-jum2mh2y.us-east-1.elasticbeanstalk.com/api/reportes/guardar")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this, "✅ Reporte enviado correctamente al servidor", Toast.LENGTH_LONG).show()
                        tvFotoCapturada.text=""
                        etDescripcion.setText("")
                    } else {
                        Toast.makeText(this, "❌ Error al enviar: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("UPLOAD_ERROR", "Error enviando reporte", e)
                runOnUiThread {
                    Toast.makeText(this, "⚠️ Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
