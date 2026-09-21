package ru.plumsoftware.notepad.data.filesaver

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import ru.plumsoftware.notepad.data.model.NoteFile
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

/** Копирует выбранный документ во внутреннее хранилище и возвращает [NoteFile] с исходным именем и типом. */
fun saveDocumentToInternalStorage(context: Context, uri: Uri): NoteFile? {
    val filesDir = File(context.filesDir, "documents")
    if (!filesDir.exists()) filesDir.mkdirs()

    val displayName = queryDisplayName(context, uri) ?: "file_${System.currentTimeMillis()}"
    val mime = context.contentResolver.getType(uri)
        ?: MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(displayName.substringAfterLast('.', "").lowercase())
        ?: "application/octet-stream"

    val ext = displayName.substringAfterLast('.', "")
    val storedName = "doc_${UUID.randomUUID()}" + if (ext.isNotEmpty()) ".$ext" else ""
    val file = File(filesDir, storedName)
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        NoteFile(path = file.absolutePath, name = displayName, mime = mime)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun deleteFilesFromStorage(paths: List<String>) {
    paths.forEach { path ->
        val file = File(path)
        if (file.exists()) file.delete()
    }
}

private fun queryDisplayName(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
    } catch (e: Exception) {
        null
    }
}

/** Путь для новой голосовой записи заметки во внутреннем хранилище. */
fun createVoiceFile(context: Context): File {
    val voiceDir = File(context.filesDir, "voice")
    if (!voiceDir.exists()) voiceDir.mkdirs()
    return File(voiceDir, "voice_${UUID.randomUUID()}.m4a")
}