package com.example.blooddonation

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blooddonation.databinding.ItemDonorBinding

class DonorAdapter(private var donors: List<Donor>) : RecyclerView.Adapter<DonorAdapter.DonorViewHolder>() {

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
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("smsto:${donor.phone}")
                it.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = donors.size

    fun updateData(newDonors: List<Donor>) {
        this.donors = newDonors
        notifyDataSetChanged()
    }
}
