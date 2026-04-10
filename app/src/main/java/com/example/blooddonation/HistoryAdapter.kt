package com.example.blooddonation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blooddonation.databinding.ItemDonationHistoryBinding

class HistoryAdapter(private var history: List<DatabaseHelper.DonationRecord>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(val binding: ItemDonationHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemDonationHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = history[position]
        holder.binding.apply {
            tvHistoryDate.text = item.date
            tvHistoryHospital.text = item.hospital
            tvHistoryStatus.text = item.status
        }
    }

    override fun getItemCount() = history.size

    fun updateData(newList: List<DatabaseHelper.DonationRecord>) {
        this.history = newList
        notifyDataSetChanged()
    }
}
