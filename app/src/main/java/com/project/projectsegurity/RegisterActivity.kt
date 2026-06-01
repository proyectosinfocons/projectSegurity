//package com.project.projectsegurity
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Patterns
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
//import okhttp3.*
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.logging.HttpLoggingInterceptor
//import org.json.JSONArray
//import org.json.JSONObject
//import java.io.IOException
//
//class RegisterActivity : AppCompatActivity() {
//
//    private lateinit var etNombre: EditText
//    private lateinit var etApellido: EditText
//    private lateinit var etCorreo: EditText
//    private lateinit var etContrasenia: EditText
//
//    private lateinit var btnAgregarContacto: Button
//    private lateinit var btnRegistrarse: Button
//    private lateinit var btnSalir: Button
//    private lateinit var tvContactos: TextView
//
//    // 🔥 Lista donde guardamos contactos
//    private val listaContactos = mutableListOf<JSONObject>()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_register)
//
//        etNombre = findViewById(R.id.etNombre)
//        etApellido = findViewById(R.id.etApellido)
//        etCorreo = findViewById(R.id.etCorreo)
//        etContrasenia = findViewById(R.id.etContrasenia)
//
//        btnAgregarContacto = findViewById(R.id.btnAgregarContacto)
//        btnRegistrarse = findViewById(R.id.btnRegistrarse)
//        btnSalir = findViewById(R.id.btnSalir)
//        tvContactos = findViewById(R.id.tvContactos)
//
//        // =====================================================
//        // 🔥 BOTÓN AGREGAR CONTACTO
//        // =====================================================
//        btnAgregarContacto.setOnClickListener {
//
//            if (listaContactos.size >= 4) {
//
//                Toast.makeText(
//                    this,
//                    "Máximo 4 contactos",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                return@setOnClickListener
//            }
//
//            mostrarDialogoContacto()
//        }
//
//        // =====================================================
//        // 🔥 BOTÓN REGISTRARSE
//        // =====================================================
//        btnRegistrarse.setOnClickListener {
//
//            val nombre =
//                etNombre.text.toString().trim()
//
//            val apellido =
//                etApellido.text.toString().trim()
//
//            val correo =
//                etCorreo.text.toString().trim()
//
//            val contrasenia =
//                etContrasenia.text.toString().trim()
//
//            // =================================================
//            // 🔥 VALIDACIONES
//            // =================================================
//
//            if (nombre.isEmpty()) {
//
//                etNombre.error =
//                    "Ingrese su nombre"
//
//                etNombre.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            if (apellido.isEmpty()) {
//
//                etApellido.error =
//                    "Ingrese su apellido"
//
//                etApellido.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            if (correo.isEmpty()) {
//
//                etCorreo.error =
//                    "Ingrese su correo"
//
//                etCorreo.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            if (
//                !Patterns.EMAIL_ADDRESS
//                    .matcher(correo)
//                    .matches()
//            ) {
//
//                etCorreo.error =
//                    "Correo inválido"
//
//                etCorreo.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            if (contrasenia.isEmpty()) {
//
//                etContrasenia.error =
//                    "Ingrese contraseña"
//
//                etContrasenia.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            if (
//                contrasenia.length < 6 ||
//                contrasenia.length > 10
//            ) {
//
//                etContrasenia.error =
//                    "La contraseña debe tener entre 6 y 10 caracteres"
//
//                etContrasenia.requestFocus()
//
//                return@setOnClickListener
//            }
//
//            if (listaContactos.isEmpty()) {
//
//                Toast.makeText(
//                    this,
//                    "Agrega al menos 1 contacto",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                return@setOnClickListener
//            }
//
//            // =================================================
//            // 🔥 NUEVO
//            // 👉 ENVÍA OTP
//            // =================================================
//            enviarCodigo(
//                nombre,
//                apellido,
//                correo,
//                contrasenia
//            )
//        }
//
//        // =====================================================
//        // 🔥 BOTÓN SALIR
//        // =====================================================
//        btnSalir.setOnClickListener {
//
//            startActivity(
//                Intent(
//                    this,
//                    LoginActivity::class.java
//                )
//            )
//
//            finish()
//        }
//    }
//
//    // =====================================================
//    // 🔥 DIALOGO CONTACTO
//    // =====================================================
//    private fun mostrarDialogoContacto() {
//
//        val view =
//            layoutInflater.inflate(
//                R.layout.dialog_contacto,
//                null
//            )
//
//        val etNombre =
//            view.findViewById<EditText>(
//                R.id.etNombreContacto
//            )
//
//        val etApellido =
//            view.findViewById<EditText>(
//                R.id.etApellidoContacto
//            )
//
//        val etTelefono =
//            view.findViewById<EditText>(
//                R.id.etTelefonoContacto
//            )
//
//        val etCorreo =
//            view.findViewById<EditText>(
//                R.id.etCorreoContacto
//            )
//
//        val spRelacion =
//            view.findViewById<Spinner>(
//                R.id.spRelacion
//            )
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
//        val adapter = ArrayAdapter(
//            this,
//            android.R.layout.simple_spinner_item,
//            relaciones
//        )
//
//        adapter.setDropDownViewResource(
//            android.R.layout.simple_spinner_dropdown_item
//        )
//
//        spRelacion.adapter = adapter
//
//        val dialog =
//            android.app.AlertDialog.Builder(this)
//
//                .setTitle(
//                    "Agregar contacto"
//                )
//
//                .setView(view)
//
//                .setPositiveButton(
//                    "Guardar",
//                    null
//                )
//
//                .setNegativeButton(
//                    "Cancelar",
//                    null
//                )
//
//                .create()
//
//        dialog.setOnShowListener {
//
//            val button =
//                dialog.getButton(
//                    android.app.AlertDialog.BUTTON_POSITIVE
//                )
//
//            button.setOnClickListener {
//
//                val nombre =
//                    etNombre.text.toString().trim()
//
//                val apellido =
//                    etApellido.text.toString().trim()
//
//                val telefono =
//                    etTelefono.text.toString().trim()
//
//                val correo =
//                    etCorreo.text.toString().trim()
//
//                val relacion =
//                    spRelacion.selectedItem.toString()
//
//                if (nombre.isEmpty()) {
//
//                    etNombre.error =
//                        "Ingrese nombre"
//
//                    return@setOnClickListener
//                }
//
//                if (apellido.isEmpty()) {
//
//                    etApellido.error =
//                        "Ingrese apellido"
//
//                    return@setOnClickListener
//                }
//
//                if (
////                    !telefono.matches(
////                        Regex("^\\d{9}$")
//                    !telefono.matches(
//                        Regex("^9\\d{8}$")
//                    )
//                ) {
//
//                    etTelefono.error =
//                        "El teléfono debe comenzar con 9 y tener 9 dígitos"
//
//                    return@setOnClickListener
//                }
//
//                if (
//                    !Patterns.EMAIL_ADDRESS
//                        .matcher(correo)
//                        .matches()
//                ) {
//
//                    etCorreo.error =
//                        "Correo inválido"
//
//                    return@setOnClickListener
//                }
//
//                if (
//                    spRelacion.selectedItemPosition == 0
//                ) {
//
//                    Toast.makeText(
//                        this,
//                        "Seleccione relación",
//                        Toast.LENGTH_SHORT
//                    ).show()
//
//                    return@setOnClickListener
//                }
//
//                val contacto = JSONObject()
//
//                contacto.put(
//                    "nombre",
//                    nombre
//                )
//
//                contacto.put(
//                    "apellido",
//                    apellido
//                )
//
//                contacto.put(
//                    "telefono",
//                    telefono
//                )
//
//                contacto.put(
//                    "correo",
//                    correo
//                )
//
//                contacto.put(
//                    "relacion",
//                    relacion
//                )
//
//                listaContactos.add(contacto)
//
//                tvContactos.text =
//                    "Contactos: ${listaContactos.size}/4"
//
//                Toast.makeText(
//                    this,
//                    "Contacto agregado",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                dialog.dismiss()
//            }
//        }
//
//        dialog.show()
//    }
//
//    // =====================================================
//// 🔥 NUEVO
//// 👉 ENVÍA CÓDIGO OTP
//// =====================================================
//    private fun enviarCodigo(
//        nombre: String,
//        apellido: String,
//        correo: String,
//        contrasenia: String
//    ) {
//
//        val json = JSONObject()
//
//        json.put("correo", correo)
//
//        val body = RequestBody.create(
//            "application/json; charset=utf-8"
//                .toMediaTypeOrNull(),
//
//            json.toString()
//        )
//
//        val request = Request.Builder()
//
//            .url(
//                "http://192.168.18.238:8080/api/usuarios/enviar-codigo"
//            )
//
//            .post(body)
//
//            .build()
//
//        // =====================================================
//        // 🔥 CLIENTE OKHTTP
//        // 👉 AUMENTAR TIMEOUT
//        // =====================================================
//        val client = OkHttpClient.Builder()
//
//            .connectTimeout(
//                60,
//                java.util.concurrent.TimeUnit.SECONDS
//            )
//
//            .readTimeout(
//                60,
//                java.util.concurrent.TimeUnit.SECONDS
//            )
//
//            .writeTimeout(
//                60,
//                java.util.concurrent.TimeUnit.SECONDS
//            )
//
//            .addInterceptor(
//                HttpLoggingInterceptor().apply {
//
//                    level =
//                        HttpLoggingInterceptor.Level.BODY
//                }
//            )
//
//            .build()
//
//        // =====================================================
//        // 🔥 LLAMADA AL BACKEND
//        // =====================================================
//        client.newCall(request)
//
//            .enqueue(object : Callback {
//
//                // =================================================
//                // 🔥 ERROR
//                // =================================================
//                override fun onFailure(
//                    call: Call,
//                    e: IOException
//                ) {
//
//                    // 🔥 VER ERROR REAL
//                    e.printStackTrace()
//
//                    runOnUiThread {
//
//                        Toast.makeText(
//                            this@RegisterActivity,
//                            "Error: ${e.message}",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
//
//                // =================================================
//                // 🔥 RESPUESTA
//                // =================================================
//                override fun onResponse(
//                    call: Call,
//                    response: Response
//                ) {
//
//                    val responseBody =
//                        response.body?.string()
//
//                    // 🔥 VER CÓDIGO HTTP
//                    println(response.code)
//
//                    // =============================================
//                    // 🔥 SI TODO SALE BIEN
//                    // =============================================
//                    if (response.isSuccessful) {
//
//                        runOnUiThread {
//
//                            Toast.makeText(
//                                this@RegisterActivity,
//                                "Código enviado al correo",
//                                Toast.LENGTH_LONG
//                            ).show()
//
//                            // 🔥 FORZAR POPUP
//                            window.decorView.post {
//
//                                mostrarDialogoCodigo(
//                                    nombre,
//                                    apellido,
//                                    correo,
//                                    contrasenia
//                                )
//                            }
//                        }
//                    }
//
//                    // =============================================
//                    // 🔥 ERROR
//                    // =============================================
//                    else {
//
//                        runOnUiThread {
//
//                            etCorreo.error =
//                                responseBody ?: "Error"
//
//                            etCorreo.requestFocus()
//
//                            Toast.makeText(
//                                this@RegisterActivity,
//                                responseBody ?: "Error",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    }
//
//                    response.close()
//                }
//            })
//    }
//
//    // =====================================================
//    // 🔥 NUEVO
//    // 👉 DIALOGO OTP
//    // =====================================================
//    private fun mostrarDialogoCodigo(
//        nombre: String,
//        apellido: String,
//        correo: String,
//        contrasenia: String
//    ) {
//
//        val editText =
//            EditText(this)
//
//        editText.hint =
//            "Ingrese código"
//
//        android.app.AlertDialog.Builder(this)
//
//            .setTitle(
//                "Verificación"
//            )
//
//            .setMessage(
//                "Ingrese el código enviado al correo"
//            )
//
//            .setView(editText)
//
//            .setPositiveButton(
//                "Verificar"
//            ) { _, _ ->
//
//                val codigo =
//                    editText.text.toString()
//
//                verificarCodigo(
//
//                    nombre,
//                    apellido,
//                    correo,
//                    contrasenia,
//                    codigo
//                )
//            }
//
//            .setNegativeButton(
//                "Cancelar",
//                null
//            )
//
//            .show()
//    }
//
//    // =====================================================
//// 🔥 VERIFICAR OTP Y REGISTRAR
//// =====================================================
//    private fun verificarCodigo(
//        nombre: String,
//        apellido: String,
//        correo: String,
//        contrasenia: String,
//        codigo: String
//    ) {
//
//        // =============================================
//        // 🔥 USUARIO JSON
//        // =============================================
//        val usuario = JSONObject()
//
//        usuario.put(
//            "nombre",
//            nombre
//        )
//
//        usuario.put(
//            "apellido",
//            apellido
//        )
//
//        usuario.put(
//            "correo",
//            correo
//        )
//
//        usuario.put(
//            "contraseña",
//            contrasenia
//        )
//
//        usuario.put(
//            "contactosEmergencia",
//            JSONArray(listaContactos)
//        )
//
//        // =============================================
//        // 🔥 JSON FINAL
//        // =============================================
//        val json = JSONObject()
//
//        json.put(
//            "correo",
//            correo
//        )
//
//        json.put(
//            "codigo",
//            codigo
//        )
//
//        json.put(
//            "usuario",
//            usuario
//        )
//
//        // =============================================
//        // 🔥 VER JSON EN LOGCAT
//        // =============================================
//        println("================================")
//        println("JSON ENVIADO")
//        println(json.toString())
//        println("================================")
//
//        // =============================================
//        // 🔥 BODY
//        // =============================================
//        val body = RequestBody.create(
//            "application/json; charset=utf-8"
//                .toMediaTypeOrNull(),
//
//            json.toString()
//        )
//
//        // =============================================
//        // 🔥 REQUEST
//        // =============================================
//        val request = Request.Builder()
//
//            .url(
//                "http://192.168.18.238:8080/api/usuarios/verificar-codigo"
//            )
//
//            .post(body)
//
//            .build()
//
//        // =============================================
//        // 🔥 CLIENTE
//        // =============================================
//        val client = OkHttpClient.Builder()
//
//            .connectTimeout(
//                60,
//                java.util.concurrent.TimeUnit.SECONDS
//            )
//
//            .readTimeout(
//                60,
//                java.util.concurrent.TimeUnit.SECONDS
//            )
//
//            .writeTimeout(
//                60,
//                java.util.concurrent.TimeUnit.SECONDS
//            )
//
//            .addInterceptor(
//                HttpLoggingInterceptor().apply {
//
//                    level =
//                        HttpLoggingInterceptor.Level.BODY
//                }
//            )
//
//            .build()
//
//        // =============================================
//        // 🔥 LLAMADA
//        // =============================================
//        client.newCall(request)
//
//            .enqueue(object : Callback {
//
//                // =====================================
//                // 🔥 ERROR
//                // =====================================
//                override fun onFailure(
//                    call: Call,
//                    e: IOException
//                ) {
//
//                    e.printStackTrace()
//
//                    runOnUiThread {
//
//                        Toast.makeText(
//                            this@RegisterActivity,
//                            "Error: ${e.message}",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
//
//                // =====================================
//                // 🔥 RESPUESTA
//                // =====================================
//                override fun onResponse(
//                    call: Call,
//                    response: Response
//                ) {
//
//                    val responseBody =
//                        response.body?.string()
//
//                    println("================================")
//                    println("RESPUESTA BACKEND")
//                    println(responseBody)
//                    println("================================")
//
//                    runOnUiThread {
//
//                        // =============================
//                        // 🔥 ÉXITO
//                        // =============================
//                        if (response.isSuccessful) {
//
//                            Toast.makeText(
//                                this@RegisterActivity,
//                                "Registro exitoso",
//                                Toast.LENGTH_LONG
//                            ).show()
//
//                            // =========================
//                            // 🔥 LIMPIAR CAMPOS
//                            // =========================
//                            etNombre.text.clear()
//
//                            etApellido.text.clear()
//
//                            etCorreo.text.clear()
//
//                            etContrasenia.text.clear()
//
//                            listaContactos.clear()
//
//                            tvContactos.text =
//                                "Contactos: 0/4"
//
//                            // =========================
//                            // 🔥 LOGIN
//                            // =========================
//                            startActivity(
//                                Intent(
//                                    this@RegisterActivity,
//                                    LoginActivity::class.java
//                                )
//                            )
//
//                            finish()
//                        }
//
//                        // =============================
//                        // 🔥 ERROR
//                        // =============================
//                        else {
//
//                            Toast.makeText(
//                                this@RegisterActivity,
//                                responseBody
//                                    ?: "Código incorrecto",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    }
//
//                    response.close()
//                }
//            })
//    }
//}

package com.project.projectsegurity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    // =====================================================
    // 🔥 BASE URL
    // =====================================================
    private val BASE_URL =
        //"http://192.168.18.238:8080"
        "http://projectsecuritypeople-env.eba-k3djm54f.us-east-2.elasticbeanstalk.com"
        //"https://appalertacomunitaria.com"
    private lateinit var etNombre: EditText
    private lateinit var etApellido: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContrasenia: EditText

    private lateinit var btnAgregarContacto: Button
    private lateinit var btnRegistrarse: Button
    private lateinit var btnSalir: Button
    private lateinit var tvContactos: TextView

    // 🔥 Lista donde guardamos contactos
    private val listaContactos = mutableListOf<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etNombre = findViewById(R.id.etNombre)
        etApellido = findViewById(R.id.etApellido)
        etCorreo = findViewById(R.id.etCorreo)
        etContrasenia = findViewById(R.id.etContrasenia)

        btnAgregarContacto = findViewById(R.id.btnAgregarContacto)
        btnRegistrarse = findViewById(R.id.btnRegistrarse)
        btnSalir = findViewById(R.id.btnSalir)
        tvContactos = findViewById(R.id.tvContactos)

        // =====================================================
        // 🔥 BOTÓN AGREGAR CONTACTO
        // =====================================================
        btnAgregarContacto.setOnClickListener {

            if (listaContactos.size >= 4) {

                Toast.makeText(
                    this,
                    "Máximo 4 contactos",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            mostrarDialogoContacto()
        }

        // =====================================================
        // 🔥 BOTÓN REGISTRARSE
        // =====================================================
        btnRegistrarse.setOnClickListener {

            val nombre =
                etNombre.text.toString().trim()

            val apellido =
                etApellido.text.toString().trim()

            val correo =
                etCorreo.text.toString().trim()

            val contrasenia =
                etContrasenia.text.toString().trim()

            // =================================================
            // 🔥 VALIDACIONES
            // =================================================

            if (nombre.isEmpty()) {

                etNombre.error =
                    "Ingrese su nombre"

                etNombre.requestFocus()

                return@setOnClickListener
            }

            if (apellido.isEmpty()) {

                etApellido.error =
                    "Ingrese su apellido"

                etApellido.requestFocus()

                return@setOnClickListener
            }

            if (correo.isEmpty()) {

                etCorreo.error =
                    "Ingrese su correo"

                etCorreo.requestFocus()

                return@setOnClickListener
            }

            if (
                !Patterns.EMAIL_ADDRESS
                    .matcher(correo)
                    .matches()
            ) {

                etCorreo.error =
                    "Correo inválido"

                etCorreo.requestFocus()

                return@setOnClickListener
            }

            if (contrasenia.isEmpty()) {

                etContrasenia.error =
                    "Ingrese contraseña"

                etContrasenia.requestFocus()

                return@setOnClickListener
            }

            if (
                contrasenia.length < 6 ||
                contrasenia.length > 10
            ) {

                etContrasenia.error =
                    "La contraseña debe tener entre 6 y 10 caracteres"

                etContrasenia.requestFocus()

                return@setOnClickListener
            }

            if (listaContactos.isEmpty()) {

                Toast.makeText(
                    this,
                    "Agrega al menos 1 contacto",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // =================================================
            // 🔥 NUEVO
            // 👉 ENVÍA OTP
            // =================================================
            enviarCodigo(
                nombre,
                apellido,
                correo,
                contrasenia
            )
        }

        // =====================================================
        // 🔥 BOTÓN SALIR
        // =====================================================
        btnSalir.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finish()
        }
    }

    // =====================================================
    // 🔥 DIALOGO CONTACTO
    // =====================================================
    private fun mostrarDialogoContacto() {

        val view =
            layoutInflater.inflate(
                R.layout.dialog_contacto,
                null
            )

        val etNombre =
            view.findViewById<EditText>(
                R.id.etNombreContacto
            )

        val etApellido =
            view.findViewById<EditText>(
                R.id.etApellidoContacto
            )

        val etTelefono =
            view.findViewById<EditText>(
                R.id.etTelefonoContacto
            )

        val etCorreo =
            view.findViewById<EditText>(
                R.id.etCorreoContacto
            )

        val spRelacion =
            view.findViewById<Spinner>(
                R.id.spRelacion
            )

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

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            relaciones
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spRelacion.adapter = adapter

        val dialog =
            android.app.AlertDialog.Builder(this)

                .setTitle(
                    "Agregar contacto"
                )

                .setView(view)

                .setPositiveButton(
                    "Guardar",
                    null
                )

                .setNegativeButton(
                    "Cancelar",
                    null
                )

                .create()

        dialog.setOnShowListener {

            val button =
                dialog.getButton(
                    android.app.AlertDialog.BUTTON_POSITIVE
                )

            button.setOnClickListener {

                val nombre =
                    etNombre.text.toString().trim()

                val apellido =
                    etApellido.text.toString().trim()

                val telefono =
                    etTelefono.text.toString().trim()

                val correo =
                    etCorreo.text.toString().trim()

                val relacion =
                    spRelacion.selectedItem.toString()

                if (nombre.isEmpty()) {

                    etNombre.error =
                        "Ingrese nombre"

                    return@setOnClickListener
                }

                if (apellido.isEmpty()) {

                    etApellido.error =
                        "Ingrese apellido"

                    return@setOnClickListener
                }

                if (
                    !telefono.matches(
                        Regex("^9\\d{8}$")
                    )
                ) {

                    etTelefono.error =
                        "El teléfono debe comenzar con 9 y tener 9 dígitos"

                    return@setOnClickListener
                }

                if (
                    !Patterns.EMAIL_ADDRESS
                        .matcher(correo)
                        .matches()
                ) {

                    etCorreo.error =
                        "Correo inválido"

                    return@setOnClickListener
                }

                if (
                    spRelacion.selectedItemPosition == 0
                ) {

                    Toast.makeText(
                        this,
                        "Seleccione relación",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                val contacto = JSONObject()

                contacto.put(
                    "nombre",
                    nombre
                )

                contacto.put(
                    "apellido",
                    apellido
                )

                contacto.put(
                    "telefono",
                    telefono
                )

                contacto.put(
                    "correo",
                    correo
                )

                contacto.put(
                    "relacion",
                    relacion
                )

                listaContactos.add(contacto)

                tvContactos.text =
                    "Contactos: ${listaContactos.size}/4"

                Toast.makeText(
                    this,
                    "Contacto agregado",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    // =====================================================
    // 🔥 NUEVO
    // 👉 ENVÍA CÓDIGO OTP
    // =====================================================
    private fun enviarCodigo(
        nombre: String,
        apellido: String,
        correo: String,
        contrasenia: String
    ) {

        val json = JSONObject()

        json.put("correo", correo)

        val body = RequestBody.create(
            "application/json; charset=utf-8"
                .toMediaTypeOrNull(),

            json.toString()
        )

        val request = Request.Builder()

            .url(
                "$BASE_URL/api/usuarios/enviar-codigo"
            )

            .post(body)

            .build()

        // =====================================================
        // 🔥 CLIENTE OKHTTP
        // 👉 AUMENTAR TIMEOUT
        // =====================================================
        val client = OkHttpClient.Builder()

            .connectTimeout(
                60,
                java.util.concurrent.TimeUnit.SECONDS
            )

            .readTimeout(
                60,
                java.util.concurrent.TimeUnit.SECONDS
            )

            .writeTimeout(
                60,
                java.util.concurrent.TimeUnit.SECONDS
            )

            .addInterceptor(
                HttpLoggingInterceptor().apply {

                    level =
                        HttpLoggingInterceptor.Level.BODY
                }
            )

            .build()

        // =====================================================
        // 🔥 LLAMADA AL BACKEND
        // =====================================================
        client.newCall(request)

            .enqueue(object : Callback {

                override fun onFailure(
                    call: Call,
                    e: IOException
                ) {

                    e.printStackTrace()

                    runOnUiThread {

                        Toast.makeText(
                            this@RegisterActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response
                ) {

                    val responseBody =
                        response.body?.string()

                    println(response.code)

                    if (response.isSuccessful) {

                        runOnUiThread {

                            Toast.makeText(
                                this@RegisterActivity,
                                "Código enviado al correo",
                                Toast.LENGTH_LONG
                            ).show()

                            window.decorView.post {

                                mostrarDialogoCodigo(
                                    nombre,
                                    apellido,
                                    correo,
                                    contrasenia
                                )
                            }
                        }
                    }

                    else {

                        runOnUiThread {

                            etCorreo.error =
                                responseBody ?: "Error"

                            etCorreo.requestFocus()

                            Toast.makeText(
                                this@RegisterActivity,
                                responseBody ?: "Error",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    response.close()
                }
            })
    }

    // =====================================================
    // 🔥 NUEVO
    // 👉 DIALOGO OTP
    // =====================================================
    private fun mostrarDialogoCodigo(
        nombre: String,
        apellido: String,
        correo: String,
        contrasenia: String
    ) {

        val editText =
            EditText(this)

        editText.hint =
            "Ingrese código"

        android.app.AlertDialog.Builder(this)

            .setTitle(
                "Verificación"
            )

            .setMessage(
                "Ingrese el código enviado al correo"
            )

            .setView(editText)

            .setPositiveButton(
                "Verificar"
            ) { _, _ ->

                val codigo =
                    editText.text.toString()

                verificarCodigo(

                    nombre,
                    apellido,
                    correo,
                    contrasenia,
                    codigo
                )
            }

            .setNegativeButton(
                "Cancelar",
                null
            )

            .show()
    }

    // =====================================================
    // 🔥 VERIFICAR OTP Y REGISTRAR
    // =====================================================
    private fun verificarCodigo(
        nombre: String,
        apellido: String,
        correo: String,
        contrasenia: String,
        codigo: String
    ) {

        val usuario = JSONObject()

        usuario.put(
            "nombre",
            nombre
        )

        usuario.put(
            "apellido",
            apellido
        )

        usuario.put(
            "correo",
            correo
        )

        usuario.put(
            "contraseña",
            contrasenia
        )

        usuario.put(
            "contactosEmergencia",
            JSONArray(listaContactos)
        )

        val json = JSONObject()

        json.put(
            "correo",
            correo
        )

        json.put(
            "codigo",
            codigo
        )

        json.put(
            "usuario",
            usuario
        )

        println("================================")
        println("JSON ENVIADO")
        println(json.toString())
        println("================================")

        val body = RequestBody.create(
            "application/json; charset=utf-8"
                .toMediaTypeOrNull(),

            json.toString()
        )

        val request = Request.Builder()

            .url(
                "$BASE_URL/api/usuarios/verificar-codigo"
            )

            .post(body)

            .build()

        val client = OkHttpClient.Builder()

            .connectTimeout(
                60,
                java.util.concurrent.TimeUnit.SECONDS
            )

            .readTimeout(
                60,
                java.util.concurrent.TimeUnit.SECONDS
            )

            .writeTimeout(
                60,
                java.util.concurrent.TimeUnit.SECONDS
            )

            .addInterceptor(
                HttpLoggingInterceptor().apply {

                    level =
                        HttpLoggingInterceptor.Level.BODY
                }
            )

            .build()

        client.newCall(request)

            .enqueue(object : Callback {

                override fun onFailure(
                    call: Call,
                    e: IOException
                ) {

                    e.printStackTrace()

                    runOnUiThread {

                        Toast.makeText(
                            this@RegisterActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response
                ) {

                    val responseBody =
                        response.body?.string()

                    println("================================")
                    println("RESPUESTA BACKEND")
                    println(responseBody)
                    println("================================")

                    runOnUiThread {

                        if (response.isSuccessful) {

                            Toast.makeText(
                                this@RegisterActivity,
                                "Registro exitoso",
                                Toast.LENGTH_LONG
                            ).show()

                            etNombre.text.clear()

                            etApellido.text.clear()

                            etCorreo.text.clear()

                            etContrasenia.text.clear()

                            listaContactos.clear()

                            tvContactos.text =
                                "Contactos: 0/4"

                            startActivity(
                                Intent(
                                    this@RegisterActivity,
                                    LoginActivity::class.java
                                )
                            )

                            finish()
                        }

                        else {

                            Toast.makeText(
                                this@RegisterActivity,
                                responseBody
                                    ?: "Código incorrecto",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    response.close()
                }
            })
    }
}