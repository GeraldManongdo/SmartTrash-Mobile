package com.example.smarttrash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class AlertsActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var alertsAdapter: AlertsAdapter
    private val alertsList = mutableListOf<Alert>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerts)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        fetchAlerts()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.alertsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        alertsAdapter = AlertsAdapter(alertsList) { alert ->
            val intent = Intent(this, BinDetailsActivity::class.java)
            intent.putExtra("BIN_ID", alert.binId)
            startActivity(intent)
        }
        recyclerView.adapter = alertsAdapter
    }

    private fun fetchAlerts() {
        db.collection("alerts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    alertsList.clear()
                    for (doc in snapshots) {
                        val alert = doc.toObject(Alert::class.java)
                        alert.id = doc.id
                        alertsList.add(alert)
                    }
                    alertsAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_alerts
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_bins -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_points -> {
                    startActivity(Intent(this, PointsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_alerts -> true
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

class AlertsAdapter(
    private val alerts: List<Alert>,
    private val onClick: (Alert) -> Unit
) : RecyclerView.Adapter<AlertsAdapter.AlertViewHolder>() {

    class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.alertTitle)
        val message: TextView = view.findViewById(R.id.alertMessage)
        val time: TextView = view.findViewById(R.id.alertTime)
        val icon: ImageView = view.findViewById(R.id.alertIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        holder.title.text = alert.title
        holder.message.text = alert.message
        
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        holder.time.text = alert.timestamp?.toDate()?.let { sdf.format(it) } ?: "Just now"

        holder.itemView.setOnClickListener { onClick(alert) }
    }

    override fun getItemCount() = alerts.size
}
