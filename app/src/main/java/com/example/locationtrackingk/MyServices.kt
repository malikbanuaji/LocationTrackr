package com.example.locationtrackingk

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase

class MyServices: Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationRequest = LocationRequest.create()
    private var locationCallback = LocationCallback()
    private lateinit var notificationManager: NotificationManager
    private lateinit var plocation: Location
    private val binder = LocalBinder()
    private var latitude: String = ""
    private var longitude: String = ""
    private val listLocation: MutableList<Location> = mutableListOf()
    private val uListLocation: List<Location> = listLocation

    inner class LocalBinder : Binder(){
        fun getService() : MyServices = this@MyServices
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?: return
                for (location in locationResult.locations){
                    plocation = location
                    listLocation.add(location)
                    Log.d(TAG, "latitude" + location?.latitude.toString() + "\nlongitude" + location?.longitude.toString() + myListLocation)
                    notificationManager.notify(2, notification(location.latitude.toString(), location.longitude.toString()))
                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
    }
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    val myListLocation: Int
        get() = uListLocation.size

    val myLocation: Location
        get() = plocation

    private fun notification(latitude: String?, longitude: String?): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent : PendingIntent = PendingIntent.getActivity(this,0, intent ,0)
        val mNotification = NotificationCompat.Builder(this, CHANNEL_ID_SERVICES)
            .setContentTitle("Location")
            .setSmallIcon(R.drawable.notification_icon_background)
            .setContentText("Latitude $latitude \n Longitude $longitude")
            .setContentIntent(pendingIntent)

        return mNotification.build()
    }

    val thisLocation: String
        get() = "Latitude $latitude \n Longitude $longitude "

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        startForeground(2, notification(null, null))

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("location")
        myRef.setValue(uListLocation)
        Log.d(TAG, "Saving State $myListLocation")
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startLocationUpdates(){
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun createLocationRequest(){
        locationRequest = LocationRequest.create()?.apply {
            interval = 5000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
}