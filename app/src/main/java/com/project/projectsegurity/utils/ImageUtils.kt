package com.project.projectsegurity.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {

    // Convierte un URI a un Bitmap
    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Obtiene la ruta real del archivo desde un URI
    fun getPathFromUri(context: Context, uri: Uri): String {
        // Para URIs de tipo file://
        if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path ?: ""
        }

        // Para Android >= Q, copiamos el archivo temporalmente
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "temp_${System.currentTimeMillis()}.jpg"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            return file.absolutePath
        }

        // Para versiones anteriores
        var path: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return path ?: copiarArchivoTemporal(context, uri)
    }

    // Copia el archivo a la carpeta temporal si no se puede obtener la ruta
    private fun copiarArchivoTemporal(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "temp_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file.absolutePath
    }
}
