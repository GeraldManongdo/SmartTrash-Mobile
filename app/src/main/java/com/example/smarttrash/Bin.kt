package com.example.smarttrash

import com.google.firebase.Timestamp

data class Bin(
    var id: String = "",
    val name: String = "",
    val location: String = "",
    val dryLevel: Int = 0,
    val wetLevel: Int = 0,
    val status: String = "normal",
    val lastCollected: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
