package com.project.projectsegurity.detector

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VideoAnalyzer(private val context: Context, private val detector: ObjectDetector) {

    suspend fun analyzeVideo(videoFile: File): List<Detection> = withContext(Dispatchers.Default) {
        val retriever = MediaMetadataRetriever()
        val detections = mutableListOf<Detection>()

        try {
            retriever.setDataSource(videoFile.absolutePath)
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val frameInterval = 1000L // cada 1 segundo

            var time = 0L
            while (time < duration) {
                val frameBitmap: Bitmap? = retriever.getFrameAtTime(
                    time * 1000,
                    MediaMetadataRetriever.OPTION_CLOSEST
                )

                frameBitmap?.let {
                    val frameDetections = detector.detectObjects(it)
                    detections.addAll(frameDetections)
                    Log.d("VideoAnalyzer", "🧠 Frame ${time / 1000}s: ${frameDetections.joinToString()}")
                }

                time += frameInterval
            }

        } catch (e: Exception) {
            Log.e("VideoAnalyzer", "❌ Error analizando video: ${e.message}")
        } finally {
            retriever.release()
        }

        detections
    }


}
