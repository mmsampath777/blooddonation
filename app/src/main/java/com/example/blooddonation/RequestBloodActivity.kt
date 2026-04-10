package com.example.blooddonation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.blooddonation.databinding.ActivityRequestBloodBinding
import com.example.blooddonation.databinding.DialogSuccessBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.*

class RequestBloodActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRequestBloodBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            fetchLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestBloodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupToolbar()
        setupBloodGroupDropdown()
        checkLocationPermissions()

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

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        updateLocationField(latitude, longitude)
                    }
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun updateLocationField(lat: Double, lon: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0].getAddressLine(0)
                binding.etReqLocation.setText(address)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun submitRequest() {
        val blood = binding.actvReqBloodGroup.text.toString().trim()
        val units = binding.etReqUnits.text.toString().trim()
        val hospital = binding.etReqHospital.text.toString().trim()
        val location = binding.etReqLocation.text.toString().trim()
        val email = session.getUserEmail() ?: ""
        val user = dbHelper.getUserData(email)
        val phone = user?.phone ?: ""
        val isEmergency = binding.switchEmergency.isChecked

        if (blood.isNotEmpty() && units.isNotEmpty() && hospital.isNotEmpty() && location.isNotEmpty()) {
            val requestId = dbHelper.addRequest(blood, units, hospital, location, latitude, longitude, phone, isEmergency)
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
            finish()
        }

        dialog.show()
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
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
