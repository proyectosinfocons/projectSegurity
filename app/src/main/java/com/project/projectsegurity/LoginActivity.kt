package com.project.projectsegurity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.project.projectsegurity.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Eventos de botones ---
        binding.btnIniciarSesion.setOnClickListener {
            iniciarSesion()
        }

        binding.btnRegistrarse.setOnClickListener {
            registrarse()
        }

        binding.tvOlvidoContrasena.setOnClickListener {
            Toast.makeText(this, "Funcionalidad para recuperar contraseña", Toast.LENGTH_SHORT).show()
        }

        // 🔐 Recuperar contraseña
        binding.tvOlvidoContrasena.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

    }

    private fun iniciarSesion() {
        val correo = binding.etCorreo.text.toString().trim()
        val contrasena = binding.etContrasena.text.toString().trim()

        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (correo == "admin@correo.com" && contrasena == "1234") {
            Toast.makeText(this, "Inicio de sesión exitoso ✅", Toast.LENGTH_SHORT).show()

            // 👉 Redirigir al menú principal
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
            finish() // opcional, evita volver al login con el botón "atrás"

        } else {
            Toast.makeText(this, "Correo o contraseña incorrectos ❌", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registrarse() {
        // 🔹 Redirige a la pantalla de registro
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
