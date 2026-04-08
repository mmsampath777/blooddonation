package com.example.blooddonation

data class Donor(
    val id: Int,
    val name: String,
    val bloodGroup: String,
    val phone: String,
    val location: String,
    val donationCount: Int = 0,
    val isBestMatch: Boolean = false
) {
    fun getBadge(): String {
        return when {
            donationCount >= 5 -> "Life Saver"
            donationCount >= 3 -> "Regular Donor"
            donationCount >= 1 -> "First Saver"
            else -> "New Donor"
        }
    }
}
