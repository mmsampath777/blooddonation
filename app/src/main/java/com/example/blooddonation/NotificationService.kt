package com.example.blooddonation

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*

class NotificationService : Service() {

    private lateinit var requestsRef: DatabaseReference
    private var lastRequestTimestamp: Long = System.currentTimeMillis()

    override fun onCreate() {
        super.onCreate()
        requestsRef = FirebaseDatabase.getInstance().getReference("requests")
        startForegroundService()
        listenForNewRequests()
    }

    private fun startForegroundService() {
        val notificationIntent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(this, BloodDonationApp.CHANNEL_ID)
            .setContentTitle("LifeLink Active")
            .setContentText("Listening for emergency blood requests...")
            .setSmallIcon(R.drawable.ic_blood_drop)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun listenForNewRequests() {
        // Only notify for requests created AFTER the service starts
        requestsRef.orderByChild("timestamp").startAt(lastRequestTimestamp.toDouble() + 1)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val request = snapshot.getValue(DatabaseHelper.BloodRequest::class.java)
                    if (request != null) {
                        sendNewRequestNotification(request)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun sendNewRequestNotification(request: DatabaseHelper.BloodRequest) {
        val intent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(this, BloodDonationApp.EMERGENCY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_blood_drop)
            .setContentTitle("New Blood Request: ${request.bloodGroup}")
            .setContentText("Emergency at ${request.hospital}. Can you help?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
