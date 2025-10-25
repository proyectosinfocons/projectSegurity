package com.project.projectsegurity

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu) // <-- debe coincidir con tu archivo XML

        // Importante: findViewById después de setContentView
        val layoutReportarDelito = findViewById<LinearLayout>(R.id.layoutReportarDelito)

        layoutReportarDelito.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
