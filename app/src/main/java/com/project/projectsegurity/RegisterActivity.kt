package com.project.projectsegurity

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etApellido: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContrasenia: EditText
    private lateinit var btnRegistrarse: Button
    private lateinit var btnSalir: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre)
        etApellido = findViewById(R.id.etApellido)
        etCorreo = findViewById(R.id.etCorreo)
        etContrasenia = findViewById(R.id.etContrasenia)
        btnRegistrarse = findViewById(R.id.btnRegistrarse)
        btnSalir = findViewById(R.id.btnSalir)

        // --- Evento del botón REGISTRARSE ---
        btnRegistrarse.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val contrasenia = etContrasenia.text.toString().trim()

            if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() || contrasenia.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "Correo electrónico inválido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registrarUsuario(nombre, apellido, correo, contrasenia)
        }

        // --- Evento del botón SALIR ---
        btnSalir.setOnClickListener {
            Toast.makeText(this, "Regresando al inicio de sesión", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registrarUsuario(nombre: String, apellido: String, correo: String, contrasenia: String) {
        // JSON del cuerpo
        val json = JSONObject().apply {
            put("nombre", nombre)
            put("apellido", apellido)
            put("correo", correo)
            put("contraseña", contrasenia)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, json.toString())

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        // ⚠️ Reemplaza con tu URL correcta si usas servidor local
        val request = Request.Builder()
            //.url("http://192.168.18.238:8086/api/usuarios/registro")
            .url("http://projectsecuritypeople-env-1.eba-jum2mh2y.us-east-1.elasticbeanstalk.com/api/usuarios/registro")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@RegisterActivity,
                        "⚠️ Error de conexión: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    val code = response.code
                    val bodyStr = response.body?.string()

                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "✅ Registro exitoso, inicia sesión ahora",
                            Toast.LENGTH_LONG
                        ).show()

                        // Limpiar campos
                        etNombre.text.clear()
                        etApellido.text.clear()
                        etCorreo.text.clear()
                        etContrasenia.text.clear()

                        // Redirigir al LoginActivity
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val msg = bodyStr ?: "Error $code"
                        Toast.makeText(
                            this@RegisterActivity,
                            "❌ No se pudo registrar: $msg",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    response.close()
                }
            }
        })
    }
}
