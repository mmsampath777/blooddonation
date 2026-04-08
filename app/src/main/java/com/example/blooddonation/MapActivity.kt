package com.example.blooddonation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.blooddonation.databinding.ActivityMapBinding

class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCurrentLocation.setOnClickListener {
            // Placeholder for location logic
        }

        // Mock search logic
        binding.etSearchMap.setOnEditorActionListener { _, _, _ ->
            true
        }
    }
}
