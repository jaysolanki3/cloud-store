package com.example.filepickerdemo.decoder

import android.net.Uri
import com.example.filepickerdemo.result.FileMeta
import com.example.filepickerdemo.result.ImageMeta
import kotlinx.coroutines.flow.Flow

internal interface Decoder {
    fun getStorageImage(imageUri: Uri?): Flow<ImageMeta?>
    fun getStorageFile(pdfUri: Uri?): Flow<FileMeta?>
    fun getCameraImage(imageUri: Uri?): Flow<ImageMeta?>
}