package com.example.smarttrash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = FirebaseFirestore.getInstance()
        
        val sharedPref = getSharedPreferences("SmartTrashPrefs", MODE_PRIVATE)
        userId = sharedPref.getString("userId", "") ?: ""
        val name = sharedPref.getString("userName", "Janitor")
        email = sharedPref.getString("userEmail", "") ?: ""

        findViewById<TextView>(R.id.profileNameText).text = name
        findViewById<TextView>(R.id.profileEmailText).text = email

        findViewById<Button>(R.id.changePasswordButton).setOnClickListener {
            val newPass = findViewById<EditText>(R.id.newPasswordEditText).text.toString().trim()
            if (newPass.isNotEmpty() && newPass.length >= 6) {
                updatePassword(newPass)
            } else {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            logout()
        }

        setupBottomNavigation()
    }

    private fun updatePassword(newPass: String) {
        db.collection("users").document(userId)
            .update("tempPassword", newPass)
            .addOnSuccessListener {
                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                findViewById<EditText>(R.id.newPasswordEditText).text.clear()
                logActivity(userId, email, "User changed password", "Update")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logout() {
        // Log activity before clearing session
        logActivity(userId, email, "User logged out", "Logout")

        val sharedPref = getSharedPreferences("SmartTrashPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun logActivity(userId: String, email: String, activityText: String, type: String) {
        val activity = mapOf(
            "activity" to activityText,
            "type" to type,
            "email" to email,
            "page" to "ProfileActivity",
            "timestamp" to Timestamp.now(),
            "userAgent" to "Android App (${Build.MANUFACTURER} ${Build.MODEL})",
            "userId" to userId
        )
        db.collection("user_activities").add(activity)
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            val targetActivity = when (item.itemId) {
                R.id.nav_bins -> DashboardActivity::class.java
                R.id.nav_alerts -> AlertsActivity::class.java
                R.id.nav_points -> PointsActivity::class.java
                R.id.nav_profile -> null
                else -> null
            }

            if (targetActivity != null) {
                val intent = Intent(this, targetActivity)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
                overridePendingTransition(0, 0)
                true
            } else {
                true
            }
        }
    }
}
