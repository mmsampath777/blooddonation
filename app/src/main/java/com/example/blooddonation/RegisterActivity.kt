package com.example.blooddonation

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.blooddonation.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bloodGroups)
        binding.actvBloodGroup.setAdapter(adapter)

        binding.btnRegister.setOnClickListener {
            val name = binding.etRegName.text.toString().trim()
            val email = binding.etRegEmail.text.toString().trim()
            val phone = binding.etRegPhone.text.toString().trim()
            val blood = binding.actvBloodGroup.text.toString().trim()
            val loc = binding.etRegLocation.text.toString().trim()
            val pass = binding.etRegPassword.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && 
                blood.isNotEmpty() && loc.isNotEmpty() && pass.isNotEmpty()) {
                
                val userId = dbHelper.registerUser(name, email, phone, blood, loc, pass)
                if (userId != -1L) {
                    Toast.makeText(this, "Registration successful! You are also listed as a donor.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Registration failed. Email might already exist.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvGoToLogin.setOnClickListener {
            finish()
        }
    }
}
