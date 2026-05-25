//package com.project.projectsegurity
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.telephony.SmsManager
//import android.util.Log
//import android.widget.LinearLayout
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.google.android.gms.location.*
//import okhttp3.*
//import org.json.JSONArray
//import java.io.IOException
//
//class MainMenuActivity : AppCompatActivity() {
//
//    private val PERMISSION_CODE = 1
//
//    // 🔥 INICIO CAMBIO: cliente GPS
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private var ultimaLatitud: Double? = null
//    private var ultimaLongitud: Double? = null
//    // 🔥 FIN CAMBIO
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main_menu)
//
//        // 🔥 INICIO CAMBIO: inicializar GPS
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        // 🔥 FIN CAMBIO
//
//        val layoutReportarDelito = findViewById<LinearLayout>(R.id.layoutReportarDelito)
//        val layoutPerfil = findViewById<LinearLayout>(R.id.layoutPerfil)
//        val layoutSalir = findViewById<LinearLayout>(R.id.layoutSalir)
//        val layoutPanico = findViewById<LinearLayout>(R.id.layoutPanico)
//
//
//        // =====================================================
//        // 🔥 CAMBIO INICIO
//        // 👉 Referencia al layout del mapa
//        // =====================================================
//        val layoutMapa = findViewById<LinearLayout>(R.id.layoutMapa)
//        // 🔥 CAMBIO FIN
//        // =====================================================
//        // 🔥 CAMBIO INICIO
//        // 👉 Referencia al Historial
//        // =====================================================
//        val layoutHistorial = findViewById<LinearLayout>(R.id.layoutHistorial)
//        // 🔥 CAMBIO FIN
//
//        // ===============================
//        // 🔥 LÓGICA ORIGINAL (SE MANTIENE)
//        // ===============================
//        layoutReportarDelito.setOnClickListener {
//            startActivity(Intent(this, MainActivity::class.java))
//        }
//
//        layoutPerfil.setOnClickListener {
//            startActivity(Intent(this, PerfilActivity::class.java))
//        }
//
//        layoutSalir.setOnClickListener {
//            val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
//            prefs.edit().clear().apply()
//
//            val intent = Intent(this, LoginActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//        }
//
//        // =====================================================
//        // 🔥 CAMBIO INICIO
//        // 👉 CLICK PARA ABRIR EL MAPA
//        // =====================================================
//        layoutMapa.setOnClickListener {
//            startActivity(Intent(this, MapActivity::class.java))
//        }
//        // 🔥 CAMBIO FIN
//
//
//        // =====================================================
//        // 🔥 CAMBIO INICIO
//        // 👉 Abrir pantalla de historial (HU10, HU11, HU12)
//        // =====================================================
//        layoutHistorial.setOnClickListener {
//            startActivity(Intent(this, HistorialActivity::class.java))
//        }
//        // ===============================
//        // 🔥 BOTÓN DE PÁNICO
//        // ===============================
//        layoutPanico.setOnClickListener {
//
//            if (!tienePermisos()) {
//                solicitarPermisos()
//                return@setOnClickListener
//            }
//
//            // 🔥 INICIO CAMBIO: obtener ubicación real antes de ejecutar
//            obtenerUbicacionYActivarPanico()
//            // 🔥 FIN CAMBIO
//        }
//    }
//
//    // ===============================
//    // 🔥 VALIDAR PERMISOS
//    // ===============================
//    private fun tienePermisos(): Boolean {
//        val sms = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
//        val location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//
//        return sms == PackageManager.PERMISSION_GRANTED &&
//                location == PackageManager.PERMISSION_GRANTED
//    }
//
//    // ===============================
//    // 🔥 SOLICITAR PERMISOS
//    // ===============================
//    private fun solicitarPermisos() {
//        ActivityCompat.requestPermissions(
//            this,
//            arrayOf(
//                Manifest.permission.SEND_SMS,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ),
//            PERMISSION_CODE
//        )
//    }
//
//    // ===============================
//    // 🔥 RESPUESTA DE PERMISOS
//    // ===============================
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == PERMISSION_CODE) {
//            if (grantResults.isNotEmpty() &&
//                grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
//
//                Toast.makeText(this, "Permisos concedidos ✅", Toast.LENGTH_SHORT).show()
//
//                // 🔥 IMPORTANTE: ahora sí obtiene ubicación
//                obtenerUbicacionYActivarPanico()
//
//            } else {
//                Toast.makeText(this, "Permisos denegados ❌", Toast.LENGTH_LONG).show()
//            }
//        }
//    }
//
//    // ===============================
//    // 🔥 INICIO CAMBIO: OBTENER GPS REAL
//    // ===============================
//    @SuppressLint("MissingPermission")
//    private fun obtenerUbicacionYActivarPanico() {
//
//        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//
//            if (location != null) {
//
//                ultimaLatitud = location.latitude
//                ultimaLongitud = location.longitude
//
//                Log.d("GPS", "Lat: $ultimaLatitud - Lng: $ultimaLongitud")
//
//                activarPanico()
//
//            } else {
//                Toast.makeText(this, "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//    // ===============================
//    // 🔥 FIN CAMBIO
//    // ===============================
//
//    // ===============================
//    // 🔥 FUNCIÓN PRINCIPAL
//    // ===============================
//    private fun activarPanico() {
//
//        val latitud = ultimaLatitud ?: return
//        val longitud = ultimaLongitud ?: return
//
//        val mensaje = "🚨 ALERTA DE PÁNICO 🚨\nUbicación:\nhttps://www.google.com/maps?q=$latitud,$longitud"
//
//        // 🔥 obtener contactos
//        obtenerContactos { contactos ->
//
//            enviarSMS(contactos, mensaje)
//
//            // 🔥 enviar al backend como reporte
//            enviarReporte(mensaje, latitud, longitud)
//        }
//    }
//
//    // ===============================
//    // 🔥 OBTENER CONTACTOS DESDE API
//    // ===============================
//    private fun obtenerContactos(callback: (List<String>) -> Unit) {
//
//        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
//        val token = prefs.getString("TOKEN", null)
//
//        val request = Request.Builder()
//            .url(
//                "http://192.168.18.238:8080/api/contactos"
//
//            )
//            .addHeader("Authorization", "Bearer $token")
//            .get()
//            .build()
//
//        OkHttpClient().newCall(request).enqueue(object : Callback {
//
//            override fun onFailure(call: Call, e: IOException) {
//                runOnUiThread {
//                    Toast.makeText(applicationContext, "Error obteniendo contactos", Toast.LENGTH_LONG).show()
//                }
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//
//                val lista = mutableListOf<String>()
//
//                try {
//                    val jsonArray = JSONArray(response.body?.string())
//
//                    for (i in 0 until jsonArray.length()) {
//                        val obj = jsonArray.getJSONObject(i)
//                        lista.add(obj.getString("telefono"))
//                    }
//
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//
//                callback(lista)
//            }
//        })
//    }
//
//    // ===============================
//    // 🔥 ENVIAR SMS
//    // ===============================
//    private fun enviarSMS(contactos: List<String>, mensaje: String) {
//
//        try {
//            val smsManager = SmsManager.getDefault()
//
//            for (numero in contactos) {
//
//                val partes = smsManager.divideMessage(mensaje)
//                smsManager.sendMultipartTextMessage(numero, null, partes, null, null)
//            }
//
//            runOnUiThread {
//                Toast.makeText(this, "SMS enviados 🚨", Toast.LENGTH_LONG).show()
//            }
//
//        } catch (e: SecurityException) {
//            e.printStackTrace()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    // ===============================
//    // 🔥 ENVIAR REPORTE AL BACKEND
//    // ===============================
//    private fun enviarReporte(descripcion: String, lat: Double, lng: Double) {
//
//        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
//        val token = prefs.getString("TOKEN", null)
//
//        val body = FormBody.Builder()
//            .add("descripcion", descripcion)
//            .add("latitud", lat.toString())
//            .add("longitud", lng.toString())
//            .build()
//
//        val request = Request.Builder()
//            .url("http://192.168.18.238:8080/api/reportes/guardar-con-token")
//            .addHeader("Authorization", "Bearer $token")
//            .post(body)
//            .build()
//
//        OkHttpClient().newCall(request).enqueue(object : Callback {
//
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e("PANICO", "Error reporte", e)
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                Log.d("PANICO", "Reporte enviado correctamente")
//            }
//        })
//    }
//}


package com.project.projectsegurity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class MainMenuActivity : AppCompatActivity() {

    private val PERMISSION_CODE = 1

    // =====================================================
    // 🔥 BASE URL
    // =====================================================
    private val BASE_URL =
        "http://192.168.18.238:8080"
        //"https://appalertacomunitaria.com"
    // 🔥 INICIO CAMBIO: cliente GPS
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var ultimaLatitud: Double? = null
    private var ultimaLongitud: Double? = null
    // 🔥 FIN CAMBIO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // 🔥 INICIO CAMBIO: inicializar GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // 🔥 FIN CAMBIO

        val layoutReportarDelito = findViewById<LinearLayout>(R.id.layoutReportarDelito)
        val layoutPerfil = findViewById<LinearLayout>(R.id.layoutPerfil)
        val layoutSalir = findViewById<LinearLayout>(R.id.layoutSalir)
        val layoutPanico = findViewById<LinearLayout>(R.id.layoutPanico)

        // =====================================================
        // 🔥 CAMBIO INICIO
        // 👉 Referencia al layout del mapa
        // =====================================================
        val layoutMapa = findViewById<LinearLayout>(R.id.layoutMapa)
        // 🔥 CAMBIO FIN

        // =====================================================
        // 🔥 CAMBIO INICIO
        // 👉 Referencia al Historial
        // =====================================================
        val layoutHistorial = findViewById<LinearLayout>(R.id.layoutHistorial)
        // 🔥 CAMBIO FIN

        // ===============================
        // 🔥 LÓGICA ORIGINAL (SE MANTIENE)
        // ===============================
        layoutReportarDelito.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        layoutPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        layoutSalir.setOnClickListener {
            val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
            prefs.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // =====================================================
        // 🔥 CAMBIO INICIO
        // 👉 CLICK PARA ABRIR EL MAPA
        // =====================================================
        layoutMapa.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
        // 🔥 CAMBIO FIN

        // =====================================================
        // 🔥 CAMBIO INICIO
        // 👉 Abrir pantalla de historial (HU10, HU11, HU12)
        // =====================================================
        layoutHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        // ===============================
        // 🔥 BOTÓN DE PÁNICO
        // ===============================
        layoutPanico.setOnClickListener {

            if (!tienePermisos()) {
                solicitarPermisos()
                return@setOnClickListener
            }

            // 🔥 INICIO CAMBIO: obtener ubicación real antes de ejecutar
            obtenerUbicacionYActivarPanico()
            // 🔥 FIN CAMBIO
        }
    }

    // ===============================
    // 🔥 VALIDAR PERMISOS
    // ===============================
    private fun tienePermisos(): Boolean {
        val sms = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
        val location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        return sms == PackageManager.PERMISSION_GRANTED &&
                location == PackageManager.PERMISSION_GRANTED
    }

    // ===============================
    // 🔥 SOLICITAR PERMISOS
    // ===============================
    private fun solicitarPermisos() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_CODE
        )
    }

    // ===============================
    // 🔥 RESPUESTA DE PERMISOS
    // ===============================
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {

                Toast.makeText(this, "Permisos concedidos ✅", Toast.LENGTH_SHORT).show()

                // 🔥 IMPORTANTE: ahora sí obtiene ubicación
                obtenerUbicacionYActivarPanico()

            } else {
                Toast.makeText(this, "Permisos denegados ❌", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ===============================
    // 🔥 INICIO CAMBIO: OBTENER GPS REAL
    // ===============================
    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionYActivarPanico() {

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->

            if (location != null) {

                ultimaLatitud = location.latitude
                ultimaLongitud = location.longitude

                Log.d("GPS", "Lat: $ultimaLatitud - Lng: $ultimaLongitud")

                activarPanico()

            } else {
                Toast.makeText(this, "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ===============================
    // 🔥 FIN CAMBIO
    // ===============================

    // ===============================
    // 🔥 FUNCIÓN PRINCIPAL
    // ===============================
    private fun activarPanico() {

        val latitud = ultimaLatitud ?: return
        val longitud = ultimaLongitud ?: return

        val mensaje = "🚨 ALERTA DE PÁNICO 🚨\nUbicación:\nhttps://www.google.com/maps?q=$latitud,$longitud"

        // 🔥 obtener contactos
        obtenerContactos { contactos ->

            enviarSMS(contactos, mensaje)

            // 🔥 enviar al backend como reporte
            enviarReporte(mensaje, latitud, longitud)
        }
    }

    // ===============================
    // 🔥 OBTENER CONTACTOS DESDE API
    // ===============================
    private fun obtenerContactos(callback: (List<String>) -> Unit) {

        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val token = prefs.getString("TOKEN", null)

        val request = Request.Builder()
            .url(
                "$BASE_URL/api/contactos"
            )
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error obteniendo contactos", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val lista = mutableListOf<String>()

                try {
                    val jsonArray = JSONArray(response.body?.string())

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        lista.add(obj.getString("telefono"))
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                callback(lista)
            }
        })
    }

    // ===============================
    // 🔥 ENVIAR SMS
    // ===============================
    private fun enviarSMS(contactos: List<String>, mensaje: String) {

        try {
            val smsManager = SmsManager.getDefault()

            for (numero in contactos) {

                val partes = smsManager.divideMessage(mensaje)
                smsManager.sendMultipartTextMessage(numero, null, partes, null, null)
            }

            runOnUiThread {
                Toast.makeText(this, "SMS enviados 🚨", Toast.LENGTH_LONG).show()
            }

        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ===============================
    // 🔥 ENVIAR REPORTE AL BACKEND
    // ===============================
    private fun enviarReporte(descripcion: String, lat: Double, lng: Double) {

        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val token = prefs.getString("TOKEN", null)

        val body = FormBody.Builder()
            .add("descripcion", descripcion)
            .add("latitud", lat.toString())
            .add("longitud", lng.toString())
            .build()

        val request = Request.Builder()
            .url("$BASE_URL/api/reportes/guardar-con-token")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("PANICO", "Error reporte", e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("PANICO", "Reporte enviado correctamente")
            }
        })
    }
}