package com.example.smarttrash

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var binAdapter: BinAdapter
    private val binsList = mutableListOf<Bin>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        fetchBins()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.binsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        binAdapter = BinAdapter(binsList) { bin ->
            val intent = Intent(this, BinDetailsActivity::class.java)
            intent.putExtra("BIN_ID", bin.id)
            startActivity(intent)
        }
        recyclerView.adapter = binAdapter
    }

    private fun fetchBins() {
        db.collection("bins").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Toast.makeText(this, "Listen failed: ${e.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshots != null) {
                binsList.clear()
                var criticalCount = 0
                var warningCount = 0

                for (doc in snapshots) {
                    val bin = doc.toObject(Bin::class.java)
                    bin.id = doc.id
                    binsList.add(bin)

                    when (bin.status.lowercase()) {
                        "critical" -> criticalCount++
                        "warning" -> warningCount++
                    }
                }
                
                binAdapter.updateBins(binsList)
                updateSummaryUI(binsList.size, criticalCount, warningCount)
            }
        }
    }

    private fun updateSummaryUI(total: Int, critical: Int, warning: Int) {
        findViewById<TextView>(R.id.binCountSummary).text = "$total bins assigned to you"
        findViewById<TextView>(R.id.totalBinsText).text = total.toString()
        findViewById<TextView>(R.id.criticalBinsText).text = critical.toString()
        findViewById<TextView>(R.id.warningBinsText).text = warning.toString()

        val priorityCard = findViewById<View>(R.id.priorityCard)
        if (critical > 0 || warning > 0) {
            priorityCard.visibility = View.VISIBLE
            findViewById<TextView>(R.id.prioritySubtext).text = 
                "$critical critical bin needs immediate attention. $warning bin is at warning level."
        } else {
            priorityCard.visibility = View.GONE
        }
    }
}
