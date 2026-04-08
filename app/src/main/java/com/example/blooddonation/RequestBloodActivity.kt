package com.example.blooddonation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.blooddonation.databinding.ActivityRequestBloodBinding
import com.example.blooddonation.databinding.DialogSuccessBinding

class RequestBloodActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRequestBloodBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestBloodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        setupToolbar()
        setupBloodGroupDropdown()

        binding.btnSubmitRequest.setOnClickListener {
            submitRequest()
        }
    }

    private fun setupToolbar() {
        binding.toolbarRequest.setNavigationOnClickListener { finish() }
    }

    private fun setupBloodGroupDropdown() {
        val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bloodGroups)
        binding.actvReqBloodGroup.setAdapter(adapter)
    }

    private fun submitRequest() {
        val blood = binding.actvReqBloodGroup.text.toString().trim()
        val units = binding.etReqUnits.text.toString().trim()
        val hospital = binding.etReqHospital.text.toString().trim()
        val location = binding.etReqLocation.text.toString().trim()

        if (blood.isNotEmpty() && units.isNotEmpty() && hospital.isNotEmpty() && location.isNotEmpty()) {
            val requestId = dbHelper.addRequest(blood, units, hospital, location)
            if (requestId != -1L) {
                sendNotification(blood, hospital)
                showSuccessDialog()
            } else {
                Toast.makeText(this, "Failed to submit request", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSuccessDialog() {
        val dialogBinding = DialogSuccessBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.btnDialogDone.setOnClickListener {
            dialog.dismiss()
            showInterstitialAd() // Simulate ad after submission
        }

        dialog.show()
    }

    private fun showInterstitialAd() {
        // Simple Ad simulation using another dialog or just finishing the activity
        // Here we just finish to return home as per app flow
        Toast.makeText(this, "Interstitial Ad simulated", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun sendNotification(blood: String, hospital: String) {
        val channelId = "blood_request_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Blood Requests",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_blood_drop)
            .setContentTitle("New Blood Request: $blood")
            .setContentText("Emergency request at $hospital. Can you help?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
