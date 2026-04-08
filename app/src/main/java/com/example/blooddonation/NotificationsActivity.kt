package com.example.blooddonation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blooddonation.databinding.ActivityNotificationsBinding

class NotificationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarNotifications.setNavigationOnClickListener { finish() }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val notifications = listOf(
            NotificationItem("Emergency: O+ Needed", "A request has been made near you at City Hospital.", "2 mins ago", true),
            NotificationItem("Donation Reminder", "It's been 3 months since your last donation. Ready to save another life?", "1 day ago"),
            NotificationItem("New Donor Nearby", "John Doe just registered in your area.", "2 days ago")
        )

        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = NotificationAdapter(notifications)

        if (notifications.isEmpty()) {
            binding.emptyNotifications.visibility = View.VISIBLE
            binding.rvNotifications.visibility = View.GONE
        } else {
            binding.emptyNotifications.visibility = View.GONE
            binding.rvNotifications.visibility = View.VISIBLE
        }
    }
}
