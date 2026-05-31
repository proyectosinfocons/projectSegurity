package com.project.projectsegurity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject



import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient:
            FusedLocationProviderClient

    private val client = OkHttpClient()


    // =====================================================
    // NUEVO - BTN ZONA SEGURA
    // =====================================================
    private lateinit var btnZonaSegura:
            FloatingActionButton


    // =====================================================
    // AUTOCOMPLETE
    // =====================================================
    private lateinit var spinnerDistrito:
            AutoCompleteTextView

    private lateinit var spinnerZona:
            AutoCompleteTextView

    // =====================================================
    // NUEVO - RUTA
    // =====================================================
    private var rutaPolyline: Polyline? = null



    private val LOCATION_PERMISSION_CODE = 100

    private var currentMarker: Marker? = null

    private var marcadorUbicacionActual:
            Marker? = null

    private var marcadorZona:
            Marker? = null

    // =====================================================
    // CIRCULO UBICACION
    // =====================================================
    private var circuloUbicacion:
            Circle? = null

    private var listaReportes = JSONArray()


    // =====================================================
    // RUTA ZONA SEGURA
    // =====================================================
    private var polylineRuta: Polyline? = null


    private var marcadorZonaSegura: Marker? = null

    // =====================================================
    // MARCADORES SERVICIOS DE SEGURIDAD
    // =====================================================
    private val listaMarcadoresServicios =
        mutableListOf<Marker>()


    // =====================================================
    // CONTROL PETICIONES OVERPASS
    // =====================================================
    @Volatile
    private var requestIdActual = 0

    // =====================================================
    // ON CREATE
    // =====================================================
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_map)

        val mapFragment =
            supportFragmentManager
                .findFragmentById(R.id.map)
                    as SupportMapFragment

        mapFragment.getMapAsync(this)

        fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(this)

        spinnerDistrito =
            findViewById(R.id.spinnerDistrito)

        // =====================================================
        // NUEVO - BTN ZONA SEGURA
        // =====================================================
        btnZonaSegura =
            findViewById(R.id.btnZonaSegura)


        spinnerZona =
            findViewById(R.id.spinnerZona)

        val panelFiltros =
            findViewById<LinearLayout>(
                R.id.panelFiltros
            )

        val btnFiltros =
            findViewById<FloatingActionButton>(
                R.id.btnFiltros
            )

        val btnCerrar =
            findViewById<TextView>(
                R.id.btnCerrarFiltros
            )

        // =====================================================
        // BOTON IR A MI UBICACION
        // =====================================================
        val btnMiUbicacion =
            findViewById<FloatingActionButton>(
                R.id.btnMiUbicacion
            )
// =====================================================
// LLAMAR MÉTODO EN onCreate()
// =====================================================
        btnZonaSegura.setOnClickListener {

            buscarZonaSeguraMasCercana()
        }


        btnMiUbicacion.setOnClickListener {

            if (tienePermisoUbicacion()) {
                //cargarServiciosSeguridad()
                obtenerUbicacionActual()

            } else {
                //cargarServiciosSeguridad()
                solicitarPermisoUbicacion()
            }
        }

        btnCerrar.setOnClickListener {

            panelFiltros.visibility =
                View.GONE
        }

        btnFiltros.setOnClickListener {

            panelFiltros.visibility =

                if (
                    panelFiltros.visibility
                    ==
                    View.GONE
                )

                    View.VISIBLE

                else

                    View.GONE
        }

        findViewById<Button>(
            R.id.btnSalirMapa
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    MainMenuActivity::class.java
                )
            )

            finish()
        }

        cargarDistritos()
    }

    // =====================================================
    // MAPA
    // =====================================================
    override fun onMapReady(
        googleMap: GoogleMap
    ) {

        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true


        mMap.setPadding(
            0,
            0,
            0,
            250
        )
        cargarServiciosSeguridad()
        if (tienePermisoUbicacion()) {
            obtenerUbicacionActual()

        } else {
            solicitarPermisoUbicacion()
        }

        cargarReportes()

    }

    // =====================================================
// BUSCAR ZONA SEGURA MÁS CERCANA
// =====================================================
    @SuppressLint("MissingPermission")
    private fun buscarZonaSeguraMasCercana() {

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->

                // =====================================================
                // VALIDAR UBICACIÓN
                // =====================================================
                if (location == null) {

                    Toast.makeText(
                        this,
                        "No se pudo obtener ubicación",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }

                // =====================================================
                // UBICACIÓN ACTUAL
                // =====================================================
                val origen =

                    LatLng(
                        location.latitude,
                        location.longitude
                    )

                // =====================================================
                // VALIDAR LISTA
                // =====================================================
                if (listaMarcadoresServicios.isEmpty()) {

                    Toast.makeText(
                        this,
                        "No hay zonas seguras",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }

                // =====================================================
                // VARIABLES
                // =====================================================
                var menorDistancia =
                    Double.MAX_VALUE

                var markerMasCercano:
                        Marker? = null

                // =====================================================
                // BUSCAR EL MÁS CERCANO
                // =====================================================
                for (marker in listaMarcadoresServicios) {

                    try {

                        val resultado =
                            FloatArray(1)

                        android.location.Location.distanceBetween(
                            origen.latitude,
                            origen.longitude,
                            marker.position.latitude,
                            marker.position.longitude,
                            resultado
                        )

                        val distancia =
                            resultado[0].toDouble()

                        if (distancia < menorDistancia) {

                            menorDistancia =
                                distancia

                            markerMasCercano =
                                marker
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }

                // =====================================================
                // VALIDAR RESULTADO
                // =====================================================
                if (markerMasCercano == null) {

                    Toast.makeText(
                        this,
                        "No se encontró zona segura",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }

                // =====================================================
                // MOSTRAR INFO WINDOW
                // =====================================================
                markerMasCercano.showInfoWindow()

                // =====================================================
                // MOVER CÁMARA
                // =====================================================
                mMap.animateCamera(

                    CameraUpdateFactory
                        .newLatLngZoom(
                            markerMasCercano.position,
                            18f
                        )
                )

                // =====================================================
                // NOMBRE
                // =====================================================
                val nombre =

                    markerMasCercano.snippet
                        ?: "Zona Segura"

// =====================================================
// BUSCAR ENTRADA PRINCIPAL
// =====================================================
                buscarEntradaPrincipal(
                    origen,
                    markerMasCercano
                ) { entradaPrincipal ->

                    // =====================================================
// OBTENER ENTRADA REAL
// =====================================================
                    obtenerEntradaEdificio(
                        markerMasCercano
                    ) { entradaReal ->

                        // =====================================================
                        // OBTENER RUTA
                        // =====================================================
                        obtenerRutaInteligente(
                            origen,
                            entradaReal,
                            nombre,
                            markerMasCercano
                        )
                    }
                }
            }
    }



    // =====================================================
// OBTENER BORDE DEL EDIFICIO MÁS CERCANO A LA CALLE
// =====================================================
    private fun obtenerEntradaEdificio(
        marker: Marker,
        callback: (LatLng) -> Unit
    ) {

        Thread {

            try {

                val lat =
                    marker.position.latitude

                val lon =
                    marker.position.longitude

                val query = """
            [out:json];

            (
              way(around:40,$lat,$lon)["amenity"="police"];
            );

            out geom;
        """.trimIndent()

                val request =

                    Request.Builder()
                        .url(
                            "https://overpass-api.de/api/interpreter"
                        )
                        .post(
                            FormBody.Builder()
                                .add("data", query)
                                .build()
                        )
                        .build()

                val response =
                    client.newCall(request)
                        .execute()

                val body =
                    response.body?.string()

                if (body.isNullOrEmpty()) {

                    runOnUiThread {

                        callback(marker.position)
                    }

                    return@Thread
                }

                val json =
                    JSONObject(body)

                val elements =
                    json.getJSONArray("elements")

                if (elements.length() == 0) {

                    runOnUiThread {

                        callback(marker.position)
                    }

                    return@Thread
                }

                val building =
                    elements.getJSONObject(0)

                val geometry =
                    building.getJSONArray("geometry")

                // =====================================================
                // BUSCAR PUNTO MÁS AL SUR
                // (MANUEL VILLAR)
                // =====================================================
                var mejorLat =
                    999.0

                var mejorLon =
                    0.0

                for (i in 0 until geometry.length()) {

                    val punto =
                        geometry.getJSONObject(i)

                    val pLat =
                        punto.getDouble("lat")

                    val pLon =
                        punto.getDouble("lon")

                    // =====================================================
                    // EL MÁS AL SUR
                    // =====================================================
                    if (pLat < mejorLat) {

                        mejorLat =
                            pLat

                        mejorLon =
                            pLon
                    }
                }

                val entrada =

                    LatLng(
                        mejorLat,
                        mejorLon
                    )

                runOnUiThread {

                    callback(entrada)
                }

            } catch (e: Exception) {

                e.printStackTrace()

                runOnUiThread {

                    callback(marker.position)
                }
            }

        }.start()
    }



    // =====================================================
// OBTENER RUTA SEGURA CON OSRM
// =====================================================
    private fun obtenerRutaSegura(
        origen: LatLng,
        destino: LatLng,
        nombre: String
    ) {

        // =====================================================
        // URL OSRM
        // =====================================================
        val url =

            "https://router.project-osrm.org/route/v1/driving/" +
                    "${origen.longitude},${origen.latitude};" +
                    "${destino.longitude},${destino.latitude}" +
                    "?overview=full&geometries=polyline"

        val request =

            Request.Builder()
                .url(url)
                .build()

        client.newCall(request)
            .enqueue(object : Callback {

                // =====================================================
                // ERROR CONEXIÓN
                // =====================================================
                override fun onFailure(
                    call: okhttp3.Call,
                    e: IOException
                ) {

                    runOnUiThread {

                        Toast.makeText(
                            this@MapActivity,
                            "Error obteniendo ruta",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    e.printStackTrace()
                }

                // =====================================================
                // RESPUESTA EXITOSA
                // =====================================================
                override fun onResponse(
                    call: okhttp3.Call,
                    response: Response
                ) {

                    try {

                        val body =
                            response.body?.string()

                        // =====================================================
                        // VALIDAR RESPUESTA
                        // =====================================================
                        if (body.isNullOrEmpty()) {

                            runOnUiThread {

                                Toast.makeText(
                                    this@MapActivity,
                                    "Respuesta vacía",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            return
                        }

                        val json =
                            JSONObject(body)

                        // =====================================================
                        // VALIDAR ROUTES
                        // =====================================================
                        if (!json.has("routes")) {

                            runOnUiThread {

                                Toast.makeText(
                                    this@MapActivity,
                                    "OSRM no devolvió rutas",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            return
                        }

                        val routes =
                            json.getJSONArray("routes")

                        // =====================================================
                        // VALIDAR RUTA
                        // =====================================================
                        if (routes.length() == 0) {

                            runOnUiThread {

                                Toast.makeText(
                                    this@MapActivity,
                                    "No se encontró ruta",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            return
                        }

                        // =====================================================
                        // PRIMERA RUTA
                        // =====================================================
                        val route =
                            routes.getJSONObject(0)

                        // =====================================================
                        // GEOMETRÍA
                        // =====================================================
                        val geometry =
                            route.getString("geometry")

                        // =====================================================
                        // DECODIFICAR POLYLINE
                        // =====================================================
                        val puntos =
                            decodificarPolyline(
                                geometry
                            )

                        runOnUiThread {

                            // =====================================================
                            // LIMPIAR ANTERIOR
                            // =====================================================
                            polylineRuta?.remove()

                            marcadorZonaSegura?.remove()

                            // =====================================================
                            // DIBUJAR RUTA
                            // =====================================================
                            polylineRuta =

                                mMap.addPolyline(

                                    PolylineOptions()
                                        .addAll(puntos)
                                        .width(18f)
                                        .color(Color.GREEN)
                                        .geodesic(true)
                                        .jointType(
                                            JointType.ROUND
                                        )
                                )

                            // =====================================================
                            // MARCADOR DESTINO
                            // =====================================================
                            marcadorZonaSegura =

                                mMap.addMarker(

                                    MarkerOptions()
                                        .position(destino)
                                        .title("🛡 $nombre")
                                        .snippet(
                                            "Zona segura más cercana"
                                        )
                                        .icon(
                                            BitmapDescriptorFactory
                                                .defaultMarker(
                                                    BitmapDescriptorFactory
                                                        .HUE_GREEN
                                                )
                                        )
                                )

                            // =====================================================
                            // MOSTRAR INFO
                            // =====================================================
                            marcadorZonaSegura
                                ?.showInfoWindow()

                            // =====================================================
                            // MOVER CÁMARA
                            // =====================================================
                            mMap.animateCamera(

                                CameraUpdateFactory
                                    .newLatLngZoom(
                                        destino,
                                        17f
                                    )
                            )

                            // =====================================================
                            // MENSAJE
                            // =====================================================
                            Toast.makeText(
                                this@MapActivity,
                                "Ruta encontrada",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()

                        runOnUiThread {

                            Toast.makeText(
                                this@MapActivity,
                                "Error procesando datos",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            })
    }






    // =====================================================
// CARGAR CASETAS - COMISARIAS - MUNICIPALIDAD
// =====================================================
    private fun cargarServiciosSeguridad() {

        Thread {

            try {

                val query = """
                [out:json][timeout:25];

                (
                  // =====================================================
                  // COMISARIAS
                  // =====================================================
                  node["amenity"="police"](-12.35,-77.20,-11.75,-76.70);
                  way["amenity"="police"](-12.35,-77.20,-11.75,-76.70);

                  // =====================================================
                  // MUNICIPALIDADES
                  // =====================================================
                  node["amenity"="townhall"](-12.35,-77.20,-11.75,-76.70);
                  way["amenity"="townhall"](-12.35,-77.20,-11.75,-76.70);

                  // =====================================================
                  // CASETAS DE SERENAZGO
                  // =====================================================
                  node["building"="guardhouse"](-12.35,-77.20,-11.75,-76.70);
                  way["building"="guardhouse"](-12.35,-77.20,-11.75,-76.70);

                  node["security"="guardhouse"](-12.35,-77.20,-11.75,-76.70);
                  way["security"="guardhouse"](-12.35,-77.20,-11.75,-76.70);

                  node["amenity"="security"](-12.35,-77.20,-11.75,-76.70);
                  way["amenity"="security"](-12.35,-77.20,-11.75,-76.70);
                );

                out center tags;
            """.trimIndent()

                val request =

                    Request.Builder()
                        .url(
                            "https://overpass-api.de/api/interpreter"
                        )
                        .post(
                            FormBody.Builder()
                                .add(
                                    "data",
                                    query
                                )
                                .build()
                        )
                        .addHeader(
                            "User-Agent",
                            "Android-App"
                        )
                        .build()

                val response =
                    client.newCall(request)
                        .execute()

                val body =
                    response.body?.string()

                if (
                    body.isNullOrEmpty()
                ) {
                    return@Thread
                }

                val json =
                    JSONObject(body)

                val elements =
                    json.getJSONArray("elements")

                runOnUiThread {

                    // =====================================================
                    // ELIMINAR MARCADORES ANTERIORES
                    // =====================================================
                    listaMarcadoresServicios.forEach {

                        it.remove()
                    }

                    listaMarcadoresServicios.clear()

                    for (
                    i in 0 until
                            elements.length()
                    ) {

                        try {

                            val obj =
                                elements.getJSONObject(i)

                            val tags =
                                obj.optJSONObject("tags")

                            val nombre =
                                tags?.optString(
                                    "name",
                                    "Sin nombre"
                                ) ?: "Sin nombre"

                            var lat = 0.0
                            var lon = 0.0

                            // =====================================================
                            // OBTENER COORDENADAS
                            // =====================================================
                            if (obj.has("lat")) {

                                lat =
                                    obj.getDouble("lat")

                                lon =
                                    obj.getDouble("lon")

                            }

                            else if (obj.has("center")) {

                                val center =
                                    obj.getJSONObject("center")

                                lat =
                                    center.getDouble("lat")

                                lon =
                                    center.getDouble("lon")
                            }

                            val amenity =
                                tags?.optString(
                                    "amenity",
                                    ""
                                ) ?: ""

                            val building =
                                tags?.optString(
                                    "building",
                                    ""
                                ) ?: ""

                            val security =
                                tags?.optString(
                                    "security",
                                    ""
                                ) ?: ""

                            // =====================================================
                            // VARIABLES
                            // =====================================================
                            var color =
                                BitmapDescriptorFactory
                                    .HUE_AZURE

                            var titulo =
                                "Servicio"

                            var mostrar = false

                            // =====================================================
                            // COMISARIAS
                            // =====================================================
                            if (
                                amenity == "police"
                            ) {

                                // CYAN
                                color =
                                    BitmapDescriptorFactory
                                        .HUE_CYAN

                                titulo =
                                    "👮 COMISARÍA"

                                mostrar = true
                            }

                            // =====================================================
                            // MUNICIPALIDADES
                            // =====================================================
                            if (
                                amenity == "townhall"
                            ) {

                                // MAGENTA
                                color =
                                    BitmapDescriptorFactory
                                        .HUE_MAGENTA

                                titulo =
                                    "🏛 MUNICIPALIDAD"

                                mostrar = true
                            }

                            // =====================================================
                            // CASETAS SERENAZGO
                            // =====================================================
                            if (
                                building == "guardhouse"
                                ||
                                security == "guardhouse"
                                ||
                                amenity == "security"
                            ) {

                                // ROSADO
                                color =
                                    BitmapDescriptorFactory
                                        .HUE_ROSE

                                titulo =
                                    "🚓 CASETA SERENAZGO"

                                mostrar = true
                            }

                            // =====================================================
                            // FILTRAR ENTIDADES NO DESEADAS
                            // =====================================================
                            val nombreMayus =
                                nombre.uppercase()

                            if (
                                nombreMayus.contains("ONPE")
                                ||
                                nombreMayus.contains("RENIEC")
                                ||
                                nombreMayus.contains("MINISTERIO")
                                ||
                                nombreMayus.contains("SUNAT")
                                ||
                                nombreMayus.contains("JNE")
                                ||
                                nombreMayus.contains("UGEL")
                                ||
                                nombreMayus.contains("COLEGIO")
                                ||
                                nombreMayus.contains("HOSPITAL")
                            ) {

                                mostrar = false
                            }

                            // =====================================================
                            // AGREGAR MARCADOR
                            // =====================================================
                            if (mostrar) {

                                val marker =

                                    mMap.addMarker(

                                        MarkerOptions()
                                            .position(
                                                LatLng(
                                                    lat,
                                                    lon
                                                )
                                            )
                                            .title(
                                                titulo
                                            )
                                            .snippet(
                                                nombre
                                            )
                                            .icon(
                                                BitmapDescriptorFactory
                                                    .defaultMarker(
                                                        color
                                                    )
                                            )
                                    )

                                marker?.let {

                                    // =====================================================
                                    // MOSTRAR TEXTO SOBRE EL PUNTO
                                    // =====================================================
                                    it.showInfoWindow()

                                    listaMarcadoresServicios
                                        .add(it)
                                }
                            }

                        } catch (e: Exception) {

                            e.printStackTrace()
                        }
                    }
                }

            } catch (e: Exception) {

                e.printStackTrace()
            }

        }.start()
    }

    // =====================================================
    // REPORTES
    // =====================================================
    private fun cargarReportes() {

        Thread {

            try {

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
                            "http://192.168.18.238:8080/api/reportes"
                        )
                        .addHeader(
                            "Authorization",
                            "Bearer $token"
                        )
                        .get()
                        .build()

                val response =
                    client.newCall(request)
                        .execute()

                val body =
                    response.body?.string()

                listaReportes =
                    JSONArray(body)

                runOnUiThread {

                    pintarReportes()

                }

            } catch (e: Exception) {

                e.printStackTrace()
            }

        }.start()
    }

    // =====================================================
    // PINTAR REPORTES
    // =====================================================
    private fun pintarReportes() {

        for (
        i in 0 until
                listaReportes.length()
        ) {

            val obj =
                listaReportes
                    .getJSONObject(i)

            val lat =
                obj.getDouble(
                    "latitud"
                )

            val lng =
                obj.getDouble(
                    "longitud"
                )

            val descripcion =
                obj.getString(
                    "descripcion"
                )

            val tipo =
                obj.optString(
                    "tiporeporte",
                    ""
                )

            val color =
                when (tipo) {

                    "BOTON DE PANICO" ->

                        BitmapDescriptorFactory.HUE_RED

                    "ROBO" ->

                        BitmapDescriptorFactory.HUE_ORANGE

                    else ->

                        BitmapDescriptorFactory.HUE_YELLOW
                }

            mMap.addMarker(

                MarkerOptions()
                    .position(
                        LatLng(lat, lng)
                    )
                    .title("🚨 $tipo")
                    .snippet(descripcion)
                    .icon(
                        BitmapDescriptorFactory
                            .defaultMarker(color)
                    )
            )
        }
    }

    // =====================================================
    // PERMISOS
    // =====================================================
    private fun tienePermisoUbicacion():
            Boolean {

        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun solicitarPermisoUbicacion() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission
                    .ACCESS_FINE_LOCATION
            ),
            LOCATION_PERMISSION_CODE
        )
    }

    // =====================================================
    // RESPUESTA PERMISOS
    // =====================================================
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode ==
            LOCATION_PERMISSION_CODE
        ) {

            if (
                grantResults.isNotEmpty()
                &&
                grantResults[0]
                ==
                PackageManager.PERMISSION_GRANTED
            ) {

                obtenerUbicacionActual()

            } else {

                Toast.makeText(
                    this,
                    "Permiso de ubicación denegado",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // =====================================================
    // UBICACIÓN ACTUAL
    // =====================================================
    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionActual() {

        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc ->

                loc?.let {

                    val pos =
                        LatLng(
                            it.latitude,
                            it.longitude
                        )

                    // =====================================================
                    // ELIMINAR ANTERIOR
                    // =====================================================
                    marcadorUbicacionActual
                        ?.remove()

                    circuloUbicacion
                        ?.remove()

                    // =====================================================
                    // MARCADOR AZUL
                    // =====================================================
                    marcadorUbicacionActual =

                        mMap.addMarker(

                            MarkerOptions()
                                .position(pos)
                                .title(
                                    "📍 Estoy aquí"
                                )
                                .snippet(
                                    "Tu ubicación actual"
                                )
                                .icon(
                                    BitmapDescriptorFactory
                                        .defaultMarker(
                                            BitmapDescriptorFactory
                                                .HUE_BLUE
                                        )
                                )
                        )

                    // =====================================================
                    // CIRCULO VISUAL
                    // =====================================================
                    circuloUbicacion =

                        mMap.addCircle(

                            CircleOptions()
                                .center(pos)
                                .radius(45.0)
                                .strokeWidth(5f)
                                .strokeColor(
                                    0xFF0288D1.toInt()
                                )
                                .fillColor(
                                    0x220288D1
                                        .toInt()
                                )
                        )

                    // =====================================================
                    // MOVER CAMARA
                    // =====================================================
                    mMap.animateCamera(

                        CameraUpdateFactory
                            .newLatLngZoom(
                                pos,
                                16f
                            )
                    )

                    // =====================================================
                    // MOSTRAR TEXTO
                    // =====================================================
                    marcadorUbicacionActual
                        ?.showInfoWindow()
                }
            }
    }

    // =====================================================
    // DISTRITOS
    // =====================================================
    private fun cargarDistritos() {

        val distritos = arrayListOf(

            "[SELECCIONE EL DISTRITO]",

            "ATE",
            "BARRANCO",
            "BREÑA",
            "CARABAYLLO",
            "CHACLACAYO",
            "CHORRILLOS",
            "CIENEGUILLA",
            "COMAS",
            "EL AGUSTINO",
            "INDEPENDENCIA",
            "JESUS MARIA",
            "LA MOLINA",
            "LA VICTORIA",
            "LIMA",
            "LINCE",
            "LOS OLIVOS",
            "LURIGANCHO",
            "LURIN",
            "MAGDALENA",
            "MIRAFLORES",
            "PACHACAMAC",
            "PUEBLO LIBRE",
            "PUENTE PIEDRA",
            "RIMAC",
            "SAN BARTOLO",
            "SAN BORJA",
            "SAN ISIDRO",
            "SAN JUAN DE LURIGANCHO",
            "SAN JUAN DE MIRAFLORES",
            "SAN LUIS",
            "SAN MARTIN DE PORRES",
            "SAN MIGUEL",
            "SANTA ANITA",
            "SANTIAGO DE SURCO",
            "SURQUILLO",
            "VILLA EL SALVADOR",
            "VILLA MARIA DEL TRIUNFO"
        )

        val adapterDistritos =

            ArrayAdapter(
                this,
                android.R.layout
                    .simple_dropdown_item_1line,
                distritos
            )

        spinnerDistrito.setAdapter(
            adapterDistritos
        )

        spinnerDistrito.threshold = 1

        spinnerDistrito.setOnClickListener {

            spinnerDistrito.showDropDown()
        }

        spinnerDistrito.setOnItemClickListener {
                parent,
                view,
                position,
                id ->

            val distrito =
                parent.getItemAtPosition(position)
                    .toString()

            mostrarDistrito(distrito)

            cargarCalles(distrito)
            cargarServiciosSeguridad()
        }
    }

    // =====================================================
    // BOUNDING BOX
    // =====================================================
    private fun getBBox(
        distrito: String
    ): String {

        return when (
            distrito.uppercase()
        ) {

            "COMAS" ->
                "-11.95,-77.08,-11.90,-77.00"

            "LOS OLIVOS" ->
                "-11.95,-77.08,-11.92,-77.02"

            "MIRAFLORES" ->
                "-12.13,-77.04,-12.10,-77.02"

            "SAN ISIDRO" ->
                "-12.10,-77.05,-12.08,-77.02"

            "SANTIAGO DE SURCO" ->
                "-12.15,-77.02,-12.10,-76.95"

            "ATE" ->
                "-12.05,-76.95,-12.00,-76.85"

            "RIMAC" ->
                "-12.04,-77.05,-12.02,-77.00"

            "SAN JUAN DE LURIGANCHO" ->
                "-11.98,-77.01,-11.90,-76.90"

            "LIMA" ->
                "-12.10,-77.05,-12.00,-77.00"

            else ->
                "-12.20,-77.10,-11.80,-76.90"
        }
    }

    // =====================================================
    // CARGAR CALLES
    // =====================================================
    private fun cargarCalles(
        distrito: String
    ) {
        // =====================================================
        // LIMPIAR CAMPO ZONA
        // =====================================================
        spinnerZona.setText("")

        if (
            distrito ==
            "[SELECCIONE EL DISTRITO]"
        ) {

            val adapterZona =

                ArrayAdapter(
                    this,
                    android.R.layout
                        .simple_dropdown_item_1line,
                    arrayListOf(
                        "[SELECCIONE ZONA]"
                    )
                )

            spinnerZona.setAdapter(
                adapterZona
            )

            return
        }

        val requestId =
            ++requestIdActual

        Thread {

            try {

                val bbox =
                    getBBox(distrito)

                val query = """
                    [out:json][timeout:25];
                    (
                        way["highway"]["name"]($bbox);

                        way["landuse"="residential"]["name"]($bbox);

                        node["place"~"suburb|neighbourhood|quarter"]["name"]($bbox);

                        relation["place"~"suburb|neighbourhood|quarter"]["name"]($bbox);
                    );
                    out center tags;
                """.trimIndent()

                val request =

                    Request.Builder()
                        .url(
                            "https://overpass-api.de/api/interpreter"
                        )
                        .post(
                            FormBody.Builder()
                                .add(
                                    "data",
                                    query
                                )
                                .build()
                        )
                        .addHeader(
                            "User-Agent",
                            "Android-App"
                        )
                        .build()

                val response =
                    client.newCall(request)
                        .execute()

                val body =
                    response.body?.string()

                if (
                    body.isNullOrEmpty()
                    ||
                    !body.trim()
                        .startsWith("{")
                ) {
                    return@Thread
                }

                if (
                    requestId
                    !=
                    requestIdActual
                ) {
                    return@Thread
                }

                val json =
                    JSONObject(body)

                val elements =
                    json.getJSONArray(
                        "elements"
                    )

                val nombres =
                    mutableListOf<String>()

                val coordenadas =
                    mutableMapOf<
                            String,
                            LatLng
                            >()

                for (
                i in 0 until
                        elements.length()
                ) {

                    val obj =
                        elements
                            .getJSONObject(i)

                    val tags =
                        obj.optJSONObject(
                            "tags"
                        )

                    val nombre =
                        tags?.optString(
                            "name",
                            ""
                        ) ?: ""

                    if (
                        nombre.isEmpty()
                    ) {
                        continue
                    }

                    var lat = 0.0
                    var lon = 0.0

                    var tieneCoordenadas =
                        false

                    if (
                        obj.has("center")
                    ) {

                        val center =
                            obj.getJSONObject(
                                "center"
                            )

                        lat =
                            center.getDouble(
                                "lat"
                            )

                        lon =
                            center.getDouble(
                                "lon"
                            )

                        tieneCoordenadas =
                            true
                    }

                    else if (

                        obj.has("lat")
                        &&
                        obj.has("lon")
                    ) {

                        lat =
                            obj.getDouble(
                                "lat"
                            )

                        lon =
                            obj.getDouble(
                                "lon"
                            )

                        tieneCoordenadas =
                            true
                    }

                    if (
                        tieneCoordenadas
                    ) {

                        if (
                            !coordenadas
                                .containsKey(
                                    nombre
                                )
                        ) {

                            nombres.add(
                                nombre
                            )

                            coordenadas[nombre] =

                                LatLng(
                                    lat,
                                    lon
                                )
                        }
                    }
                }

                val listaFinal =

                    nombres
                        .distinct()
                        .sorted()

                val listaMostrar =
                    arrayListOf<String>()

                listaMostrar.add(
                    "[SELECCIONE ZONA]"
                )

                if (
                    listaFinal.isEmpty()
                ) {

                    listaMostrar.add(
                        "No se encontraron zonas"
                    )

                } else {

                    listaMostrar.addAll(
                        listaFinal
                    )
                }

                runOnUiThread {

                    if (
                        requestId
                        !=
                        requestIdActual
                    ) {
                        return@runOnUiThread
                    }

                    val adapterZona =

                        ArrayAdapter(
                            this,
                            android.R.layout
                                .simple_dropdown_item_1line,
                            listaMostrar
                        )

                    spinnerZona.setAdapter(
                        adapterZona
                    )

                    spinnerZona.threshold = 1

                    spinnerZona.setOnClickListener {

                        spinnerZona.showDropDown()
                    }

                    spinnerZona
                        .setOnItemClickListener {
                                parent,
                                view,
                                position,
                                id ->

                            val nombreZona =
                                parent.getItemAtPosition(position)
                                    .toString()

                            if (
                                nombreZona ==
                                "[SELECCIONE ZONA]"
                                ||
                                nombreZona ==
                                "No se encontraron zonas"
                            ) {
                                return@setOnItemClickListener
                            }

                            val latLng =
                                coordenadas[nombreZona]

                            latLng?.let {

                                mMap.animateCamera(
                                    CameraUpdateFactory
                                        .newLatLngZoom(
                                            it,
                                            17f
                                        )
                                )

                                marcadorZona?.remove()

                                marcadorZona =

                                    mMap.addMarker(

                                        MarkerOptions()
                                            .position(it)
                                            .title(
                                                nombreZona
                                            )
                                            .icon(
                                                BitmapDescriptorFactory
                                                    .defaultMarker(
                                                        BitmapDescriptorFactory
                                                            .HUE_VIOLET
                                                    )
                                            )
                                    )


                            }
                        }
                }

            } catch (e: Exception) {

                e.printStackTrace()

                runOnUiThread {

                    Toast.makeText(
                        this,
                        "Error cargando zonas",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }.start()
    }

    // =====================================================
// OBTENER RUTA INTELIGENTE REAL
// =====================================================
    private fun obtenerRutaInteligente(
        origen: LatLng,
        destino: LatLng,
        nombre: String,
        markerOriginal: Marker
    ) {

        val url =

            "https://router.project-osrm.org/route/v1/driving/" +
                    "${origen.longitude},${origen.latitude};" +
                    "${destino.longitude},${destino.latitude}" +
                    "?overview=full" +
                    "&steps=true" +
                    "&geometries=geojson"

        val request =

            Request.Builder()
                .url(url)
                .build()

        client.newCall(request)
            .enqueue(object : Callback {

                // =====================================================
                // ERROR
                // =====================================================
                override fun onFailure(
                    call: okhttp3.Call,
                    e: IOException
                ) {

                    runOnUiThread {

                        Toast.makeText(
                            this@MapActivity,
                            "Error obteniendo ruta",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    e.printStackTrace()
                }

                // =====================================================
                // RESPUESTA
                // =====================================================
                override fun onResponse(
                    call: okhttp3.Call,
                    response: Response
                ) {

                    try {

                        val body =
                            response.body?.string()

                        if (body.isNullOrEmpty()) {
                            return
                        }

                        val json =
                            JSONObject(body)

                        val routes =
                            json.getJSONArray("routes")

                        if (routes.length() == 0) {

                            runOnUiThread {

                                Toast.makeText(
                                    this@MapActivity,
                                    "No se encontró ruta",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            return
                        }

                        // =====================================================
                        // ROUTE
                        // =====================================================
                        val route =
                            routes.getJSONObject(0)

                        // =====================================================
                        // LEGS
                        // =====================================================
                        val legs =
                            route.getJSONArray("legs")

                        val leg =
                            legs.getJSONObject(0)

                        val steps =
                            leg.getJSONArray("steps")

                        // =====================================================
                        // TOMAR ÚLTIMO STEP REAL
                        // =====================================================
                        val ultimoStep =

                            if (steps.length() > 1)

                                steps.getJSONObject(
                                    steps.length() - 2
                                )

                            else

                                steps.getJSONObject(0)

                        // =====================================================
                        // MANEUVER
                        // =====================================================
                        val maneuver =
                            ultimoStep.getJSONObject(
                                "maneuver"
                            )

                        val location =
                            maneuver.getJSONArray(
                                "location"
                            )

                        val lonFinal =
                            location.getDouble(0)

                        val latFinal =
                            location.getDouble(1)

                        val puntoFinal =

                            LatLng(
                                latFinal,
                                lonFinal
                            )

                        // =====================================================
                        // GEOMETRY
                        // =====================================================
                        val geometry =
                            route.getJSONObject("geometry")

                        val coordinates =
                            geometry.getJSONArray(
                                "coordinates"
                            )

                        // =====================================================
                        // CONVERTIR PUNTOS
                        // =====================================================
                        val puntos =
                            mutableListOf<LatLng>()

                        for (i in 0 until coordinates.length()) {

                            val punto =
                                coordinates.getJSONArray(i)

                            val lon =
                                punto.getDouble(0)

                            val lat =
                                punto.getDouble(1)

                            puntos.add(
                                LatLng(lat, lon)
                            )
                        }

                        // =====================================================
                        // REEMPLAZAR ÚLTIMO PUNTO
                        // =====================================================
                        if (puntos.isNotEmpty()) {

                            puntos[puntos.size - 1] =
                                puntoFinal
                        }

                        runOnUiThread {

                            // =====================================================
                            // LIMPIAR
                            // =====================================================
                            polylineRuta?.remove()

                            // =====================================================
                            // DIBUJAR RUTA
                            // =====================================================
                            polylineRuta =

                                mMap.addPolyline(

                                    PolylineOptions()
                                        .addAll(puntos)
                                        .width(18f)
                                        .color(Color.GREEN)
                                        .geodesic(true)
                                        .jointType(
                                            JointType.ROUND
                                        )
                                        .pattern(
                                            listOf(
                                                Dot(),
                                                Gap(18f)
                                            )
                                        )
                                )

                            // =====================================================
                            // MOSTRAR INFO WINDOW
                            // =====================================================
                            markerOriginal.showInfoWindow()

                            // =====================================================
                            // MOVER CÁMARA
                            // =====================================================
                            mMap.animateCamera(

                                CameraUpdateFactory
                                    .newLatLngZoom(
                                        puntoFinal,
                                        19f
                                    )
                            )

                            Toast.makeText(
                                this@MapActivity,
                                "Ruta segura encontrada",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()

                        runOnUiThread {

                            Toast.makeText(
                                this@MapActivity,
                                "Error procesando datos",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            })
    }

    // =====================================================
// BUSCAR ENTRADA PRINCIPAL REAL
// =====================================================
    private fun buscarEntradaPrincipal(
        origen: LatLng,
        marker: Marker,
        callback: (LatLng) -> Unit
    ) {

        Thread {

            try {

                val lat =
                    marker.position.latitude

                val lon =
                    marker.position.longitude

                // =====================================================
                // BUSCAR CALLES ALREDEDOR
                // =====================================================
                val query = """
            [out:json];

            (
              way(around:60,$lat,$lon)["highway"];
            );

            out geom;
        """.trimIndent()

                val request =

                    Request.Builder()
                        .url(
                            "https://overpass-api.de/api/interpreter"
                        )
                        .post(
                            FormBody.Builder()
                                .add("data", query)
                                .build()
                        )
                        .build()

                val response =
                    client.newCall(request)
                        .execute()

                val body =
                    response.body?.string()

                if (body.isNullOrEmpty()) {

                    runOnUiThread {

                        callback(marker.position)
                    }

                    return@Thread
                }

                val json =
                    JSONObject(body)

                val elements =
                    json.getJSONArray("elements")

                // =====================================================
                // MEJOR ENTRADA
                // =====================================================
                var mejorPunto =
                    marker.position

                var menorDistancia =
                    Double.MAX_VALUE

                // =====================================================
                // RECORRER CALLES
                // =====================================================
                for (i in 0 until elements.length()) {

                    val obj =
                        elements.getJSONObject(i)

                    if (!obj.has("geometry")) {
                        continue
                    }

                    val geometry =
                        obj.getJSONArray("geometry")

                    for (j in 0 until geometry.length()) {

                        val punto =
                            geometry.getJSONObject(j)

                        val pLat =
                            punto.getDouble("lat")

                        val pLon =
                            punto.getDouble("lon")

                        val resultado =
                            FloatArray(1)

                        // =====================================================
                        // DISTANCIA AL USUARIO
                        // =====================================================
                        android.location.Location.distanceBetween(
                            origen.latitude,
                            origen.longitude,
                            pLat,
                            pLon,
                            resultado
                        )

                        val distancia =
                            resultado[0].toDouble()

                        // =====================================================
                        // ELEGIR CALLE MÁS CERCANA
                        // =====================================================
                        if (distancia < menorDistancia) {

                            menorDistancia =
                                distancia

                            mejorPunto =
                                LatLng(
                                    pLat,
                                    pLon
                                )
                        }
                    }
                }

                runOnUiThread {

                    callback(mejorPunto)
                }

            } catch (e: Exception) {

                e.printStackTrace()

                runOnUiThread {

                    callback(marker.position)
                }
            }

        }.start()
    }


    // =====================================================
// IR A ZONA SEGURA MAS CERCANA REAL
// =====================================================
    @SuppressLint("MissingPermission")
    private fun irZonaSeguraMasCercana() {

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->

                if (location == null) {

                    Toast.makeText(
                        this,
                        "No se pudo obtener ubicación",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }

                val origen =
                    LatLng(
                        location.latitude,
                        location.longitude
                    )

                val query = """
            [out:json][timeout:25];

            (
              node["amenity"="police"](around:5000,${location.latitude},${location.longitude});
              node["amenity"="townhall"](around:5000,${location.latitude},${location.longitude});
              node["building"="guardhouse"](around:5000,${location.latitude},${location.longitude});
              node["security"="guardhouse"](around:5000,${location.latitude},${location.longitude});
            );

            out body;
        """.trimIndent()

                val request =
                    Request.Builder()
                        .url("https://overpass-api.de/api/interpreter")
                        .post(
                            FormBody.Builder()
                                .add("data", query)
                                .build()
                        )
                        .addHeader(
                            "User-Agent",
                            "Android-App"
                        )
                        .build()

                client.newCall(request)
                    .enqueue(object : Callback {

                        override fun onFailure(
                            call: okhttp3.Call,
                            e: IOException
                        ) {

                            runOnUiThread {

                                Toast.makeText(
                                    this@MapActivity,
                                    "Error buscando zonas",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onResponse(
                            call: okhttp3.Call,
                            response: Response
                        ) {

                            try {

                                val body =
                                    response.body?.string()

                                if (
                                    body.isNullOrEmpty()
                                ) return

                                val json =
                                    JSONObject(body)

                                val elements =
                                    json.getJSONArray("elements")

                                if (
                                    elements.length() == 0
                                ) {

                                    runOnUiThread {

                                        Toast.makeText(
                                            this@MapActivity,
                                            "No se encontraron zonas",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                    return
                                }

                                calcularMejorRuta(
                                    origen,
                                    elements
                                )

                            } catch (e: Exception) {

                                e.printStackTrace()
                            }
                        }
                    })
            }
    }


    // =====================================================
// CALCULAR MEJOR RUTA REAL
// =====================================================
    private fun calcularMejorRuta(
        origen: LatLng,
        elements: JSONArray
    ) {

        Thread {

            try {

                var mejorDistancia =
                    Double.MAX_VALUE

                var mejorRuta:
                        List<LatLng> = emptyList()

                var mejorDestino =
                    LatLng(0.0, 0.0)

                var mejorNombre =
                    "Zona Segura"

                for (
                i in 0 until elements.length()
                ) {

                    try {

                        val obj =
                            elements.getJSONObject(i)

                        if (
                            !obj.has("lat")
                            ||
                            !obj.has("lon")
                        ) continue

                        val lat =
                            obj.getDouble("lat")

                        val lon =
                            obj.getDouble("lon")

                        val destino =
                            LatLng(lat, lon)

                        val tags =
                            obj.optJSONObject("tags")

                        val nombre =
                            tags?.optString(
                                "name",
                                "Zona Segura"
                            ) ?: "Zona Segura"

                        val url =
                            "https://router.project-osrm.org/route/v1/foot/" +
                                    "${origen.longitude},${origen.latitude};" +
                                    "${destino.longitude},${destino.latitude}" +
                                    "?overview=full&geometries=polyline"

                        val request =
                            Request.Builder()
                                .url(url)
                                .build()

                        val response =
                            client.newCall(request)
                                .execute()

                        val body =
                            response.body?.string()

                        if (
                            body.isNullOrEmpty()
                        ) continue

                        val json =
                            JSONObject(body)

                        val routes =
                            json.getJSONArray("routes")

                        if (
                            routes.length() == 0
                        ) continue

                        val route =
                            routes.getJSONObject(0)

                        val distance =
                            route.getDouble("distance")

                        if (
                            distance < mejorDistancia
                        ) {

                            mejorDistancia =
                                distance

                            mejorDestino =
                                destino

                            mejorNombre =
                                nombre

                            val geometry =
                                route.getString("geometry")

                            mejorRuta =
                                decodificarPolyline(
                                    geometry
                                )
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }

                runOnUiThread {

                    mostrarRutaProfesional(
                        mejorRuta,
                        mejorDestino,
                        mejorNombre,
                        mejorDistancia
                    )
                }

            } catch (e: Exception) {

                e.printStackTrace()
            }

        }.start()
    }




    // =====================================================
// RUTA INTELIGENTE TIPO NAVEGACION
// =====================================================
    private fun mostrarRutaReal(
        origen: LatLng,
        destino: LatLng,
        nombre: String
    ) {

        val url =
            "https://router.project-osrm.org/route/v1/foot/" +
                    "${origen.longitude},${origen.latitude};" +
                    "${destino.longitude},${destino.latitude}" +
                    "?overview=full" +
                    "&geometries=polyline" +
                    "&steps=true"

        val request =
            Request.Builder()
                .url(url)
                .build()

        client.newCall(request)
            .enqueue(object : Callback {

                override fun onFailure(
                    call: okhttp3.Call,
                    e: IOException
                ) {

                    runOnUiThread {

                        Toast.makeText(
                            this@MapActivity,
                            "Error obteniendo ruta",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onResponse(
                    call: okhttp3.Call,
                    response: Response
                ) {

                    try {

                        val body =
                            response.body?.string()

                        if (body.isNullOrEmpty()) {

                            runOnUiThread {

                                Toast.makeText(
                                    this@MapActivity,
                                    "Respuesta vacía",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            return
                        }

                        val json =
                            JSONObject(body)

                        val routes =
                            json.getJSONArray("routes")

                        if (routes.length() == 0) {

                            runOnUiThread {

                                Toast.makeText(
                                    this@MapActivity,
                                    "No se encontró ruta",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            return
                        }

                        val route =
                            routes.getJSONObject(0)

                        val geometry =
                            route.getString("geometry")

                        val puntos =
                            decodificarPolyline(
                                geometry
                            )

                        // =====================================================
                        // OBTENER CALLES
                        // =====================================================
                        val legs =
                            route.getJSONArray("legs")

                        val leg =
                            legs.getJSONObject(0)

                        val steps =
                            leg.getJSONArray("steps")

                        runOnUiThread {

                            // =====================================================
                            // LIMPIAR
                            // =====================================================
                            polylineRuta?.remove()

                            marcadorZonaSegura?.remove()

                            // =====================================================
                            // ELIMINAR MARCADORES ANTERIORES
                            // =====================================================
                            for (m in listaMarcadoresServicios) {
                                m.remove()
                            }

                            // =====================================================
                            // RUTA PRINCIPAL AZUL
                            // =====================================================
                            polylineRuta =

                                mMap.addPolyline(

                                    PolylineOptions()
                                        .addAll(puntos)
                                        .width(20f)
                                        .color(Color.parseColor("#1565C0"))
                                        .geodesic(true)
                                        .jointType(JointType.ROUND)
                                        .pattern(
                                            listOf(
                                                Dot(),
                                                Gap(12f)
                                            )
                                        )
                                )

                            // =====================================================
                            // MARCADORES DE CALLES
                            // =====================================================
                            for (
                            i in 0 until
                                    steps.length()
                            ) {

                                try {

                                    val step =
                                        steps.getJSONObject(i)

                                    val maneuver =
                                        step.getJSONObject("maneuver")

                                    val location =
                                        maneuver.getJSONArray("location")

                                    val lon =
                                        location.getDouble(0)

                                    val lat =
                                        location.getDouble(1)

                                    val name =
                                        step.optString(
                                            "name",
                                            "Continuar"
                                        )

                                    if (name.isNotEmpty()) {

                                        mMap.addMarker(

                                            MarkerOptions()
                                                .position(
                                                    LatLng(
                                                        lat,
                                                        lon
                                                    )
                                                )
                                                .title(name)
                                                .icon(
                                                    BitmapDescriptorFactory
                                                        .defaultMarker(
                                                            BitmapDescriptorFactory
                                                                .HUE_AZURE
                                                        )
                                                )
                                        )
                                    }

                                } catch (e: Exception) {

                                    e.printStackTrace()
                                }
                            }

                            // =====================================================
                            // ENTRADA PRINCIPAL
                            // =====================================================
                            val puntoFinal =

                                if (puntos.size > 10)

                                    puntos[puntos.size - 10]

                                else

                                    puntos.last()

                            marcadorZonaSegura =

                                mMap.addMarker(

                                    MarkerOptions()
                                        .position(puntoFinal)
                                        .title("🛡 $nombre")
                                        .snippet(
                                            "Entrada principal"
                                        )
                                        .icon(
                                            BitmapDescriptorFactory
                                                .defaultMarker(
                                                    BitmapDescriptorFactory
                                                        .HUE_GREEN
                                                )
                                        )
                                )

                            marcadorZonaSegura
                                ?.showInfoWindow()

                            // =====================================================
                            // CAMARA
                            // =====================================================
                            mMap.animateCamera(

                                CameraUpdateFactory
                                    .newLatLngZoom(
                                        puntoFinal,
                                        18f
                                    )
                            )

                            Toast.makeText(
                                this@MapActivity,
                                "Ruta encontrada",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()

                        runOnUiThread {

                            Toast.makeText(
                                this@MapActivity,
                                "Error procesando datos",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            })
    }





    // =====================================================
// MOSTRAR RUTA PROFESIONAL
// =====================================================
    private fun mostrarRutaProfesional(
        puntos: List<LatLng>,
        destino: LatLng,
        nombre: String,
        distancia: Double
    ) {

        polylineRuta?.remove()

        marcadorZonaSegura?.remove()

        // =====================================================
        // RUTA PRINCIPAL
        // =====================================================
        polylineRuta =

            mMap.addPolyline(

                PolylineOptions()
                    .addAll(puntos)
                    .width(18f)
                    .color(Color.BLUE)
                    .geodesic(true)
                    .jointType(
                        JointType.ROUND
                    )
                    .pattern(
                        listOf(
                            Dot(),
                            Gap(16f)
                        )
                    )
            )

        // =====================================================
        // DESTINO
        // =====================================================
        marcadorZonaSegura =

            mMap.addMarker(

                MarkerOptions()
                    .position(destino)
                    .title("🛡 $nombre")
                    .snippet(
                        "Ruta más rápida encontrada"
                    )
                    .icon(
                        BitmapDescriptorFactory
                            .defaultMarker(
                                BitmapDescriptorFactory
                                    .HUE_GREEN
                            )
                    )
            )

        // =====================================================
        // ENFOCAR RUTA
        // =====================================================
        mMap.animateCamera(

            CameraUpdateFactory
                .newLatLngZoom(
                    destino,
                    17f
                )
        )

        marcadorZonaSegura
            ?.showInfoWindow()

        Toast.makeText(
            this,
            "Ruta más cercana: ${
                distancia.toInt()
            } metros",
            Toast.LENGTH_LONG
        ).show()
    }




    // =====================================================
// OBTENER RUTA SEGURA REAL
// =====================================================
    private fun obtenerRutaOSRM(
        origen: LatLng,
        destino: LatLng,
        nombre: String
    ) {

        Thread {

            try {

                // =====================================================
                // BUSCAR CALLES CERCA DEL DESTINO
                // =====================================================
                val query = """
            [out:json][timeout:25];

            (
              way["highway"](around:80,
              ${destino.latitude},
              ${destino.longitude});
            );

            out geom;
        """.trimIndent()

                val requestCalles =

                    Request.Builder()
                        .url(
                            "https://overpass-api.de/api/interpreter"
                        )
                        .post(
                            FormBody.Builder()
                                .add(
                                    "data",
                                    query
                                )
                                .build()
                        )
                        .build()

                val responseCalles =
                    client.newCall(requestCalles)
                        .execute()

                val bodyCalles =
                    responseCalles.body?.string()

                if (
                    bodyCalles.isNullOrEmpty()
                ) {

                    runOnUiThread {

                        Toast.makeText(
                            this,
                            "No se encontraron calles",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    return@Thread
                }

                val jsonCalles =
                    JSONObject(bodyCalles)

                val elements =
                    jsonCalles.getJSONArray(
                        "elements"
                    )

                // =====================================================
                // BUSCAR PUNTO MAS CERCANO
                // =====================================================
                var mejorPunto =
                    destino

                var menorDistancia =
                    Double.MAX_VALUE

                for (
                i in 0 until
                        elements.length()
                ) {

                    try {

                        val obj =
                            elements.getJSONObject(i)

                        // =====================================================
                        // VALIDAR GEOMETRY
                        // =====================================================
                        if (
                            !obj.has("geometry")
                            ||
                            obj.isNull("geometry")
                        ) {
                            continue
                        }

                        val geometry =
                            obj.getJSONArray(
                                "geometry"
                            )

                        for (
                        j in 0 until
                                geometry.length()
                        ) {

                            val punto =
                                geometry.getJSONObject(j)

                            val lat =
                                punto.getDouble("lat")

                            val lon =
                                punto.getDouble("lon")

                            val resultado =
                                FloatArray(1)

                            android.location.Location.distanceBetween(
                                destino.latitude,
                                destino.longitude,
                                lat,
                                lon,
                                resultado
                            )

                            val distancia =
                                resultado[0].toDouble()

                            // =====================================================
                            // ELEGIR EL MAS CERCANO
                            // =====================================================
                            if (
                                distancia <
                                menorDistancia
                            ) {

                                menorDistancia =
                                    distancia

                                mejorPunto =
                                    LatLng(
                                        lat,
                                        lon
                                    )
                            }
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }

                // =====================================================
                // URL OSRM
                // =====================================================
                val url =
                    "https://router.project-osrm.org/route/v1/foot/" +
                            "${origen.longitude},${origen.latitude};" +
                            "${mejorPunto.longitude},${mejorPunto.latitude}" +
                            "?overview=full" +
                            "&geometries=polyline" +
                            "&steps=true"

                val requestRuta =

                    Request.Builder()
                        .url(url)
                        .build()

                val responseRuta =
                    client.newCall(requestRuta)
                        .execute()

                val bodyRuta =
                    responseRuta.body?.string()

                if (
                    bodyRuta.isNullOrEmpty()
                ) {

                    runOnUiThread {

                        Toast.makeText(
                            this,
                            "Error obteniendo ruta",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    return@Thread
                }

                val jsonRuta =
                    JSONObject(bodyRuta)

                val routes =
                    jsonRuta.getJSONArray(
                        "routes"
                    )

                if (
                    routes.length() == 0
                ) {

                    runOnUiThread {

                        Toast.makeText(
                            this,
                            "No se encontró ruta",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    return@Thread
                }

                val route =
                    routes.getJSONObject(0)

                val geometry =
                    route.getString(
                        "geometry"
                    )

                val puntos =
                    decodificarPolyline(
                        geometry
                    )

                runOnUiThread {

                    // =====================================================
                    // LIMPIAR
                    // =====================================================
                    polylineRuta?.remove()

                    marcadorZonaSegura?.remove()

                    // =====================================================
                    // MARCADOR DESTINO
                    // =====================================================
                    marcadorZonaSegura =

                        mMap.addMarker(

                            MarkerOptions()
                                .position(
                                    mejorPunto
                                )
                                .title(
                                    "🛡 $nombre"
                                )
                                .snippet(
                                    "Entrada principal aproximada"
                                )
                                .icon(
                                    BitmapDescriptorFactory
                                        .defaultMarker(
                                            BitmapDescriptorFactory
                                                .HUE_GREEN
                                        )
                                )
                        )

                    // =====================================================
                    // RUTA SEGURA
                    // =====================================================
                    polylineRuta =

                        mMap.addPolyline(

                            PolylineOptions()
                                .addAll(
                                    puntos
                                )
                                .width(18f)
                                .color(
                                    Color.GREEN
                                )
                                .geodesic(true)
                                .clickable(true)
                                .jointType(
                                    JointType.ROUND
                                )
                        )

                    // =====================================================
                    // CAMARA
                    // =====================================================
                    mMap.animateCamera(

                        CameraUpdateFactory
                            .newLatLngZoom(
                                mejorPunto,
                                18f
                            )
                    )

                    marcadorZonaSegura
                        ?.showInfoWindow()

                    Toast.makeText(
                        this,
                        "Ruta segura encontrada",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                e.printStackTrace()

                runOnUiThread {

                    Toast.makeText(
                        this,
                        "Error procesando datos",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }.start()
    }



    // =====================================================
// OBTENER ENTRADA PRINCIPAL REAL
// =====================================================
    private fun obtenerEntradaPrincipal(
        destino: LatLng,
        callback: (LatLng) -> Unit
    ) {

        Thread {

            try {

                // =====================================================
                // BUSCAR CALLES Y ENTRADAS REALES
                // =====================================================
                val query = """
                [out:json][timeout:25];

                (
                  node["entrance"](around:120,${
                    destino.latitude
                },${destino.longitude});

                  node["highway"](around:120,${
                    destino.latitude
                },${destino.longitude});

                  way["highway"](around:120,${
                    destino.latitude
                },${destino.longitude});
                );

                out center;
            """.trimIndent()

                val request =

                    Request.Builder()
                        .url(
                            "https://overpass-api.de/api/interpreter"
                        )
                        .post(
                            FormBody.Builder()
                                .add(
                                    "data",
                                    query
                                )
                                .build()
                        )
                        .build()

                val response =
                    client.newCall(request)
                        .execute()

                val body =
                    response.body?.string()

                if (
                    body.isNullOrEmpty()
                ) {

                    runOnUiThread {

                        callback(destino)
                    }

                    return@Thread
                }

                val json =
                    JSONObject(body)

                val elements =
                    json.getJSONArray("elements")

                // =====================================================
                // SI NO HAY RESULTADOS
                // =====================================================
                if (
                    elements.length() == 0
                ) {

                    runOnUiThread {

                        callback(destino)
                    }

                    return@Thread
                }

                var mejorPunto =
                    destino

                var mejorDistancia =
                    Double.MAX_VALUE

                // =====================================================
                // BUSCAR PUNTO MAS CERCANO A CALLE
                // =====================================================
                for (
                i in 0 until
                        elements.length()
                ) {

                    try {

                        val obj =
                            elements.getJSONObject(i)

                        var lat = 0.0
                        var lon = 0.0

                        if (
                            obj.has("lat")
                        ) {

                            lat =
                                obj.getDouble("lat")

                            lon =
                                obj.getDouble("lon")
                        }

                        else if (
                            obj.has("center")
                        ) {

                            val center =
                                obj.getJSONObject(
                                    "center"
                                )

                            lat =
                                center.getDouble(
                                    "lat"
                                )

                            lon =
                                center.getDouble(
                                    "lon"
                                )
                        }

                        val resultado =
                            FloatArray(1)

                        android.location.Location.distanceBetween(
                            destino.latitude,
                            destino.longitude,
                            lat,
                            lon,
                            resultado
                        )

                        val distancia =
                            resultado[0]

                        // =====================================================
                        // ELEGIR EL MAS CERCANO
                        // =====================================================
                        if (
                            distancia <
                            mejorDistancia
                        ) {

                            mejorDistancia =
                                distancia.toDouble()

                            mejorPunto =
                                LatLng(
                                    lat,
                                    lon
                                )
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }

                runOnUiThread {

                    callback(mejorPunto)
                }

            } catch (e: Exception) {

                e.printStackTrace()

                runOnUiThread {

                    callback(destino)
                }
            }

        }.start()
    }


    // =====================================================
// CALCULAR RUTA OSRM
// =====================================================
    private fun calcularRutaOSRM(
        origen: LatLng,
        destino: LatLng,
        nombre: String
    ) {

        val url =
            "https://router.project-osrm.org/route/v1/foot/" +
                    "${origen.longitude},${origen.latitude};" +
                    "${destino.longitude},${destino.latitude}" +
                    "?overview=full&geometries=polyline"

        val request =
            Request.Builder()
                .url(url)
                .build()

        client.newCall(request)
            .enqueue(object : Callback {

                // =====================================================
                // ERROR
                // =====================================================
                override fun onFailure(
                    call: okhttp3.Call,
                    e: IOException
                ) {

                    runOnUiThread {

                        Toast.makeText(
                            this@MapActivity,
                            "Error obteniendo ruta",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                // =====================================================
                // RESPUESTA
                // =====================================================
                override fun onResponse(
                    call: okhttp3.Call,
                    response: Response
                ) {

                    try {

                        val body =
                            response.body?.string()

                        if (body.isNullOrEmpty()) {
                            return
                        }

                        val json =
                            JSONObject(body)

                        val routes =
                            json.getJSONArray("routes")

                        if (routes.length() == 0) {

                            runOnUiThread {

                                Toast.makeText(
                                    this@MapActivity,
                                    "No se encontró ruta",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            return
                        }

                        val route =
                            routes.getJSONObject(0)

                        val geometry =
                            route.getString("geometry")

                        val puntos =
                            decodificarPolyline(
                                geometry
                            )

                        runOnUiThread {

                            // =====================================================
                            // LIMPIAR
                            // =====================================================
                            polylineRuta?.remove()

                            marcadorZonaSegura?.remove()

                            // =====================================================
                            // MARCADOR
                            // =====================================================
                            marcadorZonaSegura =

                                mMap.addMarker(

                                    MarkerOptions()
                                        .position(destino)
                                        .title("🛡 $nombre")
                                        .snippet(
                                            "Entrada principal"
                                        )
                                        .icon(
                                            BitmapDescriptorFactory
                                                .defaultMarker(
                                                    BitmapDescriptorFactory
                                                        .HUE_GREEN
                                                )
                                        )
                                )

                            // =====================================================
                            // RUTA
                            // =====================================================
                            polylineRuta =

                                mMap.addPolyline(

                                    PolylineOptions()
                                        .addAll(puntos)
                                        .width(14f)
                                        .color(Color.GREEN)
                                        .geodesic(true)
                                        .jointType(
                                            JointType.ROUND
                                        )
                                )

                            // =====================================================
                            // CAMARA
                            // =====================================================
                            mMap.animateCamera(

                                CameraUpdateFactory
                                    .newLatLngZoom(
                                        destino,
                                        17f
                                    )
                            )

                            marcadorZonaSegura
                                ?.showInfoWindow()

                            Toast.makeText(
                                this@MapActivity,
                                "Ruta encontrada",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()

                        runOnUiThread {

                            Toast.makeText(
                                this@MapActivity,
                                "Error procesando ruta",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            })
    }

    // =====================================================
// DECODIFICAR POLYLINE
// =====================================================
    private fun decodificarPolyline(
        encoded: String
    ): List<LatLng> {

        val poly = ArrayList<LatLng>()

        var index = 0
        val len = encoded.length

        var lat = 0
        var lng = 0

        while (index < len) {

            var b: Int
            var shift = 0
            var result = 0

            do {

                b =
                    encoded[index++].code - 63

                result =
                    result or
                            ((b and 0x1f) shl shift)

                shift += 5

            } while (b >= 0x20)

            val dlat =
                if (result and 1 != 0)
                    (result shr 1).inv()
                else
                    result shr 1

            lat += dlat

            shift = 0
            result = 0

            do {

                b =
                    encoded[index++].code - 63

                result =
                    result or
                            ((b and 0x1f) shl shift)

                shift += 5

            } while (b >= 0x20)

            val dlng =
                if (result and 1 != 0)
                    (result shr 1).inv()
                else
                    result shr 1

            lng += dlng

            val p =
                LatLng(
                    lat / 1E5,
                    lng / 1E5
                )

            poly.add(p)
        }

        return poly
    }



    // =====================================================
    // MOSTRAR DISTRITO
    // =====================================================
    private fun mostrarDistrito(
        distrito: String
    ) {

        currentMarker?.remove()

        mMap.clear()

        marcadorUbicacionActual?.let {

            marcadorUbicacionActual =

                mMap.addMarker(

                    MarkerOptions()
                        .position(
                            it.position
                        )
                        .title(
                            "📍 Estoy aquí"
                        )
                        .snippet(
                            "Tu ubicación actual"
                        )
                        .icon(
                            BitmapDescriptorFactory
                                .defaultMarker(
                                    BitmapDescriptorFactory
                                        .HUE_BLUE
                                )
                        )
                )

            circuloUbicacion =

                mMap.addCircle(

                    CircleOptions()
                        .center(it.position)
                        .radius(45.0)
                        .strokeWidth(5f)
                        .strokeColor(
                            0xFF0288D1.toInt()
                        )
                        .fillColor(
                            0x220288D1
                                .toInt()
                        )
                )
        }

        pintarReportes()

        val coords = mapOf(

            "ATE" to
                    LatLng(-12.04318, -76.93597),

            "BARRANCO" to
                    LatLng(-12.14199, -77.02178),

            "BREÑA" to
                    LatLng(-12.05659, -77.05085),

            "CARABAYLLO" to
                    LatLng(-11.87611, -77.03472),

            "CHACLACAYO" to
                    LatLng(-11.97861, -76.76750),

            "CHORRILLOS" to
                    LatLng(-12.17000, -77.02361),

            "CIENEGUILLA" to
                    LatLng(-12.11722, -76.81611),

            "COMAS" to
                    LatLng(-11.93298, -77.04085),

            "EL AGUSTINO" to
                    LatLng(-12.04333, -76.99583),

            "INDEPENDENCIA" to
                    LatLng(-11.99194, -77.05722),

            "JESUS MARIA" to
                    LatLng(-12.07556, -77.04528),

            "LA MOLINA" to
                    LatLng(-12.08361, -76.92861),

            "LA VICTORIA" to
                    LatLng(-12.06513, -77.03397),

            "LIMA" to
                    LatLng(-12.04637, -77.04279),

            "LINCE" to
                    LatLng(-12.08417, -77.03000),

            "LOS OLIVOS" to
                    LatLng(-11.99167, -77.07028),

            "LURIGANCHO" to
                    LatLng(-11.93500, -76.69722),

            "LURIN" to
                    LatLng(-12.27528, -76.87528),

            "MAGDALENA" to
                    LatLng(-12.09167, -77.06750),

            "MIRAFLORES" to
                    LatLng(-12.12111, -77.02972),

            "PACHACAMAC" to
                    LatLng(-12.22861, -76.85722),

            "PUEBLO LIBRE" to
                    LatLng(-12.07694, -77.06750),

            "PUENTE PIEDRA" to
                    LatLng(-11.86667, -77.07694),

            "RIMAC" to
                    LatLng(-12.03528, -77.02861),

            "SAN BARTOLO" to
                    LatLng(-12.38972, -76.78056),

            "SAN BORJA" to
                    LatLng(-12.10722, -76.99722),

            "SAN ISIDRO" to
                    LatLng(-12.09750, -77.03611),

            "SAN JUAN DE LURIGANCHO" to
                    LatLng(-11.95861, -76.99861),

            "SAN JUAN DE MIRAFLORES" to
                    LatLng(-12.15611, -76.96861),

            "SAN LUIS" to
                    LatLng(-12.07278, -76.99472),

            "SAN MARTIN DE PORRES" to
                    LatLng(-12.00000, -77.08333),

            "SAN MIGUEL" to
                    LatLng(-12.09250, -77.07806),

            "SANTA ANITA" to
                    LatLng(-12.04333, -76.97111),

            "SANTIAGO DE SURCO" to
                    LatLng(-12.13972, -76.99528),

            "SURQUILLO" to
                    LatLng(-12.11750, -77.02167),

            "VILLA EL SALVADOR" to
                    LatLng(-12.22056, -76.94556),

            "VILLA MARIA DEL TRIUNFO" to
                    LatLng(-12.16250, -76.94361)
        )

        coords[distrito]?.let {

            currentMarker =

                mMap.addMarker(

                    MarkerOptions()
                        .position(it)
                        .title(distrito)
                        .icon(
                            BitmapDescriptorFactory
                                .defaultMarker(
                                    BitmapDescriptorFactory
                                        .HUE_GREEN
                                )
                        )
                )

            mMap.animateCamera(

                CameraUpdateFactory
                    .newLatLngZoom(
                        it,
                        13f
                    )
            )
        }
    }
}