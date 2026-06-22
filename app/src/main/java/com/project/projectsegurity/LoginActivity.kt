//package com.project.projectsegurity
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log // 🔥 CAMBIO 1: Para debug
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.project.projectsegurity.databinding.ActivityLoginBinding
//import okhttp3.*
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import org.json.JSONObject
//
//class LoginActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityLoginBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityLoginBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.btnIniciarSesion.setOnClickListener {
//            iniciarSesion() // 🔥 CAMBIO 2: ahora llama al backend real
//        }
//
//        binding.btnRegistrarse.setOnClickListener {
//            startActivity(Intent(this, RegisterActivity::class.java))
//        }
//
//        binding.tvOlvidoContrasena.setOnClickListener {
//            startActivity(Intent(this, ForgotPasswordActivity::class.java))
//        }
//    }
//
//    private fun iniciarSesion() {
//        val correo = binding.etCorreo.text.toString().trim()
//        val contrasena = binding.etContrasena.text.toString().trim()
//
//        if (correo.isEmpty() || contrasena.isEmpty()) {
//            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        Thread {
//            try {
//
//                // 🔥 CAMBIO 3: JSON correcto para backend
//                val json = """
//                    {
//                        "correo": "$correo",
//                        "contraseña": "$contrasena"
//                    }
//                """.trimIndent()
//
//                val body = RequestBody.create(
//                    "application/json".toMediaTypeOrNull(),
//                    json
//                )
//
//                // 🔥 CAMBIO 4: URL LOCAL (emulador)
//                val request = Request.Builder()
//                    .url("http://192.168.18.238:8080/api/usuarios/login")
//                    .post(body)
//                    .build()
//
//                val client = OkHttpClient()
//                val response = client.newCall(request).execute()
//
//                val responseBody = response.body?.string()
//
//                // 🔥 CAMBIO 5: LOGS PARA DEBUG (CLAVE)
//                Log.e("LOGIN_DEBUG", "CODE: ${response.code}")
//                Log.e("LOGIN_DEBUG", "BODY: $responseBody")
//
//                runOnUiThread {
//
//                    try {
//
//                        // 🔥 CAMBIO 6: Validar respuesta HTTP
//                        if (!response.isSuccessful) {
//                            Toast.makeText(
//                                this,
//                                "Error en la Autenticación de usuario y contraseña",
//                                //"Error login: ${response.code}",
//                                Toast.LENGTH_LONG
//                            ).show()
//                            return@runOnUiThread
//                        }
//
//                        // 🔥 CAMBIO 7: Validar body
//                        if (responseBody.isNullOrEmpty()) {
//                            Toast.makeText(
//                                this,
//                                "Respuesta vacía del servidor",
//                                Toast.LENGTH_LONG
//                            ).show()
//                            return@runOnUiThread
//                        }
//
//                        // 🔥 CAMBIO 8: Validar que sea JSON
//                        if (!responseBody.trim().startsWith("{")) {
//                            Toast.makeText(
//                                this,
//                                "Respuesta inválida del servidor",
//                                Toast.LENGTH_LONG
//                            ).show()
//                            return@runOnUiThread
//                        }
//
//                        val jsonResponse = JSONObject(responseBody)
//
//                        // 🔥 CAMBIO 9: Validar token
//                        if (!jsonResponse.has("token")) {
//                            Toast.makeText(
//                                this,
//                                "No se recibió token",
//                                Toast.LENGTH_LONG
//                            ).show()
//                            return@runOnUiThread
//                        }
//
//                        val token = jsonResponse.getString("token")
//
//                        // 🔥 CAMBIO 10: Guardar token
//                        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
//                        prefs.edit().putString("TOKEN", token).apply()
//
//                        Toast.makeText(this, "Login exitoso ✅", Toast.LENGTH_SHORT).show()
//
//                        startActivity(Intent(this, MainMenuActivity::class.java))
//                        finish()
//
//                    } catch (e: Exception) {
//                        // 🔥 CAMBIO 11: evitar crash por JSON
//                        e.printStackTrace()
//                        Toast.makeText(
//                            this,
//                            "Error procesando respuesta: ${e.message}",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
//
//            } catch (e: Exception) {
//                // 🔥 CAMBIO 12: capturar errores de red
//                e.printStackTrace()
//                runOnUiThread {
//                    Toast.makeText(
//                        this,
//                        "Error conexión: ${e.message}",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }.start()
//    }
//}



package com.project.projectsegurity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.project.projectsegurity.databinding.ActivityLoginBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var client: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // =====================================================
        // 🔥 INICIO CAMBIO: VERIFICAR SESIÓN ACTIVA
        // 👉 Si hay token guardado → entra directo
        // 👉 Mantiene sesión incluso si se cerró la app
        // =====================================================
        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val token = prefs.getString("TOKEN", null)

        if (token != null) {
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish()
            return
        }
        // 🔥 FIN CAMBIO
        // =====================================================

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // =====================================================
        // 🔥 INICIO CAMBIO: OKHTTP CON INTERCEPTOR JWT
        // 👉 Agrega automáticamente el token a TODAS las requests
        // =====================================================
        client = OkHttpClient.Builder()
            .addInterceptor { chain ->

                val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                val token = prefs.getString("TOKEN", null)

                val request = if (token != null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }

                val response = chain.proceed(request)

                // =====================================================
                // 🔥 INICIO CAMBIO: DETECTAR TOKEN EXPIRADO
                // 👉 Si backend responde 401 → eliminar sesión
                // 👉 Redirigir a login
                // =====================================================
                if (response.code == 401) {

                    val prefs2 = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                    prefs2.edit().remove("TOKEN").apply()

                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Sesión expirada, inicia sesión nuevamente",
                            Toast.LENGTH_LONG
                        ).show()

                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                }
                // 🔥 FIN CAMBIO
                // =====================================================

                response
            }
            .build()
        // 🔥 FIN CAMBIO
        // =====================================================

        binding.btnIniciarSesion.setOnClickListener {
            iniciarSesion()
        }

        binding.btnRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvOlvidoContrasena.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun iniciarSesion() {

        val correo = binding.etCorreo.text.toString().trim()
        val contrasena = binding.etContrasena.text.toString().trim()

        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {

                val json = """
                    {
                        "correo": "$correo",
                        "contraseña": "$contrasena"
                    }
                """.trimIndent()

                val body = RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    json
                )

                val request = Request.Builder()
                    .url(
                        //"http://192.168.18.238:8080/api/usuarios/login"
                       // "https://appalertacomunitaria.com/api/usuarios/login"
                       // "http://192.168.18.238:8080/api/usuarios/login"
                    "http://projectsecuritypeople-env.eba-k3djm54f.us-east-2.elasticbeanstalk.com/api/usuarios/login"
                    )
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.e("LOGIN_DEBUG", "CODE: ${response.code}")
                Log.e("LOGIN_DEBUG", "BODY: $responseBody")

                runOnUiThread {

                    try {

                        if (!response.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Credenciales incorrectas",
                                Toast.LENGTH_LONG
                            ).show()
                            return@runOnUiThread
                        }

                        if (responseBody.isNullOrEmpty()) {
                            Toast.makeText(
                                this,
                                "Respuesta vacía",
                                Toast.LENGTH_LONG
                            ).show()
                            return@runOnUiThread
                        }

                        val jsonResponse = JSONObject(responseBody)

                        if (!jsonResponse.has("token")) {
                            Toast.makeText(
                                this,
                                "No se recibió token",
                                Toast.LENGTH_LONG
                            ).show()
                            return@runOnUiThread
                        }

                        val token = jsonResponse.getString("token")

                        // =====================================================
                        // 🔥 INICIO CAMBIO: GUARDAR TOKEN (SESIÓN PERSISTENTE)
                        // 👉 Se guarda aunque cierres la app
                        // 👉 Permite login automático
                        // =====================================================
                        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                        prefs.edit().putString("TOKEN", token).apply()
                        // 🔥 FIN CAMBIO
                        // =====================================================

                        Toast.makeText(this, "Login exitoso ✅", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this, MainMenuActivity::class.java))
                        finish()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this,
                            "Error procesando respuesta",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Error de conexión",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    // =====================================================
    // 🔥 INICIO CAMBIO: MÉTODO LOGOUT
    // 👉 Borra token
    // 👉 Cierra sesión completamente
    // =====================================================
    private fun cerrarSesion() {

        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        prefs.edit().clear().apply()

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    // 🔥 FIN CAMBIO
    // =====================================================
}