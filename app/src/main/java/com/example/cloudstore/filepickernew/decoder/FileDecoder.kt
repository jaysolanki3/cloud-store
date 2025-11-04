package com.fermax.filepickernew.decoder

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.net.toUri
import com.example.cloudstore.filepickernew.decoder.PathUtil
import com.example.cloudstore.filepickernew.decoder.Utils
import com.example.filepickerdemo.result.FileMeta
import com.example.filepickerdemo.stream.FileStreamer
import com.example.filepickerdemo.stream.Streamer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FileDecoder(private val context: Context,private val contentResolver: ContentResolver) {

    private val streamer: Streamer = FileStreamer() // Assuming FileStreamer class is available

    fun decodeFile(uri: Uri): FileMeta? {
        var pfd: ParcelFileDescriptor? = null
        return try {
            pfd = contentResolver.openFileDescriptor(uri, "r")
            val inputStream = FileInputStream(pfd?.fileDescriptor)
            val meta = getFileMeta(uri) ?: return null
            val fileExtension = Utils.getExtention(meta.third)
            var name = meta.first
            if (!name.contains(fileExtension)) {
                name = meta.first +".${fileExtension}"
            }

            val pdfFile = File(context.cacheDir, name)
            val outputStream = FileOutputStream(pdfFile)
            val filepath = PathUtil.getPath(context, pdfFile.toUri()) // Assuming PathUtil is available
            streamer.copyFile(inputStream, outputStream)
            FileMeta(meta.first, meta.second, pdfFile, filepath, meta.third)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            pfd?.close()
        }
    }

    @SuppressLint("Range")
    private fun getFileMeta(uri: Uri): Triple<String, Int?,String?>? {
        return uri.let { uri ->
            var result: Triple<String, Int?,String?>? = null
            if (uri.scheme == "content") {
                val cursor = contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    val size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE)).getFileSize()
                    val ext = cursor.getColumnString(MediaStore.Files.FileColumns.MIME_TYPE)
                    result = Triple(name ?: "file", size,ext)
                    cursor.close()
                }
            }
            if (result == null) {
                val path = uri.path
                val cut = path?.lastIndexOf('/')
                if (cut != -1) {
                    val name = path?.substring(cut?.plus(1) ?: 0)
                    result = Triple(name ?: "file", null,null)
                }
            }
            result
        }
    }

    fun Cursor.getColumnString(mediaColumn: String): String =
        getString(getColumnIndexOrThrow(mediaColumn)) ?: ""

    private fun Long?.getFileSize(): Int? {
        return this?.div(1000)?.toInt()
    }
}
