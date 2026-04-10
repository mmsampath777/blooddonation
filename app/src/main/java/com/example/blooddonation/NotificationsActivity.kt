package com.example.blooddonation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blooddonation.databinding.ActivityNotificationsBinding

class NotificationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        
        binding.toolbarNotifications.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        loadRequests()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(mutableListOf(), dbHelper)
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun loadRequests() {
        val requests = dbHelper.getRequests()
        adapter.updateData(requests)
        
        if (requests.isEmpty()) {
            binding.emptyNotifications.visibility = View.VISIBLE
            binding.rvNotifications.visibility = View.GONE
        } else {
            binding.emptyNotifications.visibility = View.GONE
            binding.rvNotifications.visibility = View.VISIBLE
        }
    }
}
