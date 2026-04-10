package com.example.blooddonation

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "LifeLink.db"
        private const val DATABASE_VERSION = 6 // Upgraded for Lat/Lng support

        // Users Table
        const val TABLE_USERS = "users"
        const val COL_USER_ID = "id"
        const val COL_USER_NAME = "name"
        const val COL_USER_EMAIL = "email"
        const val COL_USER_PHONE = "phone"
        const val COL_USER_BLOOD = "bloodGroup"
        const val COL_USER_LOCATION = "location"
        const val COL_USER_LAT = "latitude"
        const val COL_USER_LON = "longitude"
        const val COL_USER_PASS = "password"
        const val COL_USER_AVAILABLE = "available"
        const val COL_USER_LAST_DONATION = "lastDonationDate"
        const val COL_USER_DONATION_COUNT = "donationCount"
        const val COL_USER_IMAGE = "profileImage"

        // Donors Table
        const val TABLE_DONORS = "donors"
        const val COL_DONOR_ID = "id"
        const val COL_DONOR_NAME = "name"
        const val COL_DONOR_BLOOD = "bloodGroup"
        const val COL_DONOR_PHONE = "phone"
        const val COL_DONOR_LOCATION = "location"
        const val COL_DONOR_LAT = "latitude"
        const val COL_DONOR_LON = "longitude"
        const val COL_DONOR_AVAILABLE = "available"
        const val COL_DONOR_DONATION_COUNT = "donationCount"

        // Requests Table
        const val TABLE_REQUESTS = "requests"
        const val COL_REQ_ID = "id"
        const val COL_REQ_BLOOD = "bloodGroup"
        const val COL_REQ_UNITS = "units"
        const val COL_REQ_HOSPITAL = "hospital"
        const val COL_REQ_LOCATION = "location"
        const val COL_REQ_LAT = "latitude"
        const val COL_REQ_LON = "longitude"
        const val COL_REQ_PHONE = "phone"
        const val COL_REQ_STATUS = "status"
        const val COL_REQ_IS_EMERGENCY = "isEmergency"
        const val COL_REQ_TIMESTAMP = "timestamp"

        // History Table
        const val TABLE_HISTORY = "donation_history"
        const val COL_HIST_ID = "id"
        const val COL_HIST_USER_EMAIL = "user_email"
        const val COL_HIST_DATE = "date"
        const val COL_HIST_HOSPITAL = "hospital"
        const val COL_HIST_STATUS = "status"
    }

    data class User(
        val id: Int = 0,
        val name: String = "",
        val email: String = "",
        val phone: String = "",
        val bloodGroup: String = "",
        val location: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val available: Boolean = true,
        val lastDonationDate: String = "",
        val donationCount: Int = 0,
        val profileImageUrl: String = ""
    )

    data class BloodRequest(
        val id: String = "",
        val bloodGroup: String = "",
        val units: String = "",
        val hospital: String = "",
        val location: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val phone: String = "",
        val status: String = "Pending",
        val isEmergency: Boolean = false,
        val timestamp: Long = 0
    )

    data class DonationRecord(
        val id: String = "",
        val date: String = "",
        val hospital: String = "",
        val status: String = "Completed"
    )

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_USERS ($COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_USER_NAME TEXT, $COL_USER_EMAIL TEXT, $COL_USER_PHONE TEXT, $COL_USER_BLOOD TEXT, $COL_USER_LOCATION TEXT, $COL_USER_LAT REAL, $COL_USER_LON REAL, $COL_USER_PASS TEXT, $COL_USER_AVAILABLE INTEGER DEFAULT 1, $COL_USER_LAST_DONATION TEXT, $COL_USER_DONATION_COUNT INTEGER DEFAULT 0, $COL_USER_IMAGE TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_DONORS ($COL_DONOR_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_DONOR_NAME TEXT, $COL_DONOR_BLOOD TEXT, $COL_DONOR_PHONE TEXT, $COL_DONOR_LOCATION TEXT, $COL_DONOR_LAT REAL, $COL_DONOR_LON REAL, $COL_DONOR_AVAILABLE INTEGER DEFAULT 1, $COL_DONOR_DONATION_COUNT INTEGER DEFAULT 0)")
        db?.execSQL("CREATE TABLE $TABLE_REQUESTS ($COL_REQ_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_REQ_BLOOD TEXT, $COL_REQ_UNITS TEXT, $COL_REQ_HOSPITAL TEXT, $COL_REQ_LOCATION TEXT, $COL_REQ_LAT REAL, $COL_REQ_LON REAL, $COL_REQ_PHONE TEXT, $COL_REQ_STATUS TEXT, $COL_REQ_IS_EMERGENCY INTEGER DEFAULT 0, $COL_REQ_TIMESTAMP LONG)")
        db?.execSQL("CREATE TABLE $TABLE_HISTORY ($COL_HIST_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_HIST_USER_EMAIL TEXT, $COL_HIST_DATE TEXT, $COL_HIST_HOSPITAL TEXT, $COL_HIST_STATUS TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 6) {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_DONORS")
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_REQUESTS")
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
            onCreate(db)
        }
    }

    fun registerUser(name: String, email: String, phone: String, blood: String, loc: String, lat: Double, lon: Double, pass: String): Long {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put(COL_USER_NAME, name); put(COL_USER_EMAIL, email); put(COL_USER_PHONE, phone)
            put(COL_USER_BLOOD, blood); put(COL_USER_LOCATION, loc)
            put(COL_USER_LAT, lat); put(COL_USER_LON, lon); put(COL_USER_PASS, pass)
        }
        val id = db.insert(TABLE_USERS, null, v)
        if (id != -1L) addDonor(name, blood, phone, loc, lat, lon)
        return id
    }

    fun loginUser(email: String, pass: String): User? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COL_USER_EMAIL=? AND $COL_USER_PASS=?", arrayOf(email, pass))
        var user: User? = null
        if (cursor.moveToFirst()) user = cursorToUser(cursor)
        cursor.close()
        return user
    }

    fun getUserData(email: String): User? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COL_USER_EMAIL=?", arrayOf(email))
        var user: User? = null
        if (cursor.moveToFirst()) user = cursorToUser(cursor)
        cursor.close()
        return user
    }

    private fun cursorToUser(cursor: Cursor) = User(
        cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)),
        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PHONE)),
        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_BLOOD)),
        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_LOCATION)),
        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_USER_LAT)),
        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_USER_LON)),
        cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_AVAILABLE)) == 1,
        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_LAST_DONATION)) ?: "",
        cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_DONATION_COUNT)),
        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_IMAGE)) ?: ""
    )

    fun updateProfileImage(email: String, path: String) {
        val db = this.writableDatabase
        val v = ContentValues().apply { put(COL_USER_IMAGE, path) }
        db.update(TABLE_USERS, v, "$COL_USER_EMAIL=?", arrayOf(email))
    }

    fun updateProfileLocation(email: String, loc: String, lat: Double, lon: Double) {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put(COL_USER_LOCATION, loc)
            put(COL_USER_LAT, lat)
            put(COL_USER_LON, lon)
        }
        db.update(TABLE_USERS, v, "$COL_USER_EMAIL=?", arrayOf(email))
        
        val user = getUserData(email)
        if (user != null) {
            val dv = ContentValues().apply {
                put(COL_DONOR_LOCATION, loc)
                put(COL_DONOR_LAT, lat)
                put(COL_DONOR_LON, lon)
            }
            db.update(TABLE_DONORS, dv, "$COL_DONOR_PHONE=?", arrayOf(user.phone))
        }
    }

    fun updateUserAvailability(email: String, available: Boolean) {
        val db = this.writableDatabase
        val v = ContentValues().apply { put(COL_USER_AVAILABLE, if (available) 1 else 0) }
        db.update(TABLE_USERS, v, "$COL_USER_EMAIL=?", arrayOf(email))
        
        val user = getUserData(email)
        if (user != null) {
            val dv = ContentValues().apply { put(COL_DONOR_AVAILABLE, if (available) 1 else 0) }
            db.update(TABLE_DONORS, dv, "$COL_DONOR_PHONE=?", arrayOf(user.phone))
        }
    }

    fun addDonor(name: String, blood: String, phone: String, loc: String, lat: Double, lon: Double) {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put(COL_DONOR_NAME, name); put(COL_DONOR_BLOOD, blood)
            put(COL_DONOR_PHONE, phone); put(COL_DONOR_LOCATION, loc)
            put(COL_DONOR_LAT, lat); put(COL_DONOR_LON, lon)
        }
        db.insert(TABLE_DONORS, null, v)
    }

    fun getDonorsFiltered(blood: String? = null, userLat: Double = 0.0, userLon: Double = 0.0, onlyAvailable: Boolean = true): List<Donor> {
        val db = this.readableDatabase
        val query = StringBuilder("SELECT * FROM $TABLE_DONORS WHERE 1=1")
        val args = mutableListOf<String>()
        if (onlyAvailable) query.append(" AND $COL_DONOR_AVAILABLE=1")
        if (!blood.isNullOrEmpty() && blood != "All") {
            query.append(" AND $COL_DONOR_BLOOD=?"); args.add(blood)
        }
        
        if (userLat != 0.0 && userLon != 0.0) {
            query.append(" ORDER BY (($COL_DONOR_LAT - $userLat)*($COL_DONOR_LAT - $userLat) + ($COL_DONOR_LON - $userLon)*($COL_DONOR_LON - $userLon)) ASC")
        } else {
            query.append(" ORDER BY $COL_DONOR_DONATION_COUNT DESC")
        }
        
        val cursor = db.rawQuery(query.toString(), args.toTypedArray())
        val list = mutableListOf<Donor>()
        if (cursor.moveToFirst()) {
            do {
                list.add(Donor(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_DONOR_ID)).toString(),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DONOR_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DONOR_BLOOD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DONOR_PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DONOR_LOCATION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_DONOR_DONATION_COUNT)),
                    false, cursor.getInt(cursor.getColumnIndexOrThrow(COL_DONOR_AVAILABLE)) == 1
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addRequest(blood: String, units: String, hosp: String, loc: String, lat: Double, lon: Double, phone: String, em: Boolean): Long {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put(COL_REQ_BLOOD, blood); put(COL_REQ_UNITS, units); put(COL_REQ_HOSPITAL, hosp)
            put(COL_REQ_LOCATION, loc); put(COL_REQ_LAT, lat); put(COL_REQ_LON, lon)
            put(COL_REQ_PHONE, phone); put(COL_REQ_STATUS, "Pending")
            put(COL_REQ_IS_EMERGENCY, if (em) 1 else 0); put(COL_REQ_TIMESTAMP, System.currentTimeMillis())
        }
        return db.insert(TABLE_REQUESTS, null, v)
    }

    fun getRequests(status: String? = null): List<BloodRequest> {
        val db = this.readableDatabase
        val query = if (status == null) "SELECT * FROM $TABLE_REQUESTS" else "SELECT * FROM $TABLE_REQUESTS WHERE $COL_REQ_STATUS=?"
        val cursor = db.rawQuery(query, if (status == null) null else arrayOf(status))
        val list = mutableListOf<BloodRequest>()
        if (cursor.moveToFirst()) {
            do {
                list.add(BloodRequest(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_REQ_ID)).toString(),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_REQ_BLOOD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_REQ_UNITS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_REQ_HOSPITAL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_REQ_LOCATION)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_REQ_LAT)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_REQ_LON)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_REQ_PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_REQ_STATUS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_REQ_IS_EMERGENCY)) == 1,
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_REQ_TIMESTAMP))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateRequestStatus(id: String, status: String) {
        val db = this.writableDatabase
        val v = ContentValues().apply { put(COL_REQ_STATUS, status) }
        db.update(TABLE_REQUESTS, v, "$COL_REQ_ID=?", arrayOf(id))
    }

    fun getDonationHistory(email: String): List<DonationRecord> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_HISTORY WHERE $COL_HIST_USER_EMAIL=?", arrayOf(email))
        val list = mutableListOf<DonationRecord>()
        if (cursor.moveToFirst()) {
            do {
                list.add(DonationRecord(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_HIST_ID)).toString(),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_HOSPITAL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_STATUS))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getStats(): Triple<Int, Int, Int> {
        val db = this.readableDatabase
        val d = db.rawQuery("SELECT COUNT(*) FROM $TABLE_DONORS", null)
        val r = db.rawQuery("SELECT COUNT(*) FROM $TABLE_REQUESTS WHERE $COL_REQ_STATUS='Pending'", null)
        val s = db.rawQuery("SELECT SUM($COL_USER_DONATION_COUNT) FROM $TABLE_USERS", null)
        var dc = 0; if (d.moveToFirst()) dc = d.getInt(0)
        var rc = 0; if (r.moveToFirst()) rc = r.getInt(0)
        var sc = 0; if (s.moveToFirst()) sc = s.getInt(0)
        d.close(); r.close(); s.close()
        return Triple(dc, rc, sc)
    }
}
