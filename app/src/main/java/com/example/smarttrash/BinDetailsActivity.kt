package com.example.smarttrash

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BinDetailsActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore
    private var binId: String? = null
    
    private lateinit var dryProgress: ProgressBar
    private lateinit var wetProgress: ProgressBar
    private lateinit var dryPercent: TextView
    private lateinit var wetPercent: TextView
    private lateinit var drySeekBar: SeekBar
    private lateinit var wetSeekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bin_details)

        db = FirebaseFirestore.getInstance()
        binId = intent.getStringExtra("BIN_ID")

        dryProgress = findViewById(R.id.detailDryProgress)
        wetProgress = findViewById(R.id.detailWetProgress)
        dryPercent = findViewById(R.id.dryPercent)
        wetPercent = findViewById(R.id.wetPercent)
        drySeekBar = findViewById(R.id.drySeekBar)
        wetSeekBar = findViewById(R.id.wetSeekBar)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }

        setupSeekBars()
        fetchBinDetails()

        findViewById<Button>(R.id.updateLevelsButton).setOnClickListener {
            updateBinLevels(drySeekBar.progress, wetSeekBar.progress)
        }

        findViewById<Button>(R.id.collectButton).setOnClickListener {
            collectTrash()
        }
    }

    private fun setupSeekBars() {
        drySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                dryPercent.text = "$progress%"
                dryProgress.progress = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        wetSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                wetPercent.text = "$progress%"
                wetProgress.progress = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun fetchBinDetails() {
        binId?.let { id ->
            db.collection("bins").document(id).get()
                .addOnSuccessListener { doc ->
                    val bin = doc.toObject(Bin::class.java)
                    bin?.let {
                        findViewById<TextView>(R.id.detailBinName).text = it.name
                        findViewById<TextView>(R.id.detailBinLocation).text = "Location: ${it.location}"
                        findViewById<TextView>(R.id.detailLastCollected).text = "Last Collected: ${it.lastCollected}"
                        
                        dryProgress.progress = it.dryLevel
                        wetProgress.progress = it.wetLevel
                        dryPercent.text = "${it.dryLevel}%"
                        wetPercent.text = "${it.wetLevel}%"
                        drySeekBar.progress = it.dryLevel
                        wetSeekBar.progress = it.wetLevel
                    }
                }
        }
    }

    private fun updateBinLevels(dry: Int, wet: Int) {
        binId?.let { id ->
            val status = when {
                dry >= 90 || wet >= 90 -> "critical"
                dry >= 70 || wet >= 70 -> "warning"
                else -> "normal"
            }

            val updates = mapOf(
                "dryLevel" to dry,
                "wetLevel" to wet,
                "status" to status,
                "updatedAt" to Timestamp.now()
            )

            db.collection("bins").document(id).update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Levels updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun collectTrash() {
        binId?.let { id ->
            val sdf = SimpleDateFormat("M/d/yyyy, h:mm:ss a", Locale.getDefault())
            val currentTime = sdf.format(Date())

            val updates = mapOf(
                "dryLevel" to 0,
                "wetLevel" to 0,
                "status" to "normal",
                "lastCollected" to currentTime,
                "updatedAt" to Timestamp.now()
            )

            db.collection("bins").document(id).update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Trash collected!", Toast.LENGTH_SHORT).show()
                    fetchBinDetails() // Refresh UI
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Collection failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
