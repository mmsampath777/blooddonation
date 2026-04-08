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
        checkDonationEligibility()
        startDonorUpdateService()
    }

    override fun onResume() {
        super.onResume()
        loadDynamicStats()
        // Resume video if it was playing
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
                    startActivity(Intent(this, RequestBloodActivity::class.java))
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
        // Awareness video from drawable resources
        val videoPath = "android.resource://" + packageName + "/" + R.raw.bgvideo
        val uri = Uri.parse(videoPath)
        
        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoBanner)
        
        binding.videoBanner.setMediaController(mediaController)
        binding.videoBanner.setVideoURI(uri)
        
        binding.videoBanner.setOnPreparedListener { mp ->
            mp.isLooping = true
            // Mute video if it's just a background awareness video
            mp.setVolume(0f, 0f)
            binding.videoBanner.start()
        }
        
        binding.videoBanner.setOnErrorListener { _, _, _ ->
            // If drawable access fails (common with mp4 in drawable), try raw or show placeholder
            false
        }
    }

    private fun showEmergencyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Emergency")
            .setMessage("This will trigger a high-priority emergency alert for all users. Do you want to proceed?")
            .setPositiveButton("YES, EMERGENCY") { _, _ ->
                triggerEmergencyAlert(true)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadDynamicStats() {
        binding.tvTotalDonors.text = dbHelper.getTotalDonorsCount().toString()
        binding.tvActiveRequests.text = dbHelper.getActiveRequestsCount().toString()
        binding.tvTotalDonations.text = dbHelper.getTotalDonationsCount().toString()
    }

    private fun checkDonationEligibility() {
        // Eligibility reminder logic
    }

    private fun triggerEmergencyAlert(isGlobal: Boolean = false) {
        val intent = Intent("com.example.blooddonation.EMERGENCY_ALERT")
        intent.putExtra("message", "URGENT: Emergency Blood Request Triggered!")
        sendBroadcast(intent)
        
        if (isGlobal) {
            dbHelper.addRequest("O+", "2", "Emergency Center", "Downtown", true)
            Toast.makeText(this, "GLOBAL EMERGENCY ALERT SENT!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Testing Emergency Alert...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startDonorUpdateService() {
        startService(Intent(this, DonorUpdateService::class.java))
    }
}
