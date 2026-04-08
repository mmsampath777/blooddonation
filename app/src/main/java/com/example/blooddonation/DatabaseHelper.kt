package com.example.blooddonation

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "LifeLink.db"
        private const val DATABASE_VERSION = 2

        // Users Table
        const val TABLE_USERS = "users"
        const val COL_USER_ID = "id"
        const val COL_USER_NAME = "name"
        const val COL_USER_EMAIL = "email"
        const val COL_USER_PHONE = "phone"
        const val COL_USER_BLOOD = "bloodGroup"
        const val COL_USER_LOCATION = "location"
        const val COL_USER_PASS = "password"
        const val COL_USER_AVAILABLE = "available" // 1 for true, 0 for false

        // Donors Table
        const val TABLE_DONORS = "donors"
        const val COL_DONOR_ID = "id"
        const val COL_DONOR_NAME = "name"
        const val COL_DONOR_BLOOD = "bloodGroup"
        const val COL_DONOR_PHONE = "phone"
        const val COL_DONOR_LOCATION = "location"
        const val COL_DONOR_AVAILABLE = "available"

        // Requests Table
        const val TABLE_REQUESTS = "requests"
        const val COL_REQ_ID = "id"
        const val COL_REQ_BLOOD = "bloodGroup"
        const val COL_REQ_UNITS = "units"
        const val COL_REQ_HOSPITAL = "hospital"
        const val COL_REQ_LOCATION = "location"
        const val COL_REQ_STATUS = "status"
        const val COL_REQ_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsers = ("CREATE TABLE $TABLE_USERS ($COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_USER_NAME TEXT, $COL_USER_EMAIL TEXT, $COL_USER_PHONE TEXT, " +
                "$COL_USER_BLOOD TEXT, $COL_USER_LOCATION TEXT, $COL_USER_PASS TEXT, $COL_USER_AVAILABLE INTEGER DEFAULT 1)")
        
        val createDonors = ("CREATE TABLE $TABLE_DONORS ($COL_DONOR_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_DONOR_NAME TEXT, $COL_DONOR_BLOOD TEXT, $COL_DONOR_PHONE TEXT, $COL_DONOR_LOCATION TEXT, $COL_DONOR_AVAILABLE INTEGER DEFAULT 1)")
        
        val createRequests = ("CREATE TABLE $TABLE_REQUESTS ($COL_REQ_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_REQ_BLOOD TEXT, $COL_REQ_UNITS TEXT, $COL_REQ_HOSPITAL TEXT, $COL_REQ_LOCATION TEXT, $COL_REQ_STATUS TEXT, $COL_REQ_TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")

        db?.execSQL(createUsers)
        db?.execSQL(createDonors)
        db?.execSQL(createRequests)

        // Seed initial donors
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
            arrayOf("John Doe", "A+", "1234567890", "New York"),
            arrayOf("Sarah Wilson", "O-", "9876543210", "Brooklyn"),
            arrayOf("Michael Chen", "B+", "5550192345", "Queens"),
            arrayOf("Emma Davis", "AB+", "4441239876", "Manhattan")
        )
        for (d in donors) {
            val v = ContentValues()
            v.put(COL_DONOR_NAME, d[0])
            v.put(COL_DONOR_BLOOD, d[1])
            v.put(COL_DONOR_PHONE, d[2])
            v.put(COL_DONOR_LOCATION, d[3])
            v.put(COL_DONOR_AVAILABLE, 1)
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
        }
        val userId = db.insert(TABLE_USERS, null, values)
        if (userId != -1L) {
            // Also add as a donor
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

        // Also update in donors table using phone number (simpler for this demo)
        val userCursor = getUserData(email)
        if (userCursor != null && userCursor.moveToFirst()) {
            val phone = userCursor.getString(userCursor.getColumnIndexOrThrow(COL_USER_PHONE))
            val dv = ContentValues()
            dv.put(COL_DONOR_AVAILABLE, if (isAvailable) 1 else 0)
            db.update(TABLE_DONORS, dv, "$COL_DONOR_PHONE=?", arrayOf(phone))
            userCursor.close()
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
        }
        return db.insert(TABLE_DONORS, null, values)
    }

    fun getDonors(bloodGroup: String? = null): Cursor {
        val db = this.readableDatabase
        return if (bloodGroup.isNullOrEmpty() || bloodGroup == "All") {
            db.rawQuery("SELECT * FROM $TABLE_DONORS WHERE $COL_DONOR_AVAILABLE=1", null)
        } else {
            db.rawQuery("SELECT * FROM $TABLE_DONORS WHERE $COL_DONOR_BLOOD=? AND $COL_DONOR_AVAILABLE=1", arrayOf(bloodGroup))
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
    fun addRequest(blood: String, units: String, hospital: String, location: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_REQ_BLOOD, blood)
            put(COL_REQ_UNITS, units)
            put(COL_REQ_HOSPITAL, hospital)
            put(COL_REQ_LOCATION, location)
            put(COL_REQ_STATUS, "Pending")
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
}
