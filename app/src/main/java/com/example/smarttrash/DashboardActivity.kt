package com.example.smarttrash

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarttrash.databinding.ActivityDashboardBinding
import com.example.smarttrash.ui.SmartTrashLogo
import com.example.smarttrash.ui.theme.SmartTrashTheme
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var binAdapter: BinAdapter
    private val binsList = mutableListOf<Bin>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()

        setContent {
            SmartTrashTheme {
                AndroidViewBinding(ActivityDashboardBinding::inflate) {
                    val logoContainer = root.findViewById<FrameLayout>(R.id.logoContainer)
                    if (logoContainer.childCount == 0) {
                        logoContainer.addView(ComposeView(this@DashboardActivity).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setContent {
                                SmartTrashLogo(modifier = Modifier.size(40.dp))
                            }
                        })
                    }

                    setupRecyclerView(binsRecyclerView)
                    setupBottomNavigation(bottomNavigation)
                    fetchBins(binCountSummary, totalBinsText, criticalBinsText, warningBinsText, priorityCard, prioritySubtext)
                }
            }
        }
    }

    private fun setupRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        binAdapter = BinAdapter(binsList) { bin ->
            val intent = Intent(this, BinDetailsActivity::class.java)
            intent.putExtra("BIN_ID", bin.id)
            startActivity(intent)
        }
        recyclerView.adapter = binAdapter
    }

    private fun setupBottomNavigation(bottomNav: BottomNavigationView) {
        bottomNav.selectedItemId = R.id.nav_bins
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_bins -> true
                R.id.nav_alerts -> {
                    startActivity(Intent(this, AlertsActivity::class.java))
                    true
                }
                R.id.nav_points -> {
                    startActivity(Intent(this, PointsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchBins(
        binCountSummary: TextView,
        totalBinsText: TextView,
        criticalBinsText: TextView,
        warningBinsText: TextView,
        priorityCard: View,
        prioritySubtext: TextView
    ) {
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
                updateSummaryUI(
                    binsList.size, criticalCount, warningCount,
                    binCountSummary, totalBinsText, criticalBinsText, warningBinsText, priorityCard, prioritySubtext
                )
            }
        }
    }

    private fun updateSummaryUI(
        total: Int, critical: Int, warning: Int,
        binCountSummary: TextView,
        totalBinsText: TextView,
        criticalBinsText: TextView,
        warningBinsText: TextView,
        priorityCard: View,
        prioritySubtext: TextView
    ) {
        binCountSummary.text = "$total bins assigned to you"
        totalBinsText.text = total.toString()
        criticalBinsText.text = critical.toString()
        warningBinsText.text = warning.toString()

        if (critical > 0 || warning > 0) {
            priorityCard.visibility = View.VISIBLE
            prioritySubtext.text = "$critical critical bin needs immediate attention. $warning bin is at warning level."
        } else {
            priorityCard.visibility = View.GONE
        }
    }
}
