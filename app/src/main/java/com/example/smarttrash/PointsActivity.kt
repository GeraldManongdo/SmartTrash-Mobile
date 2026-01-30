package com.example.smarttrash

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PointsActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var totalPointsText: TextView

    // Leaderboard UI elements
    private lateinit var rank1Name: TextView
    private lateinit var rank1Points: TextView
    private lateinit var rank2Name: TextView
    private lateinit var rank2Points: TextView
    private lateinit var rank3Name: TextView
    private lateinit var rank3Points: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_points)

        db = FirebaseFirestore.getInstance()
        
        totalPointsText = findViewById(R.id.totalPointsText)
        rank1Name = findViewById(R.id.rank1Name)
        rank1Points = findViewById(R.id.rank1Points)
        rank2Name = findViewById(R.id.rank2Name)
        rank2Points = findViewById(R.id.rank2Points)
        rank3Name = findViewById(R.id.rank3Name)
        rank3Points = findViewById(R.id.rank3Points)

        fetchUserPoints()
        fetchLeaderboard()
        setupBottomNavigation()
    }

    private fun fetchUserPoints() {
        val sharedPref = getSharedPreferences("SmartTrashPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("userId", "") ?: ""

        if (userId.isEmpty()) return

        db.collection("points").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val points = document.getLong("totalPoints") ?: 0
                    totalPointsText.text = points.toString()
                }
            }
    }

    private fun fetchLeaderboard() {
        db.collection("points")
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { documents ->
                val ranks = documents.documents
                
                if (ranks.size >= 1) {
                    rank1Name.text = ranks[0].getString("name") ?: "N/A"
                    rank1Points.text = "${ranks[0].getLong("totalPoints") ?: 0} pts"
                }
                
                if (ranks.size >= 2) {
                    rank2Name.text = ranks[1].getString("name") ?: "N/A"
                    rank2Points.text = "${ranks[1].getLong("totalPoints") ?: 0} pts"
                    findViewById<View>(R.id.rank2Card).visibility = View.VISIBLE
                } else {
                    findViewById<View>(R.id.rank2Card).visibility = View.GONE
                }
                
                if (ranks.size >= 3) {
                    rank3Name.text = ranks[2].getString("name") ?: "N/A"
                    rank3Points.text = "${ranks[2].getLong("totalPoints") ?: 0} pts"
                    findViewById<View>(R.id.rank3Card).visibility = View.VISIBLE
                } else {
                    findViewById<View>(R.id.rank3Card).visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Leaderboard error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_points
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_bins -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_alerts -> {
                    startActivity(Intent(this, AlertsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_points -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
