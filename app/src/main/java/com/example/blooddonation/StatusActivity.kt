package com.example.blooddonation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blooddonation.databinding.ActivityStatusBinding
import com.google.android.material.tabs.TabLayout

class StatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatusBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var donorAdapter: DonorAdapter
    private lateinit var requestAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        setupToolbar()
        setupRecyclerViews()
        setupTabs()

        // Default tab
        loadAvailableDonors()
    }

    private fun setupToolbar() {
        binding.toolbarStatus.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerViews() {
        binding.rvStatus.layoutManager = LinearLayoutManager(this)
        donorAdapter = DonorAdapter(mutableListOf())
        requestAdapter = NotificationAdapter(mutableListOf(), dbHelper)
    }

    private fun setupTabs() {
        binding.tabLayoutStatus.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadAvailableDonors()
                    1 -> loadBusyDonors()
                    2 -> loadAcceptedRequests()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadAvailableDonors() {
        showLoading(true)
        binding.rvStatus.adapter = donorAdapter
        val donors = dbHelper.getDonorsFiltered(onlyAvailable = true)
        donorAdapter.updateData(donors)
        updateUI(donors.isEmpty())
    }

    private fun loadBusyDonors() {
        showLoading(true)
        binding.rvStatus.adapter = donorAdapter
        val allDonors = dbHelper.getDonorsFiltered(onlyAvailable = false)
        val busyDonors = allDonors.filter { !it.available }
        donorAdapter.updateData(busyDonors)
        updateUI(busyDonors.isEmpty())
    }

    private fun loadAcceptedRequests() {
        showLoading(true)
        binding.rvStatus.adapter = requestAdapter
        val requests = dbHelper.getRequests(status = "Accepted")
        requestAdapter.updateData(requests)
        updateUI(requests.isEmpty())
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbStatus.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvStatus.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.tvEmptyStatus.visibility = View.GONE
    }

    private fun updateUI(isEmpty: Boolean) {
        binding.pbStatus.visibility = View.GONE
        if (isEmpty) {
            binding.rvStatus.visibility = View.GONE
            binding.tvEmptyStatus.visibility = View.VISIBLE
        } else {
            binding.rvStatus.visibility = View.VISIBLE
            binding.tvEmptyStatus.visibility = View.GONE
        }
    }
}
