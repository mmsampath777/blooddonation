package com.example.blooddonation

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blooddonation.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            binding.ivProfileImage.setImageBitmap(imageBitmap)
            binding.ivProfileImage.setPadding(0, 0, 0, 0)
            Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)

        loadUserProfile()
        setupHistory()

        binding.cardProfileImage.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                takePictureLauncher.launch(takePictureIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.switchAvailability.setOnCheckedChangeListener { _, isChecked ->
            val email = session.getUserEmail()
            if (email != null) {
                dbHelper.updateUserAvailability(email, isChecked)
                val status = if (isChecked) "Available" else "Not Available"
                Toast.makeText(this, "Your status: $status", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLogout.setOnClickListener {
            session.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserProfile() {
        val email = session.getUserEmail()
        if (email != null) {
            val cursor = dbHelper.getUserData(email)
            if (cursor != null && cursor.moveToFirst()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME))
                val blood = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_BLOOD))
                val available = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_AVAILABLE))

                binding.tvProfileName.text = name
                binding.tvProfileBloodGroup.text = "Blood Group: $blood"
                binding.switchAvailability.isChecked = available == 1
                cursor.close()
            }
        }
    }

    private fun setupHistory() {
        // Dynamic donation history - for now, static list or could be from DB
        val history = listOf(
            DonationHistory("12 Oct 2023", "City General Hospital", "Completed"),
            DonationHistory("05 Jun 2023", "Red Cross Center", "Completed"),
            DonationHistory("20 Jan 2023", "St. Jude Medical", "Completed")
        )

        binding.rvDonationHistory.layoutManager = LinearLayoutManager(this)
        binding.rvDonationHistory.adapter = HistoryAdapter(history)
    }
}
