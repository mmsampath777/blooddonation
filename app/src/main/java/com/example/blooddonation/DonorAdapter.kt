package com.example.blooddonation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.blooddonation.databinding.ItemDonorBinding
import java.util.*

class DonorAdapter(private var donors: List<Donor>) : RecyclerView.Adapter<DonorAdapter.DonorViewHolder>(), Filterable {

    private var donorListFull: List<Donor> = donors

    class DonorViewHolder(val binding: ItemDonorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonorViewHolder {
        val binding = ItemDonorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DonorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DonorViewHolder, position: Int) {
        val donor = donors[position]
        holder.binding.apply {
            tvDonorName.text = donor.name
            tvDonorBloodGroup.text = donor.bloodGroup
            tvDonorLocation.text = donor.location
            tvDonorBadge.text = donor.getBadge()

            if (donor.isBestMatch) {
                tvBestMatch.visibility = View.VISIBLE
                cardDonor.strokeWidth = 2
                cardDonor.setStrokeColor(android.content.res.ColorStateList.valueOf(
                    root.context.getColor(R.color.blood_red)
                ))
            } else {
                tvBestMatch.visibility = View.GONE
                cardDonor.strokeWidth = 0
            }

            btnCall.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${donor.phone}")
                it.context.startActivity(intent)
            }

            btnSMS.setOnClickListener {
                checkPermissionAndSendSms(it.context as Activity, donor.phone, donor.bloodGroup)
            }
        }
    }

    private fun checkPermissionAndSendSms(activity: Activity, phone: String, bloodGroup: String) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            sendDirectSms(activity, phone, bloodGroup)
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.SEND_SMS), 101)
        }
    }

    private fun sendDirectSms(activity: Activity, phone: String, bloodGroup: String) {
        try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                activity.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            val message = "Urgent: $bloodGroup Blood required. Please contact immediately."
            smsManager.sendTextMessage(phone, null, message, null, null)
            Toast.makeText(activity, "SMS sent to donor", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(activity, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = donors.size

    fun updateData(newDonors: List<Donor>) {
        this.donors = newDonors
        this.donorListFull = ArrayList(newDonors)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<Donor>()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(donorListFull)
                } else {
                    val filterPattern = constraint.toString().lowercase(Locale.ROOT).trim()
                    for (item in donorListFull) {
                        if (item.name.lowercase(Locale.ROOT).contains(filterPattern) ||
                            item.bloodGroup.lowercase(Locale.ROOT).contains(filterPattern) ||
                            item.location.lowercase(Locale.ROOT).contains(filterPattern)) {
                            filteredList.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                donors = results?.values as List<Donor>
                notifyDataSetChanged()
            }
        }
    }
}
