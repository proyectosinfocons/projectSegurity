package com.project.projectsegurity.detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream

data class Detection(val label: String, val confidence: Float)

class ObjectDetector(private val context: Context) {

    private var model: Module? = null
    private val labels = listOf("robo") // ✅ orden correcto

    init {
        try {
            val modelPath = assetFilePath(context, "yolov8n.torchscript.pt")
            model = Module.load(modelPath)
            Log.d("YOLOv8", "✅ Modelo TorchScript cargado correctamente desde: $modelPath")
        } catch (e: Exception) {
            Log.e("YOLOv8", "❌ Error al cargar modelo: ${e.message}")
        }
    }

    fun detectObjects(bitmap: Bitmap): List<Detection> {
        val localModel = model ?: return emptyList()
        return try {
            val resized = Bitmap.createScaledBitmap(bitmap, 640, 640, true)
            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                resized,
                floatArrayOf(0f, 0f, 0f),
                floatArrayOf(1f, 1f, 1f)
            )

            val outputTensor = localModel.forward(IValue.from(inputTensor)).toTensor()
            val scores = outputTensor.dataAsFloatArray

            val numCols = 6
            val numRows = scores.size / numCols
            val detections = mutableListOf<Detection>()

            for (i in 0 until numRows) {
                val conf = scores[i * numCols + 4]
                val clsId = scores[i * numCols + 5].toInt()
                if (conf > 0.4f && clsId in labels.indices) {
                    val label = labels[clsId]
                    detections.add(Detection(label, conf))
                }
            }

            detections
        } catch (e: Exception) {
            Log.e("YOLOv8", "❌ Error durante la detección: ${e.message}")
            emptyList()
        }
    }

    fun detectFromFile(file: File): Detection? {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val tensor = TensorImageUtils.bitmapToFloat32Tensor(
            bitmap,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
            TensorImageUtils.TORCHVISION_NORM_STD_RGB
        )

        // ⚙️ Si el modelo aún no está cargado, no hacer nada
        return model?.let { m ->
            val output = m.forward(IValue.from(tensor)).toTensor()
            val scores = output.dataAsFloatArray
            val maxIdx = scores.indices.maxByOrNull { scores[it] } ?: -1
            if (maxIdx != -1) Detection(labels[maxIdx], scores[maxIdx]) else null
        }
    }


    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) return file.absolutePath
        context.assets.open(assetName).use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }
}
