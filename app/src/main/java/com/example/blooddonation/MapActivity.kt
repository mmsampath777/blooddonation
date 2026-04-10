package com.example.blooddonation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.blooddonation.databinding.ActivityMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.*

class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager

    private var lastLat: Double = 0.0
    private var lastLon: Double = 0.0
    private var lastAddress: String = ""

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnBackMap.setOnClickListener { finish() }

        binding.btnCurrentLocation.setOnClickListener {
            checkPermissions()
        }

        binding.btnSaveLocation.setOnClickListener {
            saveLocationToSQLite()
        }

        // Initialize markers
        fetchDonorsForMap()
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        lastLat = location.latitude
                        lastLon = location.longitude
                        
                        updateLocationUI(lastLat, lastLon)
                        getAddressFromLatLng(lastLat, lastLon)
                        showUserMarker()
                    } else {
                        Toast.makeText(this, "Unable to get location. Is GPS on?", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permission missing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUserMarker() {
        binding.ivUserLocationMarker.visibility = View.VISIBLE
        val params = binding.ivUserLocationMarker.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER
        binding.ivUserLocationMarker.layoutParams = params
    }

    private fun fetchDonorsForMap() {
        val donors = dbHelper.getDonorsFiltered()
        binding.donorMarkersContainer.removeAllViews()
        val random = Random()
        
        for (donor in donors) {
            // Since we don't have a real map SDK, we simulate markers around the center
            val marker = ImageView(this).apply {
                setImageResource(R.drawable.ic_blood_drop)
                layoutParams = FrameLayout.LayoutParams(60, 60).apply {
                    gravity = Gravity.CENTER
                    // Random offset to simulate different locations
                    leftMargin = random.nextInt(600) - 300
                    topMargin = random.nextInt(800) - 400
                }
                setOnClickListener {
                    Toast.makeText(context, "Donor: ${donor.name}\nGroup: ${donor.bloodGroup}", Toast.LENGTH_SHORT).show()
                }
            }
            binding.donorMarkersContainer.addView(marker)
        }
    }

    private fun updateLocationUI(lat: Double, lon: Double) {
        binding.cardLocationInfo.visibility = View.VISIBLE
        binding.tvLatLong.text = "Lat: $lat, Lon: $lon"
        binding.tvLocationAddress.text = "Fetching address..."
    }

    private fun getAddressFromLatLng(lat: Double, lon: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                lastAddress = addresses[0].getAddressLine(0) ?: "Unknown address"
                binding.tvLocationAddress.text = lastAddress
            }
        } catch (e: Exception) {
            binding.tvLocationAddress.text = "Error getting address"
        }
    }

    private fun saveLocationToSQLite() {
        val email = session.getUserEmail()
        if (email != null && lastLat != 0.0) {
            dbHelper.updateProfileLocation(email, lastAddress, lastLat, lastLon)
            Toast.makeText(this, "Location saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
