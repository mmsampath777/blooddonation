package com.example.blooddonation

data class NotificationItem(
    val title: String,
    val message: String,
    val time: String,
    val isEmergency: Boolean = false
)
