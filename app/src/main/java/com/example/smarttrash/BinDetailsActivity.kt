package com.example.smarttrash

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BinDetailsActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore
    private var binId: String? = null
    private var binName: String = "Unknown Bin"

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
                        binName = it.name
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
                    val sharedPref = getSharedPreferences("SmartTrashPrefs", MODE_PRIVATE)
                    val userId = sharedPref.getString("userId", "") ?: ""
                    val email = sharedPref.getString("userEmail", "") ?: ""
                    
                    // Log general update activity
                    logActivity(userId, email, "Updated levels for bin: $binName", "Update Bin")
                    
                    // Add special alert/activity if critical or warning
                    if (status != "normal") {
                        val alertMsg = "Bin '$binName' reached $status level ($dry% Dry, $wet% Wet). Needs collection!"
                        logActivity(userId, email, alertMsg, "Alert")
                        createAlert(id, "Critical Bin Level", alertMsg)
                    }

                    Toast.makeText(this, "Levels updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun collectTrash() {
        binId?.let { id ->
            val sharedPref = getSharedPreferences("SmartTrashPrefs", MODE_PRIVATE)
            val userId = sharedPref.getString("userId", "") ?: ""
            val email = sharedPref.getString("userEmail", "") ?: ""
            val userName = sharedPref.getString("userName", "") ?: "User"

            if (userId.isEmpty()) {
                Toast.makeText(this, "User session error. Please re-login.", Toast.LENGTH_SHORT).show()
                return@let
            }

            val sdf = SimpleDateFormat("M/d/yyyy, h:mm:ss a", Locale.getDefault())
            val currentTime = sdf.format(Date())

            val binUpdates = mapOf(
                "dryLevel" to 0,
                "wetLevel" to 0,
                "status" to "normal",
                "lastCollected" to currentTime,
                "updatedAt" to Timestamp.now()
            )

            db.collection("bins").document(id).update(binUpdates)
                .addOnSuccessListener {
                    awardPoints(userId, email, userName)
                    logActivity(userId, email, "Collected trash from bin: $binName", "Collect Trash")
                    Toast.makeText(this, "Trash collected! +10 Points", Toast.LENGTH_SHORT).show()
                    fetchBinDetails()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Collection failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun awardPoints(userId: String, email: String, name: String) {
        val pointsRef = db.collection("points").document(userId)
        pointsRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                pointsRef.update("totalPoints", FieldValue.increment(10), "updatedAt", Timestamp.now())
            } else {
                val newPoints = mapOf(
                    "userId" to userId,
                    "email" to email,
                    "name" to name,
                    "totalPoints" to 10,
                    "createdAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now()
                )
                pointsRef.set(newPoints)
            }
        }
    }

    private fun logActivity(userId: String, email: String, activityText: String, type: String) {
        val activity = mapOf(
            "activity" to activityText,
            "type" to type,
            "email" to email,
            "page" to "BinDetailsActivity",
            "timestamp" to Timestamp.now(),
            "userAgent" to "Android App (${Build.MANUFACTURER} ${Build.MODEL})",
            "userId" to userId
        )
        db.collection("user_activities").add(activity)
    }

    private fun createAlert(binId: String, title: String, message: String) {
        val alert = mapOf(
            "binId" to binId,
            "title" to title,
            "message" to message,
            "timestamp" to Timestamp.now(),
            "isRead" to false
        )
        db.collection("alerts").add(alert)
    }
}
