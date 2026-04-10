package com.example.blooddonation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.blooddonation.databinding.ActivityProfileBinding
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager
    private lateinit var adapter: HistoryAdapter

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to take a profile picture", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            saveImageLocally(imageBitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)

        setupHistoryRecyclerView()
        loadUserProfile()

        binding.cardProfileImage.setOnClickListener {
            checkCameraPermission()
        }

        binding.switchAvailability.setOnCheckedChangeListener { _, isChecked ->
            val email = session.getUserEmail()
            if (email != null) {
                dbHelper.updateUserAvailability(email, isChecked)
                Toast.makeText(this, "Status: ${if (isChecked) "Available" else "Busy"}", Toast.LENGTH_SHORT).show()
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

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            takePictureLauncher.launch(takePictureIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Camera app not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserProfile() {
        val email = session.getUserEmail() ?: return
        val user = dbHelper.getUserData(email)
        if (user != null) {
            binding.tvProfileName.text = user.name
            binding.tvProfileBloodGroup.text = "Blood Group: ${user.bloodGroup}"
            binding.tvDonationCount.text = user.donationCount.toString()
            binding.tvLastDonationDate.text = user.lastDonationDate.ifEmpty { "No donations yet" }
            binding.switchAvailability.isChecked = user.available

            if (user.profileImageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(File(user.profileImageUrl))
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.ivProfileImage)
                binding.ivProfileImage.setPadding(0, 0, 0, 0)
            }

            loadDonationHistory(email)
        }
    }

    private fun setupHistoryRecyclerView() {
        adapter = HistoryAdapter(mutableListOf())
        binding.rvDonationHistory.layoutManager = LinearLayoutManager(this)
        binding.rvDonationHistory.adapter = adapter
    }

    private fun loadDonationHistory(email: String) {
        val history = dbHelper.getDonationHistory(email)
        adapter.updateData(history)
        if (history.isEmpty()) {
            binding.emptyHistoryText.visibility = View.VISIBLE
            binding.rvDonationHistory.visibility = View.GONE
        } else {
            binding.emptyHistoryText.visibility = View.GONE
            binding.rvDonationHistory.visibility = View.VISIBLE
        }
    }

    private fun saveImageLocally(bitmap: Bitmap) {
        val email = session.getUserEmail() ?: return
        val file = File(filesDir, "${email.replace(".", "_")}.jpg")
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            
            val imagePath = file.absolutePath
            dbHelper.updateProfileImage(email, imagePath)
            
            Glide.with(this).load(file).circleCrop().into(binding.ivProfileImage)
            binding.ivProfileImage.setPadding(0, 0, 0, 0)
            Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
        }
    }
}
