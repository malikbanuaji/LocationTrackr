package com.example.locationtrackingk

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.android.gms.location.*

class MyServices: Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationRequest = LocationRequest.create()
    private var locationCallback = LocationCallback()
    private lateinit var latitude: String
    private lateinit var longitude: String

    override fun onCreate() {
        super.onCreate()
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?: return
                for (location in locationResult.locations){
                    Log.d(TAG, "latitude" + location?.latitude.toString() + "\nlongitude" + location?.longitude.toString())
                    latitude = location?.latitude.toString()
                    longitude = location?.longitude.toString()
                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent?.getStringExtra(MY_MESSAGE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_SERVICES)
            .setContentTitle(input)
            .setSmallIcon(R.drawable.notification_icon_background)
            .setContentText("Latitude $latitude \n Longitude $longitude")

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
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