package com.project.projectsegurity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
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
        "http://projectsecuritypeople-env.eba-k3djm54f.us-east-2.elasticbeanstalk.com"

    // =====================================================
    // 🔥 GPS
    // =====================================================
    private lateinit var fusedLocationClient:
            FusedLocationProviderClient

    private var ultimaLatitud: Double? = null
    private var ultimaLongitud: Double? = null

    // =====================================================
    // 🔥 BADGE
    // =====================================================
    private lateinit var tvBadgeNotificaciones: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main_menu)

        // =====================================================
        // 🔥 GPS
        // =====================================================
        fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(this)

        // =====================================================
        // 🔥 REFERENCIAS
        // =====================================================
        val layoutReportarDelito =
            findViewById<LinearLayout>(
                R.id.layoutReportarDelito
            )

        val layoutPerfil =
            findViewById<LinearLayout>(
                R.id.layoutPerfil
            )

        val layoutSalir =
            findViewById<LinearLayout>(
                R.id.layoutSalir
            )

        val layoutPanico =
            findViewById<LinearLayout>(
                R.id.layoutPanico
            )

        val layoutMapa =
            findViewById<LinearLayout>(
                R.id.layoutMapa
            )

        val layoutHistorial =
            findViewById<LinearLayout>(
                R.id.layoutHistorial
            )

        val layoutNotificaciones =
            findViewById<LinearLayout>(
                R.id.layoutNotificaciones
            )

        tvBadgeNotificaciones =
            findViewById(
                R.id.tvBadgeNotificaciones
            )

        // =====================================================
        // 🔥 ACTUALIZAR BADGE
        // =====================================================
        actualizarBadge()

        // =====================================================
        // 🔥 REPORTAR DELITO
        // =====================================================
        layoutReportarDelito.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )
        }

        // =====================================================
        // 🔥 PERFIL
        // =====================================================
        layoutPerfil.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    PerfilActivity::class.java
                )
            )
        }

        // =====================================================
        // 🔥 MAPA
        // =====================================================
        layoutMapa.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    MapActivity::class.java
                )
            )
        }

        // =====================================================
        // 🔥 HISTORIAL
        // =====================================================
        layoutHistorial.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    HistorialActivity::class.java
                )
            )
        }

        // =====================================================
        // 🔥 NOTIFICACIONES
        // =====================================================
        layoutNotificaciones.setOnClickListener {

            // 🔥 limpiar contador
            val prefs =
                getSharedPreferences(
                    "APP_PREFS",
                    MODE_PRIVATE
                )

            prefs.edit()
                .putInt(
                    "CANTIDAD_ALERTAS",
                    0
                )
                .apply()

            actualizarBadge()

            startActivity(
                Intent(
                    this,
                    NotificacionesActivity::class.java
                )
            )
        }

        // =====================================================
        // 🔥 CERRAR SESIÓN
        // =====================================================
        layoutSalir.setOnClickListener {

            val prefs =
                getSharedPreferences(
                    "APP_PREFS",
                    MODE_PRIVATE
                )

            prefs.edit()
                .clear()
                .apply()

            val intent =
                Intent(
                    this,
                    LoginActivity::class.java
                )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }

        // =====================================================
        // 🔥 BOTÓN PÁNICO
        // =====================================================
        layoutPanico.setOnClickListener {

            if (!tienePermisos()) {

                solicitarPermisos()

                return@setOnClickListener
            }

            obtenerUbicacionYActivarPanico()
        }
    }

    // =====================================================
    // 🔥 ACTUALIZAR BADGE
    // =====================================================
    private fun actualizarBadge() {

        val prefs =
            getSharedPreferences(
                "APP_PREFS",
                MODE_PRIVATE
            )

        val cantidad =
            prefs.getInt(
                "CANTIDAD_ALERTAS",
                0
            )

        if (cantidad > 0) {

            tvBadgeNotificaciones.visibility =
                View.VISIBLE

            tvBadgeNotificaciones.text =
                cantidad.toString()

        } else {

            tvBadgeNotificaciones.visibility =
                View.GONE
        }
    }

    // =====================================================
    // 🔥 REFRESCAR BADGE
    // =====================================================
    override fun onResume() {

        super.onResume()

        actualizarBadge()
    }

    // =====================================================
    // 🔥 VALIDAR PERMISOS
    // =====================================================
    private fun tienePermisos(): Boolean {

        val sms =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            )

        val location =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        return sms ==
                PackageManager.PERMISSION_GRANTED &&
                location ==
                PackageManager.PERMISSION_GRANTED
    }

    // =====================================================
    // 🔥 SOLICITAR PERMISOS
    // =====================================================
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

    // =====================================================
    // 🔥 RESPUESTA PERMISOS
    // =====================================================
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == PERMISSION_CODE) {

            if (
                grantResults.isNotEmpty() &&
                grantResults.all {
                    it == PackageManager.PERMISSION_GRANTED
                }
            ) {

                Toast.makeText(
                    this,
                    "Permisos concedidos ✅",
                    Toast.LENGTH_SHORT
                ).show()

                obtenerUbicacionYActivarPanico()

            } else {

                Toast.makeText(
                    this,
                    "Permisos denegados ❌",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // =====================================================
    // 🔥 OBTENER GPS
    // =====================================================
    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionYActivarPanico() {

        fusedLocationClient
            .lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {

                    ultimaLatitud =
                        location.latitude

                    ultimaLongitud =
                        location.longitude

                    activarPanico()

                } else {

                    Toast.makeText(
                        this,
                        "No se pudo obtener ubicación",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // =====================================================
    // 🔥 ACTIVAR PÁNICO
    // =====================================================
    private fun activarPanico() {

        val latitud =
            ultimaLatitud ?: return

        val longitud =
            ultimaLongitud ?: return

        val mensaje =
            "🚨 ALERTA DE PÁNICO 🚨\n" +
                    "Ubicación:\n" +
                    "https://www.google.com/maps?q=$latitud,$longitud"

        obtenerContactos { contactos ->

            enviarSMS(
                contactos,
                mensaje
            )

            enviarReporte(
                mensaje,
                latitud,
                longitud
            )
        }
    }

    // =====================================================
    // 🔥 CONTACTOS
    // =====================================================
    private fun obtenerContactos(
        callback: (List<String>) -> Unit
    ) {

        val prefs =
            getSharedPreferences(
                "APP_PREFS",
                MODE_PRIVATE
            )

        val token =
            prefs.getString(
                "TOKEN",
                null
            )

        val request =
            Request.Builder()
                .url(
                    "$BASE_URL/api/contactos"
                )
                .addHeader(
                    "Authorization",
                    "Bearer $token"
                )
                .get()
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
                            "Error obteniendo contactos",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response
                ) {

                    val lista =
                        mutableListOf<String>()

                    try {

                        val jsonArray =
                            JSONArray(
                                response.body?.string()
                            )

                        for (i in 0 until jsonArray.length()) {

                            val obj =
                                jsonArray.getJSONObject(i)

                            lista.add(
                                obj.getString("telefono")
                            )
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }

                    callback(lista)
                }
            })
    }

    // =====================================================
    // 🔥 SMS
    // =====================================================
    private fun enviarSMS(
        contactos: List<String>,
        mensaje: String
    ) {

        try {

            val smsManager =
                SmsManager.getDefault()

            for (numero in contactos) {

                val partes =
                    smsManager.divideMessage(
                        mensaje
                    )

                smsManager.sendMultipartTextMessage(
                    numero,
                    null,
                    partes,
                    null,
                    null
                )
            }

            runOnUiThread {

                Toast.makeText(
                    this,
                    "SMS enviados 🚨",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    // =====================================================
    // 🔥 REPORTE BACKEND
    // =====================================================
    private fun enviarReporte(
        descripcion: String,
        lat: Double,
        lng: Double
    ) {

        val prefs =
            getSharedPreferences(
                "APP_PREFS",
                MODE_PRIVATE
            )

        val token =
            prefs.getString(
                "TOKEN",
                null
            )

        val body =
            FormBody.Builder()
                .add(
                    "descripcion",
                    descripcion
                )
                .add(
                    "latitud",
                    lat.toString()
                )
                .add(
                    "longitud",
                    lng.toString()
                )
                .build()

        val request =
            Request.Builder()
                .url(
                    "$BASE_URL/api/reportes/guardar-con-token"
                )
                .addHeader(
                    "Authorization",
                    "Bearer $token"
                )
                .post(body)
                .build()

        OkHttpClient()
            .newCall(request)
            .enqueue(object : Callback {

                override fun onFailure(
                    call: Call,
                    e: IOException
                ) {

                    Log.e(
                        "PANICO",
                        "Error reporte",
                        e
                    )
                }

                override fun onResponse(
                    call: Call,
                    response: Response
                ) {

                    Log.d(
                        "PANICO",
                        "Reporte enviado correctamente"
                    )
                }
            })
    }
}