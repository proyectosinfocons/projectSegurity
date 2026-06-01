package com.project.projectsegurity

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.LocationServices
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import java.util.Locale

class NotificacionesActivity : AppCompatActivity() {

    private lateinit var switchNotificaciones: Switch
    private lateinit var listView: ListView
    private lateinit var btnVolver: Button

    private val listaAlertas = mutableListOf<String>()

    private val client = OkHttpClient()

    private val handler = Handler(Looper.getMainLooper())

    private val CHANNEL_ID = "ALERTAS_SEGURIDAD"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_notificaciones)

        switchNotificaciones =
            findViewById(R.id.switchNotificaciones)

        listView =
            findViewById(R.id.listNotificaciones)

        btnVolver =
            findViewById(R.id.btnVolver)

        crearCanalNotificaciones()

        // =====================================================
        // 🔥 RECUPERAR ESTADO GUARDADO
        // =====================================================

        val prefs =
            getSharedPreferences(
                "APP_PREFS",
                MODE_PRIVATE
            )

        val estadoGuardado =
            prefs.getBoolean(
                "NOTIFICACIONES_ACTIVADAS",
                false
            )

        switchNotificaciones.isChecked =
            estadoGuardado

        // =====================================================
        // 🔥 MOSTRAR / OCULTAR LISTA
        // =====================================================

        if (estadoGuardado) {

            listView.visibility =
                View.VISIBLE

        } else {

            // 🔥 IMPORTANTE:
            // INVISIBLE mantiene el espacio
            // y el botón no sube

            listView.visibility =
                View.INVISIBLE
        }

        // =====================================================
        // 🔥 SI YA ESTABA ACTIVADO
        // =====================================================

        if (estadoGuardado) {

            iniciarMonitoreo()
        }

        // =====================================================
        // 🔥 SWITCH CAMBIO
        // =====================================================

        switchNotificaciones.setOnCheckedChangeListener {

                _, isChecked ->

            // =================================================
            // 🔥 GUARDAR ESTADO
            // =================================================

            prefs.edit()
                .putBoolean(
                    "NOTIFICACIONES_ACTIVADAS",
                    isChecked
                )
                .apply()

            if (isChecked) {

                Toast.makeText(
                    this,
                    "Notificaciones activadas",
                    Toast.LENGTH_LONG
                ).show()

                // =============================================
                // 🔥 MOSTRAR LISTA
                // =============================================

                listView.visibility =
                    View.VISIBLE

                // =============================================
                // 🔥 INICIAR MONITOREO
                // =============================================

                iniciarMonitoreo()

            } else {

                Toast.makeText(
                    this,
                    "Notificaciones desactivadas",
                    Toast.LENGTH_LONG
                ).show()

                // =============================================
                // 🔥 DETENER ALERTAS
                // =============================================

                handler.removeCallbacksAndMessages(null)

                // =============================================
                // 🔥 LIMPIAR LISTA
                // =============================================

                listaAlertas.clear()

                listView.adapter =
                    ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        listaAlertas
                    )

                // =============================================
                // 🔥 OCULTAR LISTA
                // Manteniendo espacio
                // =============================================

                listView.visibility =
                    View.INVISIBLE
            }
        }

        // =====================================================
        // 🔥 BOTÓN VOLVER
        // =====================================================

        btnVolver.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    MainMenuActivity::class.java
                )
            )

            finish()
        }
    }

    // =====================================================
    // 🔥 INICIAR MONITOREO
    // =====================================================

    private fun iniciarMonitoreo() {

        handler.post(object : Runnable {

            override fun run() {

                obtenerAlertasCercanas()

                handler.postDelayed(
                    this,
                    15000
                )
            }
        })
    }

    // =====================================================
    // 🔥 OBTENER ALERTAS
    // =====================================================

    @SuppressLint("MissingPermission")
    private fun obtenerAlertasCercanas() {

        val fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->

                if (location == null) return@addOnSuccessListener

                val latUsuario =
                    location.latitude

                val lonUsuario =
                    location.longitude

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
                            //"http://192.168.18.238:8080/api/reportes"
                            "http://projectsecuritypeople-env.eba-k3djm54f.us-east-2.elasticbeanstalk.com/api/reportes"

                        )
                        .addHeader(
                            "Authorization",
                            "Bearer $token"
                        )
                        .build()

                client.newCall(request)
                    .enqueue(object : Callback {

                        override fun onFailure(
                            call: Call,
                            e: IOException
                        ) {

                            runOnUiThread {

                                Toast.makeText(
                                    applicationContext,
                                    "Error obteniendo alertas",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onResponse(
                            call: Call,
                            response: Response
                        ) {

                            try {

                                val body =
                                    response.body?.string()

                                val jsonArray =
                                    JSONArray(body)

                                listaAlertas.clear()

                                for (i in 0 until jsonArray.length()) {

                                    val obj =
                                        jsonArray.getJSONObject(i)

                                    val lat =
                                        obj.getDouble("latitud")

                                    val lon =
                                        obj.getDouble("longitud")

                                    val descripcion =
                                        obj.getString("descripcion")

                                    val tipo =
                                        obj.getString("tiporeporte")

                                    val resultado =
                                        FloatArray(1)

                                    android.location.Location.distanceBetween(
                                        latUsuario,
                                        lonUsuario,
                                        lat,
                                        lon,
                                        resultado
                                    )

                                    val distancia =
                                        resultado[0]

                                    if (distancia <= 500) {

                                        val geocoder =
                                            Geocoder(
                                                this@NotificacionesActivity,
                                                Locale.getDefault()
                                            )

                                        var zona =
                                            "Zona desconocida"

                                        try {

                                            val direcciones =
                                                geocoder.getFromLocation(
                                                    lat,
                                                    lon,
                                                    1
                                                )

                                            if (
                                                direcciones != null &&
                                                direcciones.isNotEmpty()
                                            ) {

                                                val direccion =
                                                    direcciones[0]

                                                val distrito =
                                                    direccion.subLocality ?: ""

                                                val calle =
                                                    direccion.thoroughfare ?: ""

                                                zona =
                                                    "$distrito - $calle"
                                            }

                                        } catch (e: Exception) {

                                            e.printStackTrace()
                                        }

                                        val mensaje =
                                            "🚨 $tipo\n" +
                                                    "Zona: $zona\n" +
                                                    "$descripcion\n" +
                                                    "A ${distancia.toInt()} metros"

                                        listaAlertas.add(mensaje)

                                        mostrarNotificacion(
                                            mensaje
                                        )
                                    }
                                }

                                runOnUiThread {

                                    listView.adapter =
                                        ArrayAdapter(
                                            this@NotificacionesActivity,
                                            android.R.layout.simple_list_item_1,
                                            listaAlertas
                                        )
                                }

                            } catch (e: Exception) {

                                e.printStackTrace()
                            }
                        }
                    })
            }
    }

    // =====================================================
    // 🔥 MOSTRAR NOTIFICACIÓN
    // =====================================================

    private fun mostrarNotificacion(
        mensaje: String
    ) {

        val builder =
            NotificationCompat.Builder(
                this,
                CHANNEL_ID
            )
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle(
                    "Alerta cercana"
                )
                .setContentText(mensaje)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(mensaje)
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )

        with(NotificationManagerCompat.from(this)) {

            if (
                ActivityCompat.checkSelfPermission(
                    this@NotificacionesActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            notify(
                System.currentTimeMillis().toInt(),
                builder.build()
            )
        }
    }

    // =====================================================
    // 🔥 CREAR CANAL
    // =====================================================

    private fun crearCanalNotificaciones() {

        if (
            Build.VERSION.SDK_INT
            >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Alertas de Seguridad",
                    NotificationManager.IMPORTANCE_HIGH
                )

            channel.description =
                "Canal de alertas delictivas cercanas"

            val manager =
                getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    // =====================================================
    // 🔥 DETENER HANDLER
    // =====================================================

    override fun onDestroy() {

        super.onDestroy()

        handler.removeCallbacksAndMessages(null)
    }
}