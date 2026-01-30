package com.example.smarttrash

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Check if user is already logged in
        if (auth.currentUser != null) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        fetchUserData(it.uid)
                    }
                } else {
                    Toast.makeText(
                        baseContext,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun fetchUserData(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name")
                    val role = document.getString("role")
                    
                    if (role == "janitor") {
                        Toast.makeText(this, "Welcome, $name!", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Access denied. Not a janitor.", Toast.LENGTH_SHORT).show()
                        auth.signOut()
                    }
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
