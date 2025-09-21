package com.iamravi.vidlab.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File
import java.io.InputStream
import java.util.UUID

suspend fun copyUriToCacheFile(
    context: Context, source: Uri, fileNamePrefix: String = "clip"
): File {
    val input: InputStream = context.contentResolver.openInputStream(source)
        ?: throw IllegalArgumentException("Cannot open input stream from Uri")
    val outputFile =
        File.createTempFile("${fileNamePrefix}_${UUID.randomUUID()}", ".mp4", context.cacheDir)

    input.use { inputStream ->
        outputFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }


    }
    try {
        context.contentResolver.takePersistableUriPermission(
            source, Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    } catch (_: Exception) {

    }
    return outputFile

}
