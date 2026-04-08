package com.example.blooddonation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
        startDonorUpdateService()
    }

    override fun onResume() {
        super.onResume()
        loadDynamicStats() // Refresh stats when returning to home
    }

    private fun setupUI() {
        binding.toolbar.title = "LifeLink"
        
        // Dynamic greeting
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
            triggerEmergencyAlert()
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

    private fun loadDynamicStats() {
        val totalDonors = dbHelper.getTotalDonorsCount()
        val activeRequests = dbHelper.getActiveRequestsCount()

        binding.tvTotalDonors.text = totalDonors.toString()
        binding.tvActiveRequests.text = activeRequests.toString()
    }

    private fun triggerEmergencyAlert() {
        val intent = Intent("com.example.blooddonation.EMERGENCY_ALERT")
        intent.putExtra("message", "Emergency Blood Needed in your area!")
        sendBroadcast(intent)
        
        Toast.makeText(this, "Emergency broadcast sent to nearby users!", Toast.LENGTH_LONG).show()
    }

    private fun startDonorUpdateService() {
        val serviceIntent = Intent(this, DonorUpdateService::class.java)
        startService(serviceIntent)
    }
}
