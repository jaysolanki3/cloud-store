package com.example.filepickerdemo.request

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.example.filepickerdemo.decoder.Decoder
import com.example.filepickerdemo.result.ImageMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit


internal class ImageCameraRequest(
    private val decoder: Decoder,
    private val onPhotoTaken: (ImageMeta?) -> Unit,
) : PickerRequest {

    var photoPath: String? = null
    var photoURI: Uri? = null

    override val intent
        get() = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }

    override suspend fun invokeCallback(uri: Uri) {
        var result: ImageMeta? = null
        decoder.getCameraImage(uri).collect { result = it }
        withContext(Dispatchers.Main) {
            onPhotoTaken(result)
        }
    }

    internal fun createImageFile(context: Context): File {
        val timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()
        return File.createTempFile(timeStamp, ".jpg", context.cacheDir).apply {
            photoPath = absolutePath
        }
    }

}