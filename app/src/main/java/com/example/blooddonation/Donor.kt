package com.example.blooddonation

data class Donor(
    val id: String = "",
    val name: String = "",
    val bloodGroup: String = "",
    val phone: String = "",
    val location: String = "",
    val donationCount: Int = 0,
    val isBestMatch: Boolean = false,
    val available: Boolean = true
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
