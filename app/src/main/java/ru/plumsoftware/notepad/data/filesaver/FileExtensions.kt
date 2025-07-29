package ru.plumsoftware.notepad.data.filesaver

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    val photosDir = File(context.filesDir, "photos")
    if (!photosDir.exists()) photosDir.mkdirs()
    val file = File(photosDir, "photo_${UUID.randomUUID()}.jpg")
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun deleteImagesFromStorage(context: Context, photoPaths: List<String>) {
    photoPaths.forEach { path ->
        val file = File(path)
        if (file.exists()) file.delete()
    }
}