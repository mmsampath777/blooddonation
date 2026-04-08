package com.example.blooddonation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blooddonation.databinding.ActivityDonorListBinding

class DonorListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDonorListBinding
    private lateinit var dbHelper: DatabaseHelper
    private var donorList = mutableListOf<Donor>()
    private lateinit var adapter: DonorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        
        setupToolbar()
        setupRecyclerView()
        loadDonors()
        setupSearch()
    }

    private fun setupToolbar() {
        binding.toolbarDonors.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = DonorAdapter(donorList)
        binding.rvDonors.layoutManager = LinearLayoutManager(this)
        binding.rvDonors.adapter = adapter
    }

    private fun loadDonors(filter: String? = null) {
        donorList.clear()
        // Updated to use the correct method name from DatabaseHelper
        val cursor = dbHelper.getDonorsFiltered(filter)
        
        var isFirst = true
        if (cursor.moveToFirst()) {
            do {
                val donationCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DONOR_DONATION_COUNT))
                
                donorList.add(Donor(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DONOR_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DONOR_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DONOR_BLOOD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DONOR_PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DONOR_LOCATION)),
                    donationCount,
                    // Smart Matching: Mark the top donor with high experience as Best Match
                    isFirst && donationCount > 0
                ))
                isFirst = false
            } while (cursor.moveToNext())
        }
        cursor.close()

        updateUIState()
    }

    private fun setupSearch() {
        binding.etSearchDonor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.length <= 3 && query.isNotEmpty()) {
                    loadDonors(query)
                } else if (query.isEmpty()) {
                    loadDonors()
                } else {
                    filterListManually(query)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        binding.btnVoiceSearch.setOnClickListener {
            android.widget.Toast.makeText(this, "Voice Search triggered...", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterListManually(query: String) {
        val filtered = donorList.filter { 
            it.name.contains(query, ignoreCase = true) || it.location.contains(query, ignoreCase = true) 
        }
        adapter.updateData(filtered)
        updateUIState(filtered.isEmpty())
    }

    private fun updateUIState(isEmptyOverride: Boolean? = null) {
        val isEmpty = isEmptyOverride ?: donorList.isEmpty()
        if (isEmpty) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvDonors.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvDonors.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()
    }
}
