package com.example.blooddonation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.blooddonation.databinding.ActivityRegisterBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dbHelper: DatabaseHelper
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
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupBloodGroups()
        checkLocationPermissions()

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvGoToLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupBloodGroups() {
        val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bloodGroups)
        binding.actvBloodGroup.setAdapter(adapter)
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
                binding.etRegLocation.setText(address)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun registerUser() {
        val name = binding.etRegName.text.toString().trim()
        val email = binding.etRegEmail.text.toString().trim()
        val phone = binding.etRegPhone.text.toString().trim()
        val blood = binding.actvBloodGroup.text.toString().trim()
        val loc = binding.etRegLocation.text.toString().trim()
        val pass = binding.etRegPassword.text.toString().trim()

        if (name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && 
            blood.isNotEmpty() && loc.isNotEmpty() && pass.isNotEmpty()) {
            
            val userId = dbHelper.registerUser(name, email, phone, blood, loc, latitude, longitude, pass)
            if (userId != -1L) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }
}
