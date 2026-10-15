package com.geovanivargas.planetamusica.data.sampler

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

class FileManager(private val context: Context) {

    companion object {
        const val SOUNDS_DIR = "sounds"
        const val MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024   // 10 MB
        const val MAX_TOTAL_SIZE_BYTES = 50L * 1024 * 1024  // 50 MB
        private val ALLOWED_MIME_TYPES = setOf(
            "audio/mpeg", "audio/mp3", "audio/wav", "audio/x-wav", "audio/vnd.wave",
        )
    }

    private val soundsDir: File
        get() = File(context.filesDir, SOUNDS_DIR).also { it.mkdirs() }

    fun getStorageUsedBytes(): Long =
        soundsDir.listFiles()?.sumOf { it.length() } ?: 0L

    fun getFileSize(uri: Uri): Long =
        context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L

    fun isValidAudioFormat(uri: Uri): Boolean {
        val mime = context.contentResolver.getType(uri) ?: return false
        return mime in ALLOWED_MIME_TYPES
    }

    fun getFileName(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) return c.getString(idx)
            }
        }
        return "sound_${System.currentTimeMillis()}"
    }

    fun copyFileToInternal(uri: Uri): Result<String> {
        val fileSize = getFileSize(uri)
        if (fileSize > MAX_FILE_SIZE_BYTES) {
            return Result.failure(Exception("El archivo supera el límite de 10 MB."))
        }
        val used = getStorageUsedBytes()
        if (used + fileSize > MAX_TOTAL_SIZE_BYTES) {
            return Result.failure(
                Exception("Almacenamiento lleno (${formatMb(used)} / ${formatMb(MAX_TOTAL_SIZE_BYTES)} usados)."),
            )
        }
        val fileName = "${System.currentTimeMillis()}_${getFileName(uri)}"
        val dest = File(soundsDir, fileName)
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            Result.success(dest.absolutePath)
        } catch (e: Exception) {
            dest.delete()
            Result.failure(Exception("Error al copiar el archivo."))
        }
    }

    fun deleteFile(filePath: String): Boolean = File(filePath).delete()

    private fun formatMb(bytes: Long): String =
        "%.1f MB".format(bytes / (1024.0 * 1024.0))
}
