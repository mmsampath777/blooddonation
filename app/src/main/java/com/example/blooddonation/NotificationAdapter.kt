package com.example.blooddonation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.blooddonation.databinding.ItemNotificationBinding

class NotificationAdapter(
    private var requests: List<DatabaseHelper.BloodRequest>,
    private val dbHelper: DatabaseHelper
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val request = requests[position]
        val context = holder.itemView.context
        holder.binding.apply {
            tvNotifTitle.text = "Blood Group: ${request.bloodGroup}"
            tvNotifDesc.text = "Hospital: ${request.hospital}\nUnits: ${request.units}"
            tvNotifTime.text = request.status

            if (request.isEmergency) {
                ivNotifIcon.setImageResource(R.drawable.ic_emergency)
                ivNotifIcon.imageTintList = android.content.res.ColorStateList.valueOf(
                    root.context.getColor(R.color.blood_red)
                )
            } else {
                ivNotifIcon.setImageResource(R.drawable.ic_blood_drop)
                ivNotifIcon.imageTintList = null
            }

            // Show/Hide buttons based on status
            if (request.status == "Pending") {
                layoutActions.visibility = View.VISIBLE
            } else {
                layoutActions.visibility = View.GONE
            }

            btnAccept.setOnClickListener {
                updateStatus(context, request, "Accepted")
            }

            btnReject.setOnClickListener {
                updateStatus(context, request, "Rejected")
            }
        }
    }

    private fun updateStatus(context: Context, request: DatabaseHelper.BloodRequest, newStatus: String) {
        dbHelper.updateRequestStatus(request.id, newStatus)
        Toast.makeText(context, "Request $newStatus", Toast.LENGTH_SHORT).show()
        
        if (newStatus == "Accepted") {
            if (context is Activity) {
                checkPermissionAndSendSms(context, request)
            }
        }
        
        // Refresh the list locally
        if (context is NotificationsActivity) {
            // This is a simple way to refresh the specific screen
            // In a more complex app, we'd use a shared ViewModel or callback
            (context as? Activity)?.recreate() 
        }
    }

    private fun checkPermissionAndSendSms(activity: Activity, request: DatabaseHelper.BloodRequest) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            sendSms(activity, request)
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.SEND_SMS), 102)
        }
    }

    private fun sendSms(activity: Activity, request: DatabaseHelper.BloodRequest) {
        if (request.phone.isEmpty()) return
        try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                activity.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val message = "LifeLink Alert: Your request for ${request.bloodGroup} has been ACCEPTED by a donor. They will contact you shortly."
            smsManager.sendTextMessage(request.phone, null, message, null, null)
            Toast.makeText(activity, "Confirmation SMS sent to requester", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount() = requests.size

    fun updateData(newList: List<DatabaseHelper.BloodRequest>) {
        requests = newList
        notifyDataSetChanged()
    }
}
