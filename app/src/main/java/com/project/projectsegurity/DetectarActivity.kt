package com.project.projectsegurity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.project.projectsegurity.detector.ObjectDetector
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DetectarActivity : AppCompatActivity() {

    private lateinit var previewView: androidx.camera.view.PreviewView
    private lateinit var btnCapture: Button
    private lateinit var btnConfirmar: Button
    private lateinit var resultText: TextView
    private lateinit var detector: ObjectDetector

    private var imageCapture: ImageCapture? = null
    private var lastCapturedBitmap: Bitmap? = null
    private var lastResultText: String? = null
    private var photoFile: File? = null

    private val CAMERA_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detectar)

        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btnCapture)
        btnConfirmar = findViewById(R.id.btnConfirmar)
        resultText = findViewById(R.id.resultText)

        detector = ObjectDetector(this)
        btnConfirmar.isEnabled = false

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE
            )
        } else {
            startCamera()
        }

        btnCapture.setOnClickListener { captureImage() }

        btnConfirmar.setOnClickListener {
            if (photoFile != null && photoFile!!.exists()) {
                val intent = Intent()
                intent.putExtra("file_name", photoFile!!.name)
                intent.putExtra("file_path", photoFile!!.absolutePath)
                intent.putExtra("descripcion_auto", "Se detectó un acto criminal")
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, "⚠️ No se encontró el archivo capturado", Toast.LENGTH_SHORT).show()
            }
        }

        if (savedInstanceState != null) restoreInstanceState(savedInstanceState)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Error: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        val imageCapture = imageCapture ?: return
        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.toBitmap()
                    image.close()

                    runOnUiThread {
                        if (bitmap != null) {
                            try {
                                val detections = detector.detectObjects(bitmap)
                                if (detections.isNotEmpty()) {
                                    val delito = detections.firstOrNull {
                                        it.label.equals("robo", true) ||
                                                it.label.equals("incendio", true)
                                    }

                                    if (delito != null) {
                                        val porcentaje = String.format("%.1f", delito.confidence * 100)
                                        val text =
                                            "🚨 Acto delictivo detectado: ${delito.label.uppercase()}\nConfianza: $porcentaje%"
                                        resultText.text = text
                                        lastResultText = text
                                        lastCapturedBitmap = bitmap
                                        previewView.foreground = BitmapDrawable(resources, bitmap)
                                        btnCapture.isEnabled = false
                                        btnConfirmar.isEnabled = true
                                        photoFile = saveCompressedBitmap(bitmap)
                                    } else {
                                        resultText.text = "✅ No se detectaron actividades sospechosas."
                                        lastResultText = resultText.text.toString()
                                    }
                                } else {
                                    resultText.text = "✅ No se detectaron actividades sospechosas."
                                    lastResultText = resultText.text.toString()
                                }
                            } catch (e: Exception) {
                                resultText.text = "❌ Error al analizar: ${e.message}"
                                lastResultText = resultText.text.toString()
                            }
                        } else {
                            resultText.text = "❌ No se pudo obtener la imagen."
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Error: ${exception.message}")
                    Toast.makeText(this@DetectarActivity, "Error al capturar imagen.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /** Guardar bitmap comprimido para evitar “Request Entity Too Large” */
    private fun saveCompressedBitmap(bitmap: Bitmap): File {
        val fileName = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        val file = File(externalCacheDir, fileName)

        var compressQuality = 90
        var stream: ByteArrayOutputStream
        do {
            stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, stream)
            compressQuality -= 10
        } while (stream.size() / 1024 > 500 && compressQuality > 10) // límite ~500KB

        FileOutputStream(file).use { fos -> fos.write(stream.toByteArray()) }
        return file
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lastResultText?.let { outState.putString("lastResultText", it) }
        photoFile?.let { outState.putString("photoPath", it.absolutePath) }
        outState.putBoolean("btnCaptureEnabled", btnCapture.isEnabled)
        outState.putBoolean("btnConfirmarEnabled", btnConfirmar.isEnabled)
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        lastResultText = savedInstanceState.getString("lastResultText")
        resultText.text = lastResultText ?: ""
        val photoPath = savedInstanceState.getString("photoPath")
        if (!photoPath.isNullOrEmpty()) {
            val file = File(photoPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(photoPath)
                previewView.foreground = BitmapDrawable(resources, bitmap)
                lastCapturedBitmap = bitmap
                photoFile = file
            }
        }
        btnCapture.isEnabled = savedInstanceState.getBoolean("btnCaptureEnabled", true)
        btnConfirmar.isEnabled = savedInstanceState.getBoolean("btnConfirmarEnabled", false)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/** Extensión para convertir ImageProxy a Bitmap */
private fun ImageProxy.toBitmap(): Bitmap? {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
