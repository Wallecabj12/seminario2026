package com.geovanivargas.planetamusica.audio.synth

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object AudioExportHelper {

    /**
     * Comparte un archivo de audio (WAV/MP3) con otras aplicaciones del sistema (WhatsApp, Telegram, Drive, reproductor, etc.)
     */
    fun shareAudio(context: Context, file: File, title: String = "Compartir sonido sintetizado") {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Guarda una copia del archivo de audio en la carpeta pública de Música / Descargas del dispositivo
     */
    fun exportToMusicStorage(context: Context, sourceFile: File, displayName: String): Result<String> {
        return try {
            val fileName = if (displayName.endsWith(".wav", ignoreCase = true) || displayName.endsWith(".mp3", ignoreCase = true)) {
                displayName
            } else {
                "$displayName.wav"
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/SoundCraft")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return Result.failure(Exception("No se pudo crear entrada en MediaStore"))

                resolver.openOutputStream(uri)?.use { output ->
                    FileInputStream(sourceFile).use { input ->
                        input.copyTo(output)
                    }
                }

                values.clear()
                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)

                Result.success("Guardado en Música/SoundCraft/$fileName")
            } else {
                val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                val targetDir = File(musicDir, "SoundCraft").also { it.mkdirs() }
                val destFile = File(targetDir, fileName)

                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Result.success("Guardado en: ${destFile.absolutePath}")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
