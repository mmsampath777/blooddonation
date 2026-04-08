package com.example.blooddonation

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("LifeLinkPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_NAME = "userName"
    }

    fun createLoginSession(email: String, name: String) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_USER_NAME, name)
        editor.apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, "User")

    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
