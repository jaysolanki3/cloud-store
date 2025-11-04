package com.example.filepickerdemo.result

import java.io.File

data class FileMeta(
    val name: String?,
    val sizeKb: Int?,
    val file: File?,
    val filepath: String,
    val mimeType: String? = null
)