package com.example.smarttrash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.example.smarttrash.databinding.ActivityLoginBinding
import com.example.smarttrash.ui.SmartTrashLogo
import com.example.smarttrash.ui.theme.SmartTrashTheme
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val sharedPref = getSharedPreferences("SmartTrashPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        val userRole = sharedPref.getString("userRole", "")

        if (isLoggedIn && userRole == "janitor") {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        setContent {
            SmartTrashTheme {
                AndroidViewBinding(ActivityLoginBinding::inflate) {
                    val logoContainer = root.findViewById<FrameLayout>(R.id.logoContainer)
                    if (logoContainer.childCount == 0) {
                        logoContainer.addView(ComposeView(this@MainActivity).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setContent {
                                SmartTrashLogo(modifier = Modifier.size(80.dp))
                            }
                        })
                    }

                    loginButton.setOnClickListener {
                        val email = emailEditText.text.toString().trim()
                        val password = passwordEditText.text.toString().trim()

                        if (email.isEmpty() || password.isEmpty()) {
                            Toast.makeText(this@MainActivity, "Please enter email and password", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        loginUserCustom(email, password)
                    }
                }
            }
        }
    }

    private fun loginUserCustom(email: String, password: String) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val document = documents.documents[0]
                val dbPassword = document.getString("tempPassword")
                val role = document.getString("role")
                val name = document.getString("name")
                val userId = document.id

                if (dbPassword == password) {
                    if (role == "janitor") {
                        val sharedPref = getSharedPreferences("SmartTrashPrefs", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putBoolean("isLoggedIn", true)
                            putString("userEmail", email)
                            putString("userRole", role)
                            putString("userName", name)
                            putString("userId", userId)
                            apply()
                        }

                        logActivity(userId, email, "User logged in", "Login")

                        Toast.makeText(this, "Welcome, $name!", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Access denied. Not a janitor.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Login error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logActivity(userId: String, email: String, activityText: String, type: String) {
        val activity = mapOf(
            "activity" to activityText,
            "type" to type,
            "email" to email,
            "page" to "MainActivity",
            "timestamp" to Timestamp.now(),
            "userAgent" to "Android App (${Build.MANUFACTURER} ${Build.MODEL})",
            "userId" to userId
        )
        db.collection("user_activities").add(activity)
    }
}
