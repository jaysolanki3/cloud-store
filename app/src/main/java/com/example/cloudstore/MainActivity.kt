package com.example.cloudstore

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.utils.ObjectUtils
import com.example.cloudstore.DataModels.DocumentData
import com.example.cloudstore.databinding.ActivityMainBinding
import com.fermax.filepickernew.decoder.FileDecoder
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.util.UUID


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private var filePath = ""
    lateinit var pref : SharedPreferences
    lateinit var reference: DatabaseReference
    private var isPublic = true
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        pref = applicationContext.getSharedPreferences("CLOUD_STORE",Context.MODE_PRIVATE)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        reference = FirebaseDatabase.getInstance().getReference("Documents")
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading file...")  // Message on ProgressDialog
        progressDialog.setCancelable(false)

        binding.number.text = pref.getString("User_id","User")

        binding.selectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            if(SDK_INT >= Build.VERSION_CODES.R) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("*/*")
            startActivityForResult(intent, 1001)
        }
        initCloudinary()

        binding.uploadFile.setOnClickListener {
            if (filePath.isNotEmpty()) {
                // Upload file to Cloudinary
                uploadFileToCloudinary(filePath)
            } else {
                Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.logOut.setOnClickListener {
            pref.edit { clear() }
            startActivity(Intent(
                this@MainActivity,
                LoginActivity::class.java
            ))
            finish()
        }

        binding.radioGroupVisibility.check(binding.radioPublic.id)
        binding.radioGroupVisibility.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.radioPublic -> {
                    isPublic = true
                }
                R.id.radioPrivate -> {
                    isPublic = false
                }
            }
        }

        binding.fetchUploadedFile.setOnClickListener {
            startActivity(Intent(
                this@MainActivity,
                DocumentListActivity::class.java
            ))
        }
    }

    private fun initCloudinary() {
        try {
            val config= HashMap<Any?, Any?>()
            config.put("cloud_name", "dhwqzudyw")
            MediaManager.init(this, config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun uploadFileToCloudinary(filePath: String) {
        val file = File(filePath)

        if (file.exists()) {
            progressDialog.show()
            val options = ObjectUtils.asMap(
                "resource_type", "auto",  // Automatically detect file type (image, video, document, etc.)
                "public_id", "file_${System.currentTimeMillis()}"  // Optionally set a unique public ID for the file
            )
            MediaManager.get().upload(filePath)
                .options(options).unsigned("ml_default")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("Cloudinary", "Upload started: $requestId")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes * 100) / totalBytes
                        Log.d("Cloudinary", "Upload progress: $progress%")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val fileUrl = resultData["secure_url"] as String?
                        Log.d("Cloudinary", "File uploaded successfully. URL: $fileUrl")

                        val documentData = DocumentData(generateRandomUserId(10),pref.getString("User_id","")?:"",fileUrl?:"",if(isPublic) {"1"} else{ "0"})
                        uploadDocument(documentData)
                        binding.fileLocation.text = "File uploaded successfully"  // Display the URL in a TextView
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        progressDialog.dismiss()
                        Log.e("Cloudinary", "Error occurred during file upload: ${error.toString()}")
                        Toast.makeText(this@MainActivity, "Upload failed: $error", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w("Cloudinary", "Upload rescheduled: $error")
                    }
                })
                .dispatch()
        } else {
            Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadDocument(documentData: DocumentData) {
        reference.child(documentData.uid).setValue(documentData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "File uploaded successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "" + task.exception!!.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun generateRandomUserId(length: Int): String {
        // Generate a UUID and take the first 'length' characters
        val randomUUID = UUID.randomUUID().toString().replace("-", "")
        return randomUUID.take(length)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK){
            when(requestCode){
                1001 -> {
                    val fileDecoder = FileDecoder(this, contentResolver)
                    val imageData = data?.data?.let { fileDecoder.decodeFile(it) }
                    if (imageData!=null) {
                        filePath = imageData.filepath
                        binding.fileLocation.text = imageData.name?:"File selected"
                    }
                }
            }
        }
    }
}