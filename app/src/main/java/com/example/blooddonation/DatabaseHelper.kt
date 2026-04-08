package com.example.blooddonation

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "LifeLink.db"
        private const val DATABASE_VERSION = 3 // Upgraded for new columns

        // Users Table
        const val TABLE_USERS = "users"
        const val COL_USER_ID = "id"
        const val COL_USER_NAME = "name"
        const val COL_USER_EMAIL = "email"
        const val COL_USER_PHONE = "phone"
        const val COL_USER_BLOOD = "bloodGroup"
        const val COL_USER_LOCATION = "location"
        const val COL_USER_PASS = "password"
        const val COL_USER_AVAILABLE = "available"
        const val COL_USER_LAST_DONATION = "lastDonationDate"
        const val COL_USER_DONATION_COUNT = "donationCount"

        // Donors Table
        const val TABLE_DONORS = "donors"
        const val COL_DONOR_ID = "id"
        const val COL_DONOR_NAME = "name"
        const val COL_DONOR_BLOOD = "bloodGroup"
        const val COL_DONOR_PHONE = "phone"
        const val COL_DONOR_LOCATION = "location"
        const val COL_DONOR_AVAILABLE = "available"
        const val COL_DONOR_DONATION_COUNT = "donationCount"

        // Requests Table
        const val TABLE_REQUESTS = "requests"
        const val COL_REQ_ID = "id"
        const val COL_REQ_BLOOD = "bloodGroup"
        const val COL_REQ_UNITS = "units"
        const val COL_REQ_HOSPITAL = "hospital"
        const val COL_REQ_LOCATION = "location"
        const val COL_REQ_STATUS = "status"
        const val COL_REQ_IS_EMERGENCY = "isEmergency"
        const val COL_REQ_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsers = ("CREATE TABLE $TABLE_USERS ($COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_USER_NAME TEXT, $COL_USER_EMAIL TEXT, $COL_USER_PHONE TEXT, " +
                "$COL_USER_BLOOD TEXT, $COL_USER_LOCATION TEXT, $COL_USER_PASS TEXT, " +
                "$COL_USER_AVAILABLE INTEGER DEFAULT 1, $COL_USER_LAST_DONATION TEXT, " +
                "$COL_USER_DONATION_COUNT INTEGER DEFAULT 0)")
        
        val createDonors = ("CREATE TABLE $TABLE_DONORS ($COL_DONOR_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_DONOR_NAME TEXT, $COL_DONOR_BLOOD TEXT, $COL_DONOR_PHONE TEXT, " +
                "$COL_DONOR_LOCATION TEXT, $COL_DONOR_AVAILABLE INTEGER DEFAULT 1, " +
                "$COL_DONOR_DONATION_COUNT INTEGER DEFAULT 0)")
        
        val createRequests = ("CREATE TABLE $TABLE_REQUESTS ($COL_REQ_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_REQ_BLOOD TEXT, $COL_REQ_UNITS TEXT, $COL_REQ_HOSPITAL TEXT, " +
                "$COL_REQ_LOCATION TEXT, $COL_REQ_STATUS TEXT, $COL_REQ_IS_EMERGENCY INTEGER DEFAULT 0, " +
                "$COL_REQ_TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")

        db?.execSQL(createUsers)
        db?.execSQL(createDonors)
        db?.execSQL(createRequests)

        seedDonors(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_DONORS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_REQUESTS")
        onCreate(db)
    }

    private fun seedDonors(db: SQLiteDatabase?) {
        val donors = arrayOf(
            arrayOf("John Doe", "A+", "1234567890", "New York", "5"),
            arrayOf("Sarah Wilson", "O-", "9876543210", "Brooklyn", "2"),
            arrayOf("Michael Chen", "B+", "5550192345", "Queens", "0"),
            arrayOf("Emma Davis", "AB+", "4441239876", "Manhattan", "8")
        )
        for (d in donors) {
            val v = ContentValues().apply {
                put(COL_DONOR_NAME, d[0])
                put(COL_DONOR_BLOOD, d[1])
                put(COL_DONOR_PHONE, d[2])
                put(COL_DONOR_LOCATION, d[3])
                put(COL_DONOR_DONATION_COUNT, d[4].toInt())
                put(COL_DONOR_AVAILABLE, 1)
            }
            db?.insert(TABLE_DONORS, null, v)
        }
    }

    // --- User Operations ---
    fun registerUser(name: String, email: String, phone: String, blood: String, loc: String, pass: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_NAME, name)
            put(COL_USER_EMAIL, email)
            put(COL_USER_PHONE, phone)
            put(COL_USER_BLOOD, blood)
            put(COL_USER_LOCATION, loc)
            put(COL_USER_PASS, pass)
            put(COL_USER_AVAILABLE, 1)
            put(COL_USER_DONATION_COUNT, 0)
        }
        val userId = db.insert(TABLE_USERS, null, values)
        if (userId != -1L) {
            addDonor(name, blood, phone, loc)
        }
        return userId
    }

    fun loginUser(email: String, pass: String): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COL_USER_EMAIL=? AND $COL_USER_PASS=?", arrayOf(email, pass))
    }

    fun getUserData(email: String): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COL_USER_EMAIL=?", arrayOf(email))
    }

    fun updateUserAvailability(email: String, isAvailable: Boolean) {
        val db = this.writableDatabase
        val v = ContentValues()
        v.put(COL_USER_AVAILABLE, if (isAvailable) 1 else 0)
        db.update(TABLE_USERS, v, "$COL_USER_EMAIL=?", arrayOf(email))

        val userCursor = getUserData(email)
        if (userCursor != null && userCursor.moveToFirst()) {
            val phone = userCursor.getString(userCursor.getColumnIndexOrThrow(COL_USER_PHONE))
            val dv = ContentValues()
            dv.put(COL_DONOR_AVAILABLE, if (isAvailable) 1 else 0)
            db.update(TABLE_DONORS, dv, "$COL_DONOR_PHONE=?", arrayOf(phone))
            userCursor.close()
        }
    }

    fun incrementDonationCount(email: String) {
        val db = this.writableDatabase
        val cursor = getUserData(email)
        if (cursor != null && cursor.moveToFirst()) {
            val currentCount = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_DONATION_COUNT))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PHONE))
            
            val newCount = currentCount + 1
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = sdf.format(Date())

            val uv = ContentValues()
            uv.put(COL_USER_DONATION_COUNT, newCount)
            uv.put(COL_USER_LAST_DONATION, currentDate)
            db.update(TABLE_USERS, uv, "$COL_USER_EMAIL=?", arrayOf(email))

            val dv = ContentValues()
            dv.put(COL_DONOR_DONATION_COUNT, newCount)
            db.update(TABLE_DONORS, dv, "$COL_DONOR_PHONE=?", arrayOf(phone))
            
            cursor.close()
        }
    }

    // --- Donor Operations ---
    fun addDonor(name: String, blood: String, phone: String, loc: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_DONOR_NAME, name)
            put(COL_DONOR_BLOOD, blood)
            put(COL_DONOR_PHONE, phone)
            put(COL_DONOR_LOCATION, loc)
            put(COL_DONOR_AVAILABLE, 1)
            put(COL_DONOR_DONATION_COUNT, 0)
        }
        return db.insert(TABLE_DONORS, null, values)
    }

    fun getDonorsFiltered(bloodGroup: String? = null): Cursor {
        val db = this.readableDatabase
        return if (bloodGroup.isNullOrEmpty() || bloodGroup == "All") {
            db.rawQuery("SELECT * FROM $TABLE_DONORS WHERE $COL_DONOR_AVAILABLE=1 ORDER BY $COL_DONOR_DONATION_COUNT DESC", null)
        } else {
            db.rawQuery("SELECT * FROM $TABLE_DONORS WHERE $COL_DONOR_BLOOD=? AND $COL_DONOR_AVAILABLE=1 ORDER BY $COL_DONOR_DONATION_COUNT DESC", arrayOf(bloodGroup))
        }
    }

    fun getTotalDonorsCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_DONORS", null)
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    // --- Request Operations ---
    fun addRequest(blood: String, units: String, hospital: String, location: String, isEmergency: Boolean = false): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_REQ_BLOOD, blood)
            put(COL_REQ_UNITS, units)
            put(COL_REQ_HOSPITAL, hospital)
            put(COL_REQ_LOCATION, location)
            put(COL_REQ_STATUS, "Pending")
            put(COL_REQ_IS_EMERGENCY, if (isEmergency) 1 else 0)
        }
        return db.insert(TABLE_REQUESTS, null, values)
    }

    fun getActiveRequestsCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_REQUESTS WHERE $COL_REQ_STATUS='Pending'", null)
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    fun getTotalDonationsCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COL_USER_DONATION_COUNT) FROM $TABLE_USERS", null)
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }
}
