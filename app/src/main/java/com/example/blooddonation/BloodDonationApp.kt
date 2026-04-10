package com.example.blooddonation

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class BloodDonationApp : Application() {

    companion object {
        const val CHANNEL_ID = "blood_request_service_channel"
        const val EMERGENCY_CHANNEL_ID = "emergency_alert_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Blood Request Service",
                NotificationManager.IMPORTANCE_LOW
            )
            
            val emergencyChannel = NotificationChannel(
                EMERGENCY_CHANNEL_ID,
                "Emergency Blood Requests",
                NotificationManager.IMPORTANCE_HIGH
            )
            emergencyChannel.description = "Urgent notifications for new blood requests"

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
            manager?.createNotificationChannel(emergencyChannel)
        }
    }
}
