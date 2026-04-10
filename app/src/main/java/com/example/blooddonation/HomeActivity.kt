package com.example.blooddonation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.blooddonation.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)

        if (!session.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupUI()
        loadDynamicStats()
        setupVideoBanner()
        startDonorUpdateService()
    }

    override fun onResume() {
        super.onResume()
        loadDynamicStats()
        binding.videoBanner.start()
    }

    private fun setupUI() {
        binding.toolbar.title = "LifeLink"
        
        val userName = session.getUserName()
        binding.tvGreeting.text = "Hello, $userName!"

        binding.cardFindDonors.setOnClickListener {
            startActivity(Intent(this, DonorListActivity::class.java))
        }

        binding.cardRequestBlood.setOnClickListener {
            startActivity(Intent(this, RequestBloodActivity::class.java))
        }

        binding.cardHospitals.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        binding.cardEmergency.setOnClickListener {
            showEmergencyDialog()
        }

        binding.btnEmergencySound.setOnClickListener {
            triggerEmergencyAlert()
        }

        binding.fabQuickRequest.setOnClickListener {
            startActivity(Intent(this, RequestBloodActivity::class.java))
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_donors -> {
                    startActivity(Intent(this, DonorListActivity::class.java))
                    false
                }
                R.id.nav_requests -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun setupVideoBanner() {
        val videoPath = "android.resource://" + packageName + "/" + R.raw.bgvideo
        val uri = Uri.parse(videoPath)
        
        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoBanner)
        
        binding.videoBanner.setMediaController(mediaController)
        binding.videoBanner.setVideoURI(uri)
        
        binding.videoBanner.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.setVolume(0f, 0f)
            binding.videoBanner.start()
        }
    }

    private fun showEmergencyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Emergency")
            .setMessage("This will trigger an emergency request locally. Proceed?")
            .setPositiveButton("YES, EMERGENCY") { _, _ ->
                triggerEmergencyAlert(true)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadDynamicStats() {
        val stats = dbHelper.getStats()
        binding.tvTotalDonors.text = stats.first.toString()
        binding.tvActiveRequests.text = stats.second.toString()
        binding.tvTotalDonations.text = stats.third.toString()
    }

    private fun triggerEmergencyAlert(isGlobal: Boolean = false) {
        if (isGlobal) {
            val email = session.getUserEmail() ?: ""
            val user = dbHelper.getUserData(email)
            val phone = user?.phone ?: ""
            
            // Fixed parameters: (blood, units, hosp, loc, lat, lon, phone, em)
            dbHelper.addRequest("O+", "2", "Emergency Center", "Downtown", 0.0, 0.0, phone, true)
            
            loadDynamicStats()
            Toast.makeText(this, "Emergency Alert Created!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Testing Emergency Alert...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startDonorUpdateService() {
        startService(Intent(this, DonorUpdateService::class.java))
    }
}
