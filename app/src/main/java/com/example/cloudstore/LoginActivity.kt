package com.example.cloudstore

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cloudstore.DataModels.User
import com.example.cloudstore.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    lateinit var reference: DatabaseReference
    lateinit var pref : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        pref = applicationContext.getSharedPreferences("CLOUD_STORE",Context.MODE_PRIVATE)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        reference = FirebaseDatabase.getInstance().getReference("Users")
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                clearFocusAndHideKeyboard()
            }
            false
        }

        if(pref.getBoolean("LOGGED_IN",false)){
            startActivity(
                Intent(
                    this@LoginActivity,
                    MainActivity::class.java
                )
            )
            finish()
        }

        binding.signup.setOnClickListener {
            checkIfPhoneExists(binding.textno.text.toString(),binding.textpass.text.toString())
        }
    }

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun generateRandomUserId(length: Int): String {
        // Generate a UUID and take the first 'length' characters
        val randomUUID = UUID.randomUUID().toString().replace("-", "")
        return randomUUID.take(length)
    }

    private fun clearFocusAndHideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            view.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun checkIfPhoneExists(phoneNo: String, enteredPassword: String) {
        reference.orderByChild("phoneNo").equalTo(phoneNo)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(User::class.java)
                            user?.let {
                                val storedHashPassword = it.password
                                if (verifyPassword(enteredPassword, storedHashPassword)) {
                                    Toast.makeText(applicationContext, "Sign in Successfully", Toast.LENGTH_SHORT).show()
                                    pref.edit { putBoolean("LOGGED_IN", true) }
                                    pref.edit { putString("User_id", phoneNo) }
                                    pref.edit { putString("uid", user.uid) }
                                    startActivity(
                                        Intent(
                                            this@LoginActivity,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()
                                } else {
                                    Toast.makeText(applicationContext, "Invalid Password.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        val user = User(uid = generateRandomUserId(10), phoneNo = binding.textno.text.toString(), password = hashPassword(binding.textpass.text.toString()))
                        reference.child(user.uid).setValue(user).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(applicationContext, "Sign in Successfully", Toast.LENGTH_SHORT).show()
                                pref.edit { putBoolean("LOGGED_IN", true) }
                                pref.edit { putString("User_id", phoneNo) }
                                pref.edit { putString("uid", user.uid) }
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        MainActivity::class.java
                                    )
                                )
                                finish()
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "" + task.exception!!.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun verifyPassword(enteredPassword: String, storedHash: String): Boolean {
        return BCrypt.checkpw(enteredPassword, storedHash)
    }
}