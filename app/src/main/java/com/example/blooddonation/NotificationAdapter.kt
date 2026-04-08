package com.example.blooddonation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blooddonation.databinding.ItemNotificationBinding

class NotificationAdapter(private val notifications: List<NotificationItem>) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = notifications[position]
        holder.binding.apply {
            tvNotifTitle.text = item.title
            tvNotifMessage.text = item.message
            tvNotifTime.text = item.time
            
            if (item.isEmergency) {
                ivNotifIcon.setImageResource(android.R.drawable.ic_dialog_alert)
            } else {
                ivNotifIcon.setImageResource(android.R.drawable.ic_popup_reminder)
            }
        }
    }

    override fun getItemCount() = notifications.size
}
