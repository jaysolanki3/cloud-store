package com.example.cloudstore

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cloudstore.DataModels.DocumentData
import com.example.cloudstore.DataModels.User
import com.example.cloudstore.databinding.ActivityDocumentListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DocumentListActivity : AppCompatActivity() {

    lateinit var binding : ActivityDocumentListBinding
    lateinit var reference: DatabaseReference
    lateinit var pref : SharedPreferences
    private lateinit var fileList: ArrayList<DocumentData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDocumentListBinding.inflate(layoutInflater)
        pref = applicationContext.getSharedPreferences("CLOUD_STORE",Context.MODE_PRIVATE)
        reference = FirebaseDatabase.getInstance().getReference("Documents")
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fileList = ArrayList()
        fetchData()
    }

    private fun fetchData(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                fileList.clear()
                for (fileSnapshot in dataSnapshot.children) {
                    val fileData = fileSnapshot.getValue(DocumentData::class.java)
                    if (fileData != null) {
                        if(fileData.public == "1"){
                            fileList.add(fileData)
                        } else {
                            if (fileData.uploadedBy == pref.getString("User_id","")){
                                fileList.add(fileData)
                            }
                        }
                    }
                }
                val adapter = FileAdapter(this@DocumentListActivity, fileList)
                binding.fileList.layoutManager = LinearLayoutManager(this@DocumentListActivity)
                binding.fileList.adapter = adapter
                Log.w("TAG", "loadPost:onCancelled")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        }
        reference.addValueEventListener(postListener)
    }
}