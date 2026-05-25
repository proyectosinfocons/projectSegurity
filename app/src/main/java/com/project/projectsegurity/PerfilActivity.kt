//package com.project.projectsegurity
//
//import android.Manifest
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.telephony.TelephonyManager
//import android.view.View
//import android.widget.*
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.project.projectsegurity.adapter.ContactoAdapter
//import com.project.projectsegurity.databinding.ActivityLoginBinding
//import com.project.projectsegurity.model.ContactoEmergencia
//import okhttp3.*
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import org.json.JSONArray
//import android.content.pm.PackageManager
//import android.util.Patterns
//import org.json.JSONObject
//
//class PerfilActivity : AppCompatActivity() {
//
//
//    private lateinit var binding: ActivityLoginBinding
//    private lateinit var etNombre: EditText
//    private lateinit var etApellido: EditText
//    private lateinit var etCorreo: EditText
//
//    private lateinit var btnGuardar: Button
//    private lateinit var btnAgregarContacto: Button
//    private lateinit var tvContactos: TextView
//    private lateinit var btnSalir: Button
//
//    private lateinit var adapter: ContactoAdapter
//
//    private val client = OkHttpClient()
//    private val listaContactos = mutableListOf<ContactoEmergencia>()
//
//    private val contactosNuevos = mutableListOf<ContactoEmergencia>()
//    private val contactosEliminados = mutableListOf<Long>()
//    private val contactosEditados = mutableListOf<ContactoEmergencia>()
//
//    private var contactoSeleccionadoId: Long? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_perfil)
//
//        etNombre = findViewById(R.id.etNombre)
//        etApellido = findViewById(R.id.etApellido)
//        etCorreo = findViewById(R.id.etCorreo)
//
//        btnGuardar = findViewById(R.id.btnGuardar)
//        btnAgregarContacto = findViewById(R.id.btnAgregarContacto)
//        tvContactos = findViewById(R.id.tvContactos)
//        btnSalir = findViewById(R.id.btnSalir)
//
//        cargarPerfil()
//        cargarContactos()
//
//        btnGuardar.setOnClickListener {
//            actualizarPerfil()
//        }
//
//        btnAgregarContacto.setOnClickListener {
//            mostrarContactosPopup()
//        }
//
//        tvContactos.setOnClickListener {
//            mostrarContactosPopup()
//        }
//
//        btnSalir.setOnClickListener {
//            startActivity(Intent(this, MainMenuActivity::class.java))
//            finish()
//        }
//
//
//    }
//
//
//
//
//
//    private fun getToken(): String {
//        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
//        return prefs.getString("TOKEN", "") ?: ""
//    }
//
//    private fun cargarPerfil() {
//        Thread {
//            val request = Request.Builder()
//                .url("http://192.168.18.238:8080/api/usuarios/perfil")
//                .addHeader("Authorization", "Bearer ${getToken()}")
//                .build()
//
//            val response = client.newCall(request).execute()
//            val body = response.body?.string()
//
//            runOnUiThread {
//                val json = JSONObject(body)
//                etNombre.setText(json.optString("nombre"))
//                etApellido.setText(json.optString("apellido"))
//                etCorreo.setText(json.optString("correo"))
//            }
//        }.start()
//    }
//
//    private fun actualizarPerfil() {
//
//        val nombre = etNombre.text.toString().trim()
//        val apellido = etApellido.text.toString().trim()
//        val correo = etCorreo.text.toString().trim()
//
//        if (nombre.isEmpty()) {
//            etNombre.error = "Ingrese su nombre"
//            etNombre.requestFocus()
//            return
//        }
//
//        if (apellido.isEmpty()) {
//            etApellido.error = "Ingrese su apellido"
//            etApellido.requestFocus()
//            return
//        }
//
//        if (correo.isEmpty()) {
//            etCorreo.error = "Ingrese su correo"
//            etCorreo.requestFocus()
//            return
//        }
//
//        // =========================================
//        // VALIDAR FORMATO DE CORREO
//        // =========================================
//
//        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
//            etCorreo.error = "Ingrese un correo válido"
//            etCorreo.requestFocus()
//            return
//        }
//
//
//
//        val json = JSONObject()
//        json.put("nombre", etNombre.text.toString())
//        json.put("apellido", etApellido.text.toString())
//        json.put("correo", etCorreo.text.toString())
//
//        Thread {
//            val body = RequestBody.create(
//                "application/json".toMediaTypeOrNull(),
//                json.toString()
//            )
//
//            val request = Request.Builder()
//                .url("http://192.168.18.238:8080/api/usuarios/perfil")
//                .put(body)
//                .addHeader("Authorization", "Bearer ${getToken()}")
//                .build()
//
//            client.newCall(request).execute()
//
//            runOnUiThread {
//                Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
//            }
//        }.start()
//    }
//
//    private fun cargarContactos(callback: (() -> Unit)? = null) {
//        Thread {
//            val request = Request.Builder()
//                .url("http://192.168.18.238:8080/api/contactos")
//                .addHeader("Authorization", "Bearer ${getToken()}")
//                .build()
//
//            val response = client.newCall(request).execute()
//            val array = JSONArray(response.body?.string())
//
//            listaContactos.clear()
//
//            for (i in 0 until array.length()) {
//                val obj = array.getJSONObject(i)
//
//                val contacto = ContactoEmergencia(
//                    obj.getLong("id"),
//                    obj.getString("nombre"),
//                    obj.getString("telefono"),
//                    obj.optString("apellido"),
//                    obj.optString("relacion"),
//                    obj.getString("correo")
//                )
//
//                listaContactos.add(contacto)
//            }
//
//            runOnUiThread {
//                tvContactos.text = "${listaContactos.size}/4 contactos"
//                callback?.invoke()
//            }
//        }.start()
//    }
//
//    private fun registrarEdicion(
//        etNombre: EditText,
//        etTelefono: EditText,
//        etCorreo: EditText,
//        etApellido: EditText,
//        spRelacion: Spinner
//    ) {
//        val id = contactoSeleccionadoId ?: return
//
//        val contactoEditado = ContactoEmergencia(
//            id,
//            etNombre.text.toString(),
//            etTelefono.text.toString(),
//            etApellido.text.toString(),
//            spRelacion.selectedItem.toString(),
//            etCorreo.text.toString()
//        )
//
//        contactosEditados.removeAll { it.id == id }
//        contactosEditados.add(contactoEditado)
//
//        val index = listaContactos.indexOfFirst { it.id == id }
//        if (index != -1) {
//            listaContactos[index] = contactoEditado
//            adapter.notifyItemChanged(index)
//        }
//    }
//
//    private fun mostrarContactosPopup() {
//
//        val view = layoutInflater.inflate(R.layout.dialog_contactos, null)
//
//        val recycler = view.findViewById<RecyclerView>(R.id.recyclerContactos)
//        val etNombre = view.findViewById<EditText>(R.id.etNombreContacto)
//        val etTelefono = view.findViewById<EditText>(R.id.etTelefonoContacto)
//        val etCorreo = view.findViewById<EditText>(R.id.etCorreoContacto)
//        val etApellido = view.findViewById<EditText>(R.id.etApellidoContacto)
//        val spRelacion = view.findViewById<Spinner>(R.id.spRelacion)
//
//
//
//
//
//
//
//
//
//        val relaciones = arrayOf(
//            "Seleccione su relación",
//            "Padre","Madre","Hijo","Hija",
//            "Tío","Tía","Abuelo","Abuela",
//            "Sobrino","Sobrina","Amigo","Amiga","Otros"
//        )
//
//        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, relaciones)
//        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spRelacion.adapter = adapterSpinner
//
//        val btnAgregar = view.findViewById<Button>(R.id.btnGuardarContacto)
//        val btnActualizar = view.findViewById<Button>(R.id.btnActualizarContacto)
//        val btnGuardarLocal = view.findViewById<Button>(R.id.btnGuardarLocal)
//       // val btnCerrarLocal = view.findViewById<Button>(R.id.btnCerrarLocal)
//        val layoutForm = view.findViewById<LinearLayout>(R.id.layoutForm)
//
//        recycler.layoutManager = LinearLayoutManager(this)
//
//        adapter = ContactoAdapter(
//            listaContactos,
//            { contacto ->
//                listaContactos.remove(contacto)
//                contactosEliminados.add(contacto.id)
//                adapter.notifyDataSetChanged()
//            },
//            { contacto ->
//                etNombre.setText(contacto.nombre)
//                etTelefono.setText(contacto.telefono)
//                etCorreo.setText(contacto.correo)
//                etApellido.setText(contacto.apellido)
//
//                val posicion = relaciones.indexOf(contacto.relacion)
//
//                if (posicion >= 0) {
//                    spRelacion.setSelection(posicion)
//                } else {
//                    spRelacion.setSelection(0)
//                }
//
//                contactoSeleccionadoId = contacto.id
//
//                // =====================================================
//                // 🔥 CAMBIO INICIO
//                // 👉 Deshabilita botón AGREGAR CONTACTO al editar
//                // =====================================================
//                btnAgregar.isEnabled = false
//                // 🔥 CAMBIO FIN
//            }
//        )
//
//        recycler.adapter = adapter
//
//        val dialog = AlertDialog.Builder(this)
//            .setView(view)
//            .setPositiveButton("Cerrar", null)
//            .create()
//
//        cargarContactos {
//            adapter.notifyDataSetChanged()
//        }
//
//        btnAgregar.setOnClickListener {
//            layoutForm.visibility = View.VISIBLE
//            btnGuardarLocal.visibility = View.VISIBLE
//            //btnCerrarLocal.visibility = View.VISIBLE
//
//            btnAgregar.visibility = View.GONE
//            btnActualizar.visibility = View.GONE
//            recycler.visibility = View.GONE
//        }
//
//        btnGuardarLocal.setOnClickListener {
//
//            // =========================================
//            // VALIDAR MÁXIMO 4 CONTACTOS
//            // =========================================
//
//            if (listaContactos.size >= 4) {
//
//                Toast.makeText(
//                    this,
//                    "Solo puede registrar máximo 4 contactos",
//                    Toast.LENGTH_LONG
//                ).show()
//
//                return@setOnClickListener
//            }
//
//
//            // =========================================
//            // OBTENER VALORES
//            // =========================================
//
//            val nombre = etNombre.text.toString().trim()
//            val apellido = etApellido.text.toString().trim()
//            val telefono = etTelefono.text.toString().trim()
//            val correo = etCorreo.text.toString().trim()
//            val relacion = spRelacion.selectedItem.toString()
//
//            // =========================================
//            // VALIDAR NOMBRE
//            // =========================================
//
//            if (nombre.isEmpty()) {
//
//                etNombre.error = "Ingrese el nombre"
//                etNombre.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            // =========================================
//            // VALIDAR APELLIDO
//            // =========================================
//
//            if (apellido.isEmpty()) {
//
//                etApellido.error = "Ingrese el apellido"
//                etApellido.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            // =========================================
//            // VALIDAR TELÉFONO
//            // =========================================
//
//            if (telefono.isEmpty()) {
//
//                etTelefono.error = "Ingrese el teléfono"
//                etTelefono.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            // SOLO NÚMEROS Y 9 DÍGITOS
//            if (!telefono.matches(Regex("^\\d{9}$"))) {
//
//                etTelefono.error = "Ingrese un teléfono válido de 9 dígitos"
//                etTelefono.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            // =========================================
//            // VALIDAR CORREO
//            // =========================================
//
//            if (correo.isEmpty()) {
//
//                etCorreo.error = "Ingrese el correo"
//                etCorreo.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            // FORMATO EMAIL
//            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
//
//                etCorreo.error = "Ingrese un correo válido"
//                etCorreo.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            // =========================================
//            // VALIDAR RELACIÓN
//            // =========================================
//
//            if (relacion == "Seleccione su relación") {
//
//                Toast.makeText(
//                    this,
//                    "Seleccione una relación",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                return@setOnClickListener
//            }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//            val nuevo = ContactoEmergencia(
//                System.currentTimeMillis(),
//                etNombre.text.toString(),
//                etTelefono.text.toString(),
//                etApellido.text.toString(),
//                spRelacion.selectedItem.toString(),
//                etCorreo.text.toString()
//            )
//
//            contactosNuevos.add(nuevo)
//            listaContactos.add(nuevo)
//
//            adapter.notifyDataSetChanged()
//
//            etNombre.text.clear()
//            etApellido.text.clear()
//            etTelefono.text.clear()
//            etCorreo.text.clear()
//            spRelacion.setSelection(0)
//
//            layoutForm.visibility = View.VISIBLE
//            btnGuardarLocal.visibility = View.GONE
//            //btnCerrarLocal.visibility = View.GONE
//
//            btnAgregar.visibility = View.VISIBLE
//            btnActualizar.visibility = View.VISIBLE
//            recycler.visibility = View.VISIBLE
//        }
//
//        btnActualizar.setOnClickListener {
//
//            if (contactoSeleccionadoId != null) {
//                registrarEdicion(etNombre, etTelefono, etCorreo, etApellido, spRelacion)
//                contactoSeleccionadoId = null
//            }
//
//            Thread {
//
//                contactosNuevos.forEach {
//                    val json = JSONObject()
//                    json.put("nombre", it.nombre)
//                    json.put("apellido", it.apellido)
//                    json.put("relacion", it.relacion)
//                    json.put("telefono", it.telefono)
//                    json.put("correo", it.correo)
//
//                    val body = RequestBody.create(
//                        "application/json".toMediaTypeOrNull(),
//                        json.toString()
//                    )
//
//                    val request = Request.Builder()
//                        .url("http://192.168.18.238:8080/api/contactos")
//                        .post(body)
//                        .addHeader("Authorization", "Bearer ${getToken()}")
//                        .build()
//
//                    client.newCall(request).execute()
//                }
//
//                contactosEditados.forEach {
//                    val json = JSONObject()
//                    json.put("nombre", it.nombre)
//                    json.put("apellido", it.apellido)
//                    json.put("relacion", it.relacion)
//                    json.put("telefono", it.telefono)
//                    json.put("correo", it.correo)
//
//                    val body = RequestBody.create(
//                        "application/json".toMediaTypeOrNull(),
//                        json.toString()
//                    )
//
//                    val request = Request.Builder()
//                        .url("http://192.168.18.238:8080/api/contactos/actualizar/${it.id}")
//                        .put(body)
//                        .addHeader("Authorization", "Bearer ${getToken()}")
//                        .build()
//
//                    client.newCall(request).execute()
//                }
//
//                contactosEliminados.forEach {
//                    val request = Request.Builder()
//                        .url("http://192.168.18.238:8080/api/contactos/$it")
//                        .delete()
//                        .addHeader("Authorization", "Bearer ${getToken()}")
//                        .build()
//
//                    client.newCall(request).execute()
//                }
//
//                runOnUiThread {
//                    AlertDialog.Builder(this)
//                        .setTitle("Éxito")
//                        .setMessage("Se guardaron los cambios correctamente")
//                        .setPositiveButton("OK", null)
//                        .show()
//
//                    // =====================================================
//                    // 🔥 CAMBIO INICIO
//                    // 👉 Habilita nuevamente botón AGREGAR CONTACTO
//                    // =====================================================
//                    btnAgregar.isEnabled = true
//                    // 🔥 CAMBIO FIN
//
//
//
//                    // 🔥 CAMBIO 3: limpiar campos + reset spinner
//                    etNombre.text.clear()
//                    etApellido.text.clear()
//                    etTelefono.text.clear()
//                    etCorreo.text.clear()
//                    spRelacion.setSelection(0)
//
//
//                    contactosNuevos.clear()
//                    contactosEliminados.clear()
//                    contactosEditados.clear()
//
//                    cargarContactos { adapter.notifyDataSetChanged() }
//                }
//
//            }.start()
//        }
//
//        dialog.show()
//    }
//}


//package com.project.projectsegurity
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Patterns
//import android.view.View
//import android.widget.*
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.project.projectsegurity.adapter.ContactoAdapter
//import com.project.projectsegurity.databinding.ActivityLoginBinding
//import com.project.projectsegurity.model.ContactoEmergencia
//import okhttp3.*
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import org.json.JSONArray
//import org.json.JSONObject
//
//class PerfilActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityLoginBinding
//
//    private lateinit var etNombre: EditText
//    private lateinit var etApellido: EditText
//    private lateinit var etCorreo: EditText
//
//    private lateinit var btnGuardar: Button
//    private lateinit var btnAgregarContacto: Button
//    private lateinit var tvContactos: TextView
//    private lateinit var btnSalir: Button
//
//    private lateinit var adapter: ContactoAdapter
//
//    private val client = OkHttpClient()
//
//    private val listaContactos = mutableListOf<ContactoEmergencia>()
//
//    private val contactosNuevos = mutableListOf<ContactoEmergencia>()
//    private val contactosEliminados = mutableListOf<Long>()
//    private val contactosEditados = mutableListOf<ContactoEmergencia>()
//
//    private var contactoSeleccionadoId: Long? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_perfil)
//
//        etNombre = findViewById(R.id.etNombre)
//        etApellido = findViewById(R.id.etApellido)
//        etCorreo = findViewById(R.id.etCorreo)
//
//        btnGuardar = findViewById(R.id.btnGuardar)
//        btnAgregarContacto = findViewById(R.id.btnAgregarContacto)
//        tvContactos = findViewById(R.id.tvContactos)
//        btnSalir = findViewById(R.id.btnSalir)
//
//        cargarPerfil()
//        cargarContactos()
//
//        btnGuardar.setOnClickListener {
//            actualizarPerfil()
//        }
//
//        btnAgregarContacto.setOnClickListener {
//            mostrarContactosPopup()
//        }
//
//        tvContactos.setOnClickListener {
//            mostrarContactosPopup()
//        }
//
//        btnSalir.setOnClickListener {
//            startActivity(Intent(this, MainMenuActivity::class.java))
//            finish()
//        }
//    }
//
//    // =====================================================
//    // TOKEN
//    // =====================================================
//
//    private fun getToken(): String {
//
//        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
//
//        return prefs.getString("TOKEN", "") ?: ""
//    }
//
//    // =====================================================
//    // CARGAR PERFIL
//    // =====================================================
//
//    private fun cargarPerfil() {
//
//        Thread {
//
//            val request = Request.Builder()
//                .url("http://192.168.18.238:8080/api/usuarios/perfil")
//                .addHeader("Authorization", "Bearer ${getToken()}")
//                .build()
//
//            val response = client.newCall(request).execute()
//
//            val body = response.body?.string()
//
//            runOnUiThread {
//
//                val json = JSONObject(body)
//
//                etNombre.setText(json.optString("nombre"))
//                etApellido.setText(json.optString("apellido"))
//                etCorreo.setText(json.optString("correo"))
//            }
//
//        }.start()
//    }
//
//    // =====================================================
//    // ACTUALIZAR PERFIL
//    // =====================================================
//
//    private fun actualizarPerfil() {
//
//        val nombre = etNombre.text.toString().trim()
//        val apellido = etApellido.text.toString().trim()
//        val correo = etCorreo.text.toString().trim()
//
//
//
//        // 🔥 NUEVO: CONTRASEÑAS
//        val passwordNueva = findViewById<EditText>(R.id.etPasswordNueva).text.toString().trim()
//        val passwordConfirmar = findViewById<EditText>(R.id.etPasswordConfirmar).text.toString().trim()
//
//
//        // =========================================
//        // VALIDAR NOMBRE
//        // =========================================
//
//        if (nombre.isEmpty()) {
//
//            etNombre.error = "Ingrese su nombre"
//            etNombre.requestFocus()
//
//            return
//        }
//
//        // =========================================
//        // VALIDAR APELLIDO
//        // =========================================
//
//        if (apellido.isEmpty()) {
//
//            etApellido.error = "Ingrese su apellido"
//            etApellido.requestFocus()
//
//            return
//        }
//
//        // =========================================
//        // VALIDAR CORREO
//        // =========================================
//
//        if (correo.isEmpty()) {
//
//            etCorreo.error = "Ingrese su correo"
//            etCorreo.requestFocus()
//
//            return
//        }
//
//        // =========================================
//        // VALIDAR FORMATO EMAIL
//        // =========================================
//
//        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
//
//            etCorreo.error = "Ingrese un correo válido"
//            etCorreo.requestFocus()
//
//            return
//        }
//
//        if (passwordNueva.isNotEmpty() || passwordConfirmar.isNotEmpty()) {
//
//            if (passwordNueva != passwordConfirmar) {
//                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
//                return
//            }
//
//            if (passwordNueva.length < 6 || passwordNueva.length > 10) {
//                findViewById<EditText>(R.id.etPasswordNueva).error =
//                    "La contraseña debe tener entre 6 y 10 caracteres"
//                findViewById<EditText>(R.id.etPasswordNueva).requestFocus()
//                return
//            }
//        }
//
//        val json = JSONObject()
//
//        json.put("nombre", nombre)
//        json.put("apellido", apellido)
//        json.put("correo", correo)
//        json.put("contraseña", passwordNueva)
//
//
//        Thread {
//
//            val body = RequestBody.create(
//                "application/json".toMediaTypeOrNull(),
//                json.toString()
//            )
//
//            val request = Request.Builder()
//                .url("http://192.168.18.238:8080/api/usuarios/perfil")
//                .put(body)
//                .addHeader("Authorization", "Bearer ${getToken()}")
//                .build()
//
//            client.newCall(request).execute()
//
//            runOnUiThread {
//
//                Toast.makeText(
//                    this,
//                    "Perfil actualizado",
//                    Toast.LENGTH_SHORT
//                ).show()
//                // 🔥 LIMPIAR CAMPOS DE CONTRASEÑA
//                findViewById<EditText>(R.id.etPasswordNueva).text.clear()
//                findViewById<EditText>(R.id.etPasswordConfirmar).text.clear()
//
//            }
//
//        }.start()
//    }
//
//    // =====================================================
//    // CARGAR CONTACTOS
//    // =====================================================
//
//    private fun cargarContactos(callback: (() -> Unit)? = null) {
//
//        Thread {
//
//            val request = Request.Builder()
//                .url("http://192.168.18.238:8080/api/contactos")
//                .addHeader("Authorization", "Bearer ${getToken()}")
//                .build()
//
//            val response = client.newCall(request).execute()
//
//            val array = JSONArray(response.body?.string())
//
//            listaContactos.clear()
//
//            for (i in 0 until array.length()) {
//
//                val obj = array.getJSONObject(i)
//
//                val contacto = ContactoEmergencia(
//                    obj.getLong("id"),
//                    obj.getString("nombre"),
//                    obj.getString("telefono"),
//                    obj.optString("apellido"),
//                    obj.optString("relacion"),
//                    obj.getString("correo")
//                )
//
//                listaContactos.add(contacto)
//            }
//
//            runOnUiThread {
//
//                tvContactos.text = "${listaContactos.size}/4 contactos"
//
//                callback?.invoke()
//            }
//
//        }.start()
//    }
//
//    // =====================================================
//    // REGISTRAR EDICIÓN
//    // =====================================================
//
//    private fun registrarEdicion(
//        etNombre: EditText,
//        etTelefono: EditText,
//        etCorreo: EditText,
//        etApellido: EditText,
//        spRelacion: Spinner
//    ) {
//
//        val id = contactoSeleccionadoId ?: return
//
//        val contactoEditado = ContactoEmergencia(
//            id,
//            etNombre.text.toString(),
//            etTelefono.text.toString(),
//            etApellido.text.toString(),
//            spRelacion.selectedItem.toString(),
//            etCorreo.text.toString()
//        )
//
//        contactosEditados.removeAll { it.id == id }
//
//        contactosEditados.add(contactoEditado)
//
//        val index = listaContactos.indexOfFirst { it.id == id }
//
//        if (index != -1) {
//
//            listaContactos[index] = contactoEditado
//
//            adapter.notifyItemChanged(index)
//        }
//    }
//
//    // =====================================================
//    // POPUP CONTACTOS
//    // =====================================================
//
//    private fun mostrarContactosPopup() {
//
//        val view = layoutInflater.inflate(
//            R.layout.dialog_contactos,
//            null
//        )
//
//        val recycler = view.findViewById<RecyclerView>(R.id.recyclerContactos)
//
//        val etNombre = view.findViewById<EditText>(R.id.etNombreContacto)
//        val etTelefono = view.findViewById<EditText>(R.id.etTelefonoContacto)
//        val etCorreo = view.findViewById<EditText>(R.id.etCorreoContacto)
//        val etApellido = view.findViewById<EditText>(R.id.etApellidoContacto)
//
//        val spRelacion = view.findViewById<Spinner>(R.id.spRelacion)
//
//        val btnAgregar = view.findViewById<Button>(R.id.btnGuardarContacto)
//
//        val btnActualizar = view.findViewById<Button>(R.id.btnActualizarContacto)
//
//        val btnGuardarLocal = view.findViewById<Button>(R.id.btnGuardarLocal)
//
//        val layoutForm = view.findViewById<LinearLayout>(R.id.layoutForm)
//
//        // =====================================================
//        // HABILITAR / DESHABILITAR FORMULARIO
//        // =====================================================
//
//        fun habilitarFormulario(habilitar: Boolean) {
//
//            etNombre.isEnabled = habilitar
//            etApellido.isEnabled = habilitar
//            etTelefono.isEnabled = habilitar
//            etCorreo.isEnabled = habilitar
//            spRelacion.isEnabled = habilitar
//        }
//
//        // =====================================================
//        // FORMULARIO INICIALMENTE DESHABILITADO
//        // =====================================================
//
//        habilitarFormulario(false)
//
//        val relaciones = arrayOf(
//            "Seleccione su relación",
//            "Padre",
//            "Madre",
//            "Hijo",
//            "Hija",
//            "Tío",
//            "Tía",
//            "Abuelo",
//            "Abuela",
//            "Sobrino",
//            "Sobrina",
//            "Amigo",
//            "Amiga",
//            "Otros"
//        )
//
//        val adapterSpinner = ArrayAdapter(
//            this,
//            android.R.layout.simple_spinner_item,
//            relaciones
//        )
//
//        adapterSpinner.setDropDownViewResource(
//            android.R.layout.simple_spinner_dropdown_item
//        )
//
//        spRelacion.adapter = adapterSpinner
//
//        recycler.layoutManager = LinearLayoutManager(this)
//
//        adapter = ContactoAdapter(
//
//            listaContactos,
//
//            // =====================================================
//            // ELIMINAR
//            // =====================================================
//
//            { contacto ->
//
//                listaContactos.remove(contacto)
//
//                contactosEliminados.add(contacto.id)
//
//                adapter.notifyDataSetChanged()
//            },
//
//            // =====================================================
//            // EDITAR
//            // =====================================================
//
//            { contacto ->
//
//                etNombre.setText(contacto.nombre)
//                etTelefono.setText(contacto.telefono)
//                etCorreo.setText(contacto.correo)
//                etApellido.setText(contacto.apellido)
//
//                val posicion = relaciones.indexOf(contacto.relacion)
//
//                if (posicion >= 0) {
//
//                    spRelacion.setSelection(posicion)
//
//                } else {
//
//                    spRelacion.setSelection(0)
//                }
//
//                contactoSeleccionadoId = contacto.id
//
//                // =====================================================
//                // HABILITAR FORMULARIO
//                // =====================================================
//
//                habilitarFormulario(true)
//
//                // =====================================================
//                // DESHABILITAR AGREGAR
//                // =====================================================
//
//                btnAgregar.isEnabled = false
//            }
//        )
//
//        recycler.adapter = adapter
//
//        val dialog = AlertDialog.Builder(this)
//            .setView(view)
//            .setPositiveButton("Cerrar", null)
//            .create()
//
//        cargarContactos {
//
//            adapter.notifyDataSetChanged()
//        }
//
//        // =====================================================
//        // AGREGAR CONTACTO
//        // =====================================================
//
//        btnAgregar.setOnClickListener {
//
//            layoutForm.visibility = View.VISIBLE
//
//            btnGuardarLocal.visibility = View.VISIBLE
//
//            // =====================================================
//            // HABILITAR FORMULARIO
//            // =====================================================
//
//            habilitarFormulario(true)
//
//            btnAgregar.visibility = View.GONE
//
//            btnActualizar.visibility = View.GONE
//
//            recycler.visibility = View.GONE
//        }
//
//        // =====================================================
//        // GUARDAR LOCAL
//        // =====================================================
//
//        btnGuardarLocal.setOnClickListener {
//
//            // =========================================
//            // VALIDAR MÁXIMO 4 CONTACTOS
//            // =========================================
//
//            if (listaContactos.size >= 4) {
//
//                Toast.makeText(
//                    this,
//                    "Solo puede registrar máximo 4 contactos",
//                    Toast.LENGTH_LONG
//                ).show()
//
//                return@setOnClickListener
//            }
//
//            // =========================================
//            // OBTENER VALORES
//            // =========================================
//
//            val nombre = etNombre.text.toString().trim()
//
//            val apellido = etApellido.text.toString().trim()
//
//            val telefono = etTelefono.text.toString().trim()
//
//            val correo = etCorreo.text.toString().trim()
//
//            val relacion = spRelacion.selectedItem.toString()
//
//            // =========================================
//            // VALIDAR NOMBRE
//            // =========================================
//
//            if (nombre.isEmpty()) {
//
//                etNombre.error = "Ingrese el nombre"
//
//                etNombre.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            // =========================================
//            // VALIDAR APELLIDO
//            // =========================================
//
//            if (apellido.isEmpty()) {
//
//                etApellido.error = "Ingrese el apellido"
//
//                etApellido.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            // =========================================
//            // VALIDAR TELÉFONO
//            // =========================================
//
//            if (telefono.isEmpty()) {
//
//                etTelefono.error = "Ingrese el teléfono"
//
//                etTelefono.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            // =========================================
//            // VALIDAR 9 DÍGITOS
//            // =========================================
//
//            if (!telefono.matches(Regex("^\\d{9}$"))) {
//
//                etTelefono.error =
//                    "Ingrese un teléfono válido de 9 dígitos"
//
//                etTelefono.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            // =========================================
//            // VALIDAR CORREO
//            // =========================================
//
//            if (correo.isEmpty()) {
//
//                etCorreo.error = "Ingrese el correo"
//
//                etCorreo.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            // =========================================
//            // VALIDAR EMAIL
//            // =========================================
//
//            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
//
//                etCorreo.error = "Ingrese un correo válido"
//
//                etCorreo.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            // =========================================
//            // VALIDAR RELACIÓN
//            // =========================================
//
//            if (relacion == "Seleccione su relación") {
//
//                Toast.makeText(
//                    this,
//                    "Seleccione una relación",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                return@setOnClickListener
//            }
//
//            val nuevo = ContactoEmergencia(
//                System.currentTimeMillis(),
//                nombre,
//                telefono,
//                apellido,
//                relacion,
//                correo
//            )
//
//            contactosNuevos.add(nuevo)
//
//            listaContactos.add(nuevo)
//
//            adapter.notifyDataSetChanged()
//
//            // =========================================
//            // LIMPIAR CAMPOS
//            // =========================================
//
//            etNombre.text.clear()
//            etApellido.text.clear()
//            etTelefono.text.clear()
//            etCorreo.text.clear()
//
//            spRelacion.setSelection(0)
//
//            // =========================================
//            // DESHABILITAR FORMULARIO
//            // =========================================
//
//            habilitarFormulario(false)
//
//            layoutForm.visibility = View.VISIBLE
//
//            btnGuardarLocal.visibility = View.GONE
//
//            btnAgregar.visibility = View.VISIBLE
//
//            btnActualizar.visibility = View.VISIBLE
//
//            recycler.visibility = View.VISIBLE
//        }
//
//        // =====================================================
//        // ACTUALIZAR
//        // =====================================================
//
//// =====================================================
//// ACTUALIZAR
//// =====================================================
//
//        btnActualizar.setOnClickListener {
//
//            // =====================================================
//            // SI LOS CAMPOS ESTÁN DESHABILITADOS
//            // GUARDAR DIRECTAMENTE LOS CAMBIOS PENDIENTES
//            // =====================================================
//
//            if (
//                !etNombre.isEnabled &&
//                !etApellido.isEnabled &&
//                !etTelefono.isEnabled &&
//                !etCorreo.isEnabled &&
//                !spRelacion.isEnabled
//            ) {
//
//                Thread {
//
//                    // =====================================================
//                    // NUEVOS
//                    // =====================================================
//
//                    contactosNuevos.forEach {
//
//                        val json = JSONObject()
//
//                        json.put("nombre", it.nombre)
//                        json.put("apellido", it.apellido)
//                        json.put("relacion", it.relacion)
//                        json.put("telefono", it.telefono)
//                        json.put("correo", it.correo)
//
//                        val body = RequestBody.create(
//                            "application/json".toMediaTypeOrNull(),
//                            json.toString()
//                        )
//
//                        val request = Request.Builder()
//                            .url("http://192.168.18.238:8080/api/contactos")
//                            .post(body)
//                            .addHeader(
//                                "Authorization",
//                                "Bearer ${getToken()}"
//                            )
//                            .build()
//
//                        client.newCall(request).execute()
//                    }
//
//                    // =====================================================
//                    // EDITADOS
//                    // =====================================================
//
//                    contactosEditados.forEach {
//
//                        val json = JSONObject()
//
//                        json.put("nombre", it.nombre)
//                        json.put("apellido", it.apellido)
//                        json.put("relacion", it.relacion)
//                        json.put("telefono", it.telefono)
//                        json.put("correo", it.correo)
//
//                        val body = RequestBody.create(
//                            "application/json".toMediaTypeOrNull(),
//                            json.toString()
//                        )
//
//                        val request = Request.Builder()
//                            .url(
//                                "http://192.168.18.238:8080/api/contactos/actualizar/${it.id}"
//                            )
//                            .put(body)
//                            .addHeader(
//                                "Authorization",
//                                "Bearer ${getToken()}"
//                            )
//                            .build()
//
//                        client.newCall(request).execute()
//                    }
//
//                    // =====================================================
//                    // ELIMINADOS
//                    // =====================================================
//
//                    contactosEliminados.forEach {
//
//                        val request = Request.Builder()
//                            .url(
//                                "http://192.168.18.238:8080/api/contactos/$it"
//                            )
//                            .delete()
//                            .addHeader(
//                                "Authorization",
//                                "Bearer ${getToken()}"
//                            )
//                            .build()
//
//                        client.newCall(request).execute()
//                    }
//
//                    runOnUiThread {
//
//                        AlertDialog.Builder(this)
//                            .setTitle("Éxito")
//                            .setMessage(
//                                "Se guardaron los cambios correctamente"
//                            )
//                            .setPositiveButton("OK", null)
//                            .show()
//
//                        // =========================================
//                        // HABILITAR AGREGAR
//                        // =========================================
//
//                        btnAgregar.isEnabled = true
//
//                        // =========================================
//                        // LIMPIAR CAMPOS
//                        // =========================================
//
//                        etNombre.text.clear()
//                        etApellido.text.clear()
//                        etTelefono.text.clear()
//                        etCorreo.text.clear()
//
//                        spRelacion.setSelection(0)
//
//                        // =========================================
//                        // DESHABILITAR FORMULARIO
//                        // =========================================
//
//                        habilitarFormulario(false)
//
//                        contactosNuevos.clear()
//
//                        contactosEliminados.clear()
//
//                        contactosEditados.clear()
//
//                        cargarContactos {
//
//                            adapter.notifyDataSetChanged()
//                        }
//                    }
//
//                }.start()
//
//                return@setOnClickListener
//            }
//
//            val nombre = etNombre.text.toString().trim()
//            val apellido = etApellido.text.toString().trim()
//            val telefono = etTelefono.text.toString().trim()
//            val correo = etCorreo.text.toString().trim()
//            val relacion = spRelacion.selectedItem.toString()
//
//            // VALIDAR NOMBRE
//            if (nombre.isEmpty()) {
//                etNombre.error = "Ingrese el nombre"
//                return@setOnClickListener
//            }
//
//            // VALIDAR APELLIDO
//            if (apellido.isEmpty()) {
//                etApellido.error = "Ingrese el apellido"
//                return@setOnClickListener
//            }
//
//            // VALIDAR TELÉFONO
//            if (telefono.isEmpty() ||
//                //!telefono.matches(Regex("^\\d{9}$")
//                !telefono.matches(
//                    Regex("^9\\d{8}$")
//                )) {
//                etTelefono.error = "El teléfono debe comenzar con 9 y tener 9 dígitos"
//                return@setOnClickListener
//            }
//
//            // VALIDAR CORREO
//            if (correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
//                etCorreo.error = "Correo inválido"
//                return@setOnClickListener
//            }
//
//            // VALIDAR RELACIÓN
//            if (relacion == "Seleccione su relación") {
//                Toast.makeText(this, "Seleccione una relación", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (contactoSeleccionadoId != null) {
//
//                registrarEdicion(
//                    etNombre,
//                    etTelefono,
//                    etCorreo,
//                    etApellido,
//                    spRelacion
//                )
//
//                contactoSeleccionadoId = null
//            }
//
//            Thread {
//
//                // =====================================================
//                // NUEVOS
//                // =====================================================
//
//                contactosNuevos.forEach {
//
//                    val json = JSONObject()
//
//                    json.put("nombre", it.nombre)
//                    json.put("apellido", it.apellido)
//                    json.put("relacion", it.relacion)
//                    json.put("telefono", it.telefono)
//                    json.put("correo", it.correo)
//
//                    val body = RequestBody.create(
//                        "application/json".toMediaTypeOrNull(),
//                        json.toString()
//                    )
//
//                    val request = Request.Builder()
//                        .url("http://192.168.18.238:8080/api/contactos")
//                        .post(body)
//                        .addHeader(
//                            "Authorization",
//                            "Bearer ${getToken()}"
//                        )
//                        .build()
//
//                    client.newCall(request).execute()
//                }
//
//                // =====================================================
//                // EDITADOS
//                // =====================================================
//
//                contactosEditados.forEach {
//
//                    val json = JSONObject()
//
//                    json.put("nombre", it.nombre)
//                    json.put("apellido", it.apellido)
//                    json.put("relacion", it.relacion)
//                    json.put("telefono", it.telefono)
//                    json.put("correo", it.correo)
//
//                    val body = RequestBody.create(
//                        "application/json".toMediaTypeOrNull(),
//                        json.toString()
//                    )
//
//                    val request = Request.Builder()
//                        .url(
//                            "http://192.168.18.238:8080/api/contactos/actualizar/${it.id}"
//                        )
//                        .put(body)
//                        .addHeader(
//                            "Authorization",
//                            "Bearer ${getToken()}"
//                        )
//                        .build()
//
//                    client.newCall(request).execute()
//                }
//
//                // =====================================================
//                // ELIMINADOS
//                // =====================================================
//
//                contactosEliminados.forEach {
//
//                    val request = Request.Builder()
//                        .url(
//                            "http://192.168.18.238:8080/api/contactos/$it"
//                        )
//                        .delete()
//                        .addHeader(
//                            "Authorization",
//                            "Bearer ${getToken()}"
//                        )
//                        .build()
//
//                    client.newCall(request).execute()
//                }
//
//                runOnUiThread {
//
//                    AlertDialog.Builder(this)
//                        .setTitle("Éxito")
//                        .setMessage(
//                            "Se guardaron los cambios correctamente"
//                        )
//                        .setPositiveButton("OK", null)
//                        .show()
//
//                    // =========================================
//                    // HABILITAR AGREGAR
//                    // =========================================
//
//                    btnAgregar.isEnabled = true
//
//                    // =========================================
//                    // LIMPIAR CAMPOS
//                    // =========================================
//
//                    etNombre.text.clear()
//                    etApellido.text.clear()
//                    etTelefono.text.clear()
//                    etCorreo.text.clear()
//
//                    spRelacion.setSelection(0)
//
//                    // =========================================
//                    // DESHABILITAR FORMULARIO
//                    // =========================================
//
//                    habilitarFormulario(false)
//
//                    contactosNuevos.clear()
//
//                    contactosEliminados.clear()
//
//                    contactosEditados.clear()
//
//                    cargarContactos {
//
//                        adapter.notifyDataSetChanged()
//                    }
//                }
//
//            }.start()
//        }
//
//        dialog.show()
//    }
//}


package com.project.projectsegurity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.projectsegurity.adapter.ContactoAdapter
import com.project.projectsegurity.databinding.ActivityLoginBinding
import com.project.projectsegurity.model.ContactoEmergencia
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject

class PerfilActivity : AppCompatActivity() {

    // =====================================================
    // 🔥 BASE URL
    // =====================================================
    private val BASE_URL =
        "http://192.168.18.238:8080"
        //"https://appalertacomunitaria.com"
    private lateinit var binding: ActivityLoginBinding

    private lateinit var etNombre: EditText
    private lateinit var etApellido: EditText
    private lateinit var etCorreo: EditText

    private lateinit var btnGuardar: Button
    private lateinit var btnAgregarContacto: Button
    private lateinit var tvContactos: TextView
    private lateinit var btnSalir: Button

    private lateinit var adapter: ContactoAdapter

    private val client = OkHttpClient()

    private val listaContactos = mutableListOf<ContactoEmergencia>()

    private val contactosNuevos = mutableListOf<ContactoEmergencia>()
    private val contactosEliminados = mutableListOf<Long>()
    private val contactosEditados = mutableListOf<ContactoEmergencia>()

    private var contactoSeleccionadoId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        etNombre = findViewById(R.id.etNombre)
        etApellido = findViewById(R.id.etApellido)
        etCorreo = findViewById(R.id.etCorreo)

        btnGuardar = findViewById(R.id.btnGuardar)
        btnAgregarContacto = findViewById(R.id.btnAgregarContacto)
        tvContactos = findViewById(R.id.tvContactos)
        btnSalir = findViewById(R.id.btnSalir)

        cargarPerfil()
        cargarContactos()

        btnGuardar.setOnClickListener {
            actualizarPerfil()
        }

        btnAgregarContacto.setOnClickListener {
            mostrarContactosPopup()
        }

        tvContactos.setOnClickListener {
            mostrarContactosPopup()
        }

        btnSalir.setOnClickListener {
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish()
        }
    }

    // =====================================================
    // TOKEN
    // =====================================================

    private fun getToken(): String {

        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)

        return prefs.getString("TOKEN", "") ?: ""
    }

    // =====================================================
    // CARGAR PERFIL
    // =====================================================

    private fun cargarPerfil() {

        Thread {

            val request = Request.Builder()
                .url("$BASE_URL/api/usuarios/perfil")
                .addHeader("Authorization", "Bearer ${getToken()}")
                .build()

            val response = client.newCall(request).execute()

            val body = response.body?.string()

            runOnUiThread {

                val json = JSONObject(body)

                etNombre.setText(json.optString("nombre"))
                etApellido.setText(json.optString("apellido"))
                etCorreo.setText(json.optString("correo"))
            }

        }.start()
    }

    // =====================================================
    // ACTUALIZAR PERFIL
    // =====================================================

    private fun actualizarPerfil() {

        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()
        val correo = etCorreo.text.toString().trim()

        // 🔥 NUEVO: CONTRASEÑAS
        val passwordNueva = findViewById<EditText>(R.id.etPasswordNueva).text.toString().trim()
        val passwordConfirmar = findViewById<EditText>(R.id.etPasswordConfirmar).text.toString().trim()

        // =========================================
        // VALIDAR NOMBRE
        // =========================================

        if (nombre.isEmpty()) {

            etNombre.error = "Ingrese su nombre"
            etNombre.requestFocus()

            return
        }

        // =========================================
        // VALIDAR APELLIDO
        // =========================================

        if (apellido.isEmpty()) {

            etApellido.error = "Ingrese su apellido"
            etApellido.requestFocus()

            return
        }

        // =========================================
        // VALIDAR CORREO
        // =========================================

        if (correo.isEmpty()) {

            etCorreo.error = "Ingrese su correo"
            etCorreo.requestFocus()

            return
        }

        // =========================================
        // VALIDAR FORMATO EMAIL
        // =========================================

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {

            etCorreo.error = "Ingrese un correo válido"
            etCorreo.requestFocus()

            return
        }

        if (passwordNueva.isNotEmpty() || passwordConfirmar.isNotEmpty()) {

            if (passwordNueva != passwordConfirmar) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return
            }

            if (passwordNueva.length < 6 || passwordNueva.length > 10) {
                findViewById<EditText>(R.id.etPasswordNueva).error =
                    "La contraseña debe tener entre 6 y 10 caracteres"
                findViewById<EditText>(R.id.etPasswordNueva).requestFocus()
                return
            }
        }

        val json = JSONObject()

        json.put("nombre", nombre)
        json.put("apellido", apellido)
        json.put("correo", correo)
        json.put("contraseña", passwordNueva)

        Thread {

            val body = RequestBody.create(
                "application/json".toMediaTypeOrNull(),
                json.toString()
            )

            val request = Request.Builder()
                .url("$BASE_URL/api/usuarios/perfil")
                .put(body)
                .addHeader("Authorization", "Bearer ${getToken()}")
                .build()

            client.newCall(request).execute()

            runOnUiThread {

                Toast.makeText(
                    this,
                    "Perfil actualizado",
                    Toast.LENGTH_SHORT
                ).show()

                // 🔥 LIMPIAR CAMPOS DE CONTRASEÑA
                findViewById<EditText>(R.id.etPasswordNueva).text.clear()
                findViewById<EditText>(R.id.etPasswordConfirmar).text.clear()
            }

        }.start()
    }

    // =====================================================
    // CARGAR CONTACTOS
    // =====================================================

    private fun cargarContactos(callback: (() -> Unit)? = null) {

        Thread {

            val request = Request.Builder()
                .url("$BASE_URL/api/contactos")
                .addHeader("Authorization", "Bearer ${getToken()}")
                .build()

            val response = client.newCall(request).execute()

            val array = JSONArray(response.body?.string())

            listaContactos.clear()

            for (i in 0 until array.length()) {

                val obj = array.getJSONObject(i)

                val contacto = ContactoEmergencia(
                    obj.getLong("id"),
                    obj.getString("nombre"),
                    obj.getString("telefono"),
                    obj.optString("apellido"),
                    obj.optString("relacion"),
                    obj.getString("correo")
                )

                listaContactos.add(contacto)
            }

            runOnUiThread {

                tvContactos.text = "${listaContactos.size}/4 contactos"

                callback?.invoke()
            }

        }.start()
    }

    // =====================================================
    // REGISTRAR EDICIÓN
    // =====================================================

    private fun registrarEdicion(
        etNombre: EditText,
        etTelefono: EditText,
        etCorreo: EditText,
        etApellido: EditText,
        spRelacion: Spinner
    ) {

        val id = contactoSeleccionadoId ?: return

        val contactoEditado = ContactoEmergencia(
            id,
            etNombre.text.toString(),
            etTelefono.text.toString(),
            etApellido.text.toString(),
            spRelacion.selectedItem.toString(),
            etCorreo.text.toString()
        )

        contactosEditados.removeAll { it.id == id }

        contactosEditados.add(contactoEditado)

        val index = listaContactos.indexOfFirst { it.id == id }

        if (index != -1) {

            listaContactos[index] = contactoEditado

            adapter.notifyItemChanged(index)
        }
    }

    // =====================================================
    // POPUP CONTACTOS
    // =====================================================

    private fun mostrarContactosPopup() {

        val view = layoutInflater.inflate(
            R.layout.dialog_contactos,
            null
        )

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerContactos)

        val etNombre = view.findViewById<EditText>(R.id.etNombreContacto)
        val etTelefono = view.findViewById<EditText>(R.id.etTelefonoContacto)
        val etCorreo = view.findViewById<EditText>(R.id.etCorreoContacto)
        val etApellido = view.findViewById<EditText>(R.id.etApellidoContacto)

        val spRelacion = view.findViewById<Spinner>(R.id.spRelacion)

        val btnAgregar = view.findViewById<Button>(R.id.btnGuardarContacto)

        val btnActualizar = view.findViewById<Button>(R.id.btnActualizarContacto)

        val btnGuardarLocal = view.findViewById<Button>(R.id.btnGuardarLocal)

        val layoutForm = view.findViewById<LinearLayout>(R.id.layoutForm)

        fun habilitarFormulario(habilitar: Boolean) {

            etNombre.isEnabled = habilitar
            etApellido.isEnabled = habilitar
            etTelefono.isEnabled = habilitar
            etCorreo.isEnabled = habilitar
            spRelacion.isEnabled = habilitar
        }

        habilitarFormulario(false)

        val relaciones = arrayOf(
            "Seleccione su relación",
            "Padre",
            "Madre",
            "Hijo",
            "Hija",
            "Tío",
            "Tía",
            "Abuelo",
            "Abuela",
            "Sobrino",
            "Sobrina",
            "Amigo",
            "Amiga",
            "Otros"
        )

        val adapterSpinner = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            relaciones
        )

        adapterSpinner.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spRelacion.adapter = adapterSpinner

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = ContactoAdapter(

            listaContactos,

            { contacto ->

                listaContactos.remove(contacto)

                contactosEliminados.add(contacto.id)

                adapter.notifyDataSetChanged()
            },

            { contacto ->

                etNombre.setText(contacto.nombre)
                etTelefono.setText(contacto.telefono)
                etCorreo.setText(contacto.correo)
                etApellido.setText(contacto.apellido)

                val posicion = relaciones.indexOf(contacto.relacion)

                if (posicion >= 0) {

                    spRelacion.setSelection(posicion)

                } else {

                    spRelacion.setSelection(0)
                }

                contactoSeleccionadoId = contacto.id

                habilitarFormulario(true)

                btnAgregar.isEnabled = false
            }
        )

        recycler.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Cerrar", null)
            .create()

        cargarContactos {

            adapter.notifyDataSetChanged()
        }

        btnAgregar.setOnClickListener {

            layoutForm.visibility = View.VISIBLE

            btnGuardarLocal.visibility = View.VISIBLE

            habilitarFormulario(true)

            btnAgregar.visibility = View.GONE

            btnActualizar.visibility = View.GONE

            recycler.visibility = View.GONE
        }

        btnGuardarLocal.setOnClickListener {

            if (listaContactos.size >= 4) {

                Toast.makeText(
                    this,
                    "Solo puede registrar máximo 4 contactos",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            val nombre = etNombre.text.toString().trim()

            val apellido = etApellido.text.toString().trim()

            val telefono = etTelefono.text.toString().trim()

            val correo = etCorreo.text.toString().trim()

            val relacion = spRelacion.selectedItem.toString()

            if (nombre.isEmpty()) {

                etNombre.error = "Ingrese el nombre"

                etNombre.requestFocus()

                return@setOnClickListener
            }

            if (apellido.isEmpty()) {

                etApellido.error = "Ingrese el apellido"

                etApellido.requestFocus()

                return@setOnClickListener
            }

            if (telefono.isEmpty()) {

                etTelefono.error = "Ingrese el teléfono"

                etTelefono.requestFocus()

                return@setOnClickListener
            }

            if (!telefono.matches(Regex("^\\d{9}$"))) {

                etTelefono.error =
                    "Ingrese un teléfono válido de 9 dígitos"

                etTelefono.requestFocus()

                return@setOnClickListener
            }

            if (correo.isEmpty()) {

                etCorreo.error = "Ingrese el correo"

                etCorreo.requestFocus()

                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {

                etCorreo.error = "Ingrese un correo válido"

                etCorreo.requestFocus()

                return@setOnClickListener
            }

            if (relacion == "Seleccione su relación") {

                Toast.makeText(
                    this,
                    "Seleccione una relación",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val nuevo = ContactoEmergencia(
                System.currentTimeMillis(),
                nombre,
                telefono,
                apellido,
                relacion,
                correo
            )

            contactosNuevos.add(nuevo)

            listaContactos.add(nuevo)

            adapter.notifyDataSetChanged()

            etNombre.text.clear()
            etApellido.text.clear()
            etTelefono.text.clear()
            etCorreo.text.clear()

            spRelacion.setSelection(0)

            habilitarFormulario(false)

            layoutForm.visibility = View.VISIBLE

            btnGuardarLocal.visibility = View.GONE

            btnAgregar.visibility = View.VISIBLE

            btnActualizar.visibility = View.VISIBLE

            recycler.visibility = View.VISIBLE
        }

        btnActualizar.setOnClickListener {

            if (
                !etNombre.isEnabled &&
                !etApellido.isEnabled &&
                !etTelefono.isEnabled &&
                !etCorreo.isEnabled &&
                !spRelacion.isEnabled
            ) {

                Thread {

                    contactosNuevos.forEach {

                        val json = JSONObject()

                        json.put("nombre", it.nombre)
                        json.put("apellido", it.apellido)
                        json.put("relacion", it.relacion)
                        json.put("telefono", it.telefono)
                        json.put("correo", it.correo)

                        val body = RequestBody.create(
                            "application/json".toMediaTypeOrNull(),
                            json.toString()
                        )

                        val request = Request.Builder()
                            .url("$BASE_URL/api/contactos")
                            .post(body)
                            .addHeader(
                                "Authorization",
                                "Bearer ${getToken()}"
                            )
                            .build()

                        client.newCall(request).execute()
                    }

                    contactosEditados.forEach {

                        val json = JSONObject()

                        json.put("nombre", it.nombre)
                        json.put("apellido", it.apellido)
                        json.put("relacion", it.relacion)
                        json.put("telefono", it.telefono)
                        json.put("correo", it.correo)

                        val body = RequestBody.create(
                            "application/json".toMediaTypeOrNull(),
                            json.toString()
                        )

                        val request = Request.Builder()
                            .url(
                                "$BASE_URL/api/contactos/actualizar/${it.id}"
                            )
                            .put(body)
                            .addHeader(
                                "Authorization",
                                "Bearer ${getToken()}"
                            )
                            .build()

                        client.newCall(request).execute()
                    }

                    contactosEliminados.forEach {

                        val request = Request.Builder()
                            .url(
                                "$BASE_URL/api/contactos/$it"
                            )
                            .delete()
                            .addHeader(
                                "Authorization",
                                "Bearer ${getToken()}"
                            )
                            .build()

                        client.newCall(request).execute()
                    }

                    runOnUiThread {

                        AlertDialog.Builder(this)
                            .setTitle("Éxito")
                            .setMessage(
                                "Se guardaron los cambios correctamente"
                            )
                            .setPositiveButton("OK", null)
                            .show()

                        btnAgregar.isEnabled = true

                        etNombre.text.clear()
                        etApellido.text.clear()
                        etTelefono.text.clear()
                        etCorreo.text.clear()

                        spRelacion.setSelection(0)

                        habilitarFormulario(false)

                        contactosNuevos.clear()

                        contactosEliminados.clear()

                        contactosEditados.clear()

                        cargarContactos {

                            adapter.notifyDataSetChanged()
                        }
                    }

                }.start()

                return@setOnClickListener
            }

            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val relacion = spRelacion.selectedItem.toString()

            if (nombre.isEmpty()) {
                etNombre.error = "Ingrese el nombre"
                return@setOnClickListener
            }

            if (apellido.isEmpty()) {
                etApellido.error = "Ingrese el apellido"
                return@setOnClickListener
            }

            if (telefono.isEmpty() ||
                !telefono.matches(
                    Regex("^9\\d{8}$")
                )) {
                etTelefono.error = "El teléfono debe comenzar con 9 y tener 9 dígitos"
                return@setOnClickListener
            }

            if (correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                etCorreo.error = "Correo inválido"
                return@setOnClickListener
            }

            if (relacion == "Seleccione su relación") {
                Toast.makeText(this, "Seleccione una relación", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contactoSeleccionadoId != null) {

                registrarEdicion(
                    etNombre,
                    etTelefono,
                    etCorreo,
                    etApellido,
                    spRelacion
                )

                contactoSeleccionadoId = null
            }

            Thread {

                contactosNuevos.forEach {

                    val json = JSONObject()

                    json.put("nombre", it.nombre)
                    json.put("apellido", it.apellido)
                    json.put("relacion", it.relacion)
                    json.put("telefono", it.telefono)
                    json.put("correo", it.correo)

                    val body = RequestBody.create(
                        "application/json".toMediaTypeOrNull(),
                        json.toString()
                    )

                    val request = Request.Builder()
                        .url("$BASE_URL/api/contactos")
                        .post(body)
                        .addHeader(
                            "Authorization",
                            "Bearer ${getToken()}"
                        )
                        .build()

                    client.newCall(request).execute()
                }

                contactosEditados.forEach {

                    val json = JSONObject()

                    json.put("nombre", it.nombre)
                    json.put("apellido", it.apellido)
                    json.put("relacion", it.relacion)
                    json.put("telefono", it.telefono)
                    json.put("correo", it.correo)

                    val body = RequestBody.create(
                        "application/json".toMediaTypeOrNull(),
                        json.toString()
                    )

                    val request = Request.Builder()
                        .url(
                            "$BASE_URL/api/contactos/actualizar/${it.id}"
                        )
                        .put(body)
                        .addHeader(
                            "Authorization",
                            "Bearer ${getToken()}"
                        )
                        .build()

                    client.newCall(request).execute()
                }

                contactosEliminados.forEach {

                    val request = Request.Builder()
                        .url(
                            "$BASE_URL/api/contactos/$it"
                        )
                        .delete()
                        .addHeader(
                            "Authorization",
                            "Bearer ${getToken()}"
                        )
                        .build()

                    client.newCall(request).execute()
                }

                runOnUiThread {

                    AlertDialog.Builder(this)
                        .setTitle("Éxito")
                        .setMessage(
                            "Se guardaron los cambios correctamente"
                        )
                        .setPositiveButton("OK", null)
                        .show()

                    btnAgregar.isEnabled = true

                    etNombre.text.clear()
                    etApellido.text.clear()
                    etTelefono.text.clear()
                    etCorreo.text.clear()

                    spRelacion.setSelection(0)

                    habilitarFormulario(false)

                    contactosNuevos.clear()

                    contactosEliminados.clear()

                    contactosEditados.clear()

                    cargarContactos {

                        adapter.notifyDataSetChanged()
                    }
                }

            }.start()
        }

        dialog.show()
    }
}