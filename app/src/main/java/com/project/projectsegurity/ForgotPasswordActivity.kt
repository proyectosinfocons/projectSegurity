package com.project.projectsegurity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import java.io.IOException
import java.net.URLEncoder

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSend: Button
    private lateinit var btnExit: Button

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etEmail = findViewById(R.id.etEmail)
        btnSend = findViewById(R.id.btnSend)
        btnExit = findViewById(R.id.btnExit)


        btnSend.setOnClickListener {
            val email = etEmail.text?.toString()?.trim() ?: ""

            if (!isValidEmail(email)) {
                etEmail.error = "Ingresa un correo válido"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            sendRecoveryRequest(email)
        }


        btnExit.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun sendRecoveryRequest(email: String) {
        val url = "http://192.168.18.238:8080/api/usuarios/recuperar?correo=$email"
        //val url = "https://appalertacomunitaria.com/api/usuarios/recuperar?correo=$email"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "⚠ Error de conexión con el servidor",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "📩 Revisa tu correo",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "❌ No existe usuario con ese correo",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }


    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
