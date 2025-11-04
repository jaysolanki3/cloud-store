package com.example.filepickerdemo.request

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.example.filepickerdemo.decoder.Decoder
import com.example.filepickerdemo.result.FileMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class FilePickerRequest(
    private val decoder: Decoder,
    private val onFilePicked: (FileMeta?) -> Unit,
    private val initialDirectoryPath: String?=null
) : PickerRequest {

    private val fileType = "application/*"

    override val intent: Intent
        get() = Intent(Intent.ACTION_GET_CONTENT).apply {
            if (initialDirectoryPath.isNullOrBlank())
                type = fileType
            else setDataAndType(initialDirectoryPath.toUri(), fileType)
        }

    override suspend fun invokeCallback(uri: Uri) {
        var result: FileMeta? = null
        decoder.getStorageFile(uri).collect { result = it }
        withContext(Dispatchers.Main) {
            onFilePicked(result)
        }
    }
}