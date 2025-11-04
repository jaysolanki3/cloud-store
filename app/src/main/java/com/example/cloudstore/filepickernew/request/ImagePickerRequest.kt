package com.example.filepickerdemo.request

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.example.filepickerdemo.decoder.Decoder
import com.example.filepickerdemo.result.ImageMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ImagePickerRequest(
    private val decoder: Decoder,
    private val onImagePicked: (ImageMeta?) -> Unit
) : PickerRequest {
    override val intent: Intent
        get() = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

    override suspend fun invokeCallback(uri: Uri) {
        var result: ImageMeta? = null
        decoder.getStorageImage(uri).collect { result = it }
        withContext(Dispatchers.Main) {
            onImagePicked(result)
        }
    }
}