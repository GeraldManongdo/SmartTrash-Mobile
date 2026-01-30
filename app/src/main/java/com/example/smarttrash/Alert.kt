package com.example.smarttrash

import com.google.firebase.Timestamp

data class Alert(
    var id: String = "",
    val binId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp? = null,
    val isRead: Boolean = false
)
