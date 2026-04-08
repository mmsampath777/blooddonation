package com.example.blooddonation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class EmergencyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.blooddonation.EMERGENCY_ALERT") {
            val message = intent.getStringExtra("message") ?: "Emergency alert received!"
            Toast.makeText(context, "BROADCAST: $message", Toast.LENGTH_LONG).show()
        }
    }
}
