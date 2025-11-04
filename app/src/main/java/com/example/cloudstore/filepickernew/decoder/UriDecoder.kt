package com.example.filepickerdemo.decoder


import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory.decodeFileDescriptor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.example.filepickerdemo.result.FileMeta
import com.example.filepickerdemo.result.ImageMeta
import com.example.filepickerdemo.stream.FileStreamer
import com.example.filepickerdemo.stream.Streamer
import kotlinx.coroutines.flow.flow
import java.io.*

 internal class UriDecoder(
    private val context: Context?,
    private val streamer: Streamer = FileStreamer()
) : Decoder {

    private var uri: Uri? = null
    private var contentResolver: ContentResolver? = null

    override fun getStorageImage(imageUri: Uri?) = flow {
        uri = imageUri
        contentResolver = context?.contentResolver
        decodeImage().also { emit(it) }
    }

    override fun getCameraImage(imageUri: Uri?) = flow {
        uri = imageUri
        contentResolver = context?.contentResolver
        decodeCameraImage().also { emit(it) }
    }


    override fun getStorageFile(pdfUri: Uri?) = flow {
        uri = pdfUri
        contentResolver = context?.contentResolver
        decodeFile().also { emit(it) }
    }


    private fun decodeImage(): ImageMeta? {
        var pfd: ParcelFileDescriptor? = null
        return try {
            uri?.let { uri ->
                pfd = contentResolver?.openFileDescriptor(uri, "r")
                pfd?.let { fd ->
                    val inputStream = FileInputStream(fd.fileDescriptor)
                    val bitmap = decodeFileDescriptor(fd.fileDescriptor)
                    val meta = getFileMeta() ?: Pair("file", null)
                    val imageFile = File(context?.cacheDir, meta.first)
                    val outputStream = FileOutputStream(imageFile)
                    val filepath= imageFile.absolutePath
                    streamer.copyFile(inputStream, outputStream)
                    ImageMeta(meta.first, meta.second, imageFile, bitmap,filepath)
                }
            }
        } catch (e: Exception) {
            println(e.message)
            null
        } finally {
            pfd?.close()
        }
    }

    private fun decodeCameraImage(): ImageMeta? {
        var pfd: ParcelFileDescriptor? = null
        return try {
            uri?.let { uri ->
                pfd = contentResolver?.openFileDescriptor(uri, "r")
                pfd?.let { fd ->
                    val originalBitmap = decodeFileDescriptor(fd.fileDescriptor)
                    val bitmap = BitmapRotation(context?.contentResolver, uri)
                        .run { rotateAccordingToOrientation(originalBitmap) }
                    val meta = getFileMeta() ?: Pair("file", null)
                    val imageFile = File(context?.cacheDir, meta.first)
                    val filepath=imageFile.absolutePath
                    ImageMeta(meta.first, meta.second, imageFile, bitmap,filepath)
                }
            }
        } catch (e: Exception) {
            println(e.message)
            null
        } finally {
            pfd?.close()
        }
    }


    private fun decodeFile(): FileMeta? {
        var pfd: ParcelFileDescriptor? = null
        return try {
            uri?.let { pdfUri ->
                pfd = contentResolver?.openFileDescriptor(pdfUri, "r")
                val inputStream = FileInputStream(pfd?.fileDescriptor)
                val meta = getFileMeta() ?: Pair("file", null)
                val pdfFile = File(context?.cacheDir, meta.first)
                val outputStream = FileOutputStream(pdfFile)
                val filepath = pdfFile.absolutePath
                streamer.copyFile(inputStream, outputStream)
                FileMeta(meta.first, meta.second, pdfFile,filepath)
            }
        } catch (e: Exception) {
            println(e.message)
            null
        } finally {
            pfd?.close()

        }
    }

    @SuppressLint("Range")
    private fun getFileMeta(): Pair<String, Int?>? {
        return uri?.let { uri ->
            var result: Pair<String, Int?>? = null
            if (uri.scheme == "content") {
                val cursor = contentResolver?.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    val size =
                        cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE)).getFileSize()
                    result = Pair(name ?: "file", size)
                    cursor.close()
                }
            }
            if (result == null) {
                val path = uri.path
                val cut = path?.lastIndexOf('/')
                if (cut != -1) {
                    val name = path?.substring(cut?.plus(1) ?: 0)
                    result = Pair(name ?: "file", null)
                }
            }
            result
        }
    }

}

private fun Long?.getFileSize(): Int? {
    return this?.div(1000)?.toInt()
}





