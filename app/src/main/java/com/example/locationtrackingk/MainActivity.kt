package com.example.locationtrackingk

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


const val TAG = "MainActivity"
private const val MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1101
private const val REQUEST_CHECK_SETTINGS = 0x1
const val MY_MESSAGE = "com.example.locationtrackingk.MY_MESSAGE"

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationRequest = LocationRequest.create()
    private var requestingLocationUpdates = true
    private var locationCallback = LocationCallback()
    private var mBound : Boolean = false
    private lateinit var myServices: MyServices


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?: return
                for (location in locationResult.locations){
                    Log.d(TAG, "latitude" + location?.latitude.toString() + "\nlongitude" + location?.longitude)
                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
    }

    private val connection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MyServices.LocalBinder
            Log.d("BINDER", "$name")
            myServices = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("BINDER", "$name")
            mBound = false
        }
    }

    fun getValFromServices(v: View){
        val longitudeView : TextView = findViewById(R.id.longitudeValue)
        val latitudeView : TextView = findViewById(R.id.latitudeValue)
        try {
            if (mBound){
                val location: Location? = myServices.myLocation

                latitudeView.text = location?.latitude.toString()
                longitudeView.text = location?.longitude.toString()

            }
        } catch (e: UninitializedPropertyAccessException ){
            Toast.makeText(this, "Please Wait ..", Toast.LENGTH_LONG).show()
        }

    }

    fun startServicesGeo(v : View){
        mGetLocation()
    }

    override fun onResume() {
        super.onResume()
        Log.d("onResume", "RESUME")
        Intent(this, MyServices::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_IMPORTANT)
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("onRestart","RESTART")
    }

    override fun onPause() {
        super.onPause()
        Log.d("onPause", "PAUSE")
        if (mBound){
            unbindService(connection)
        }
    }


    fun stopServicesGeo(v: View){
        if (mBound){
            unbindService(connection)
        }
        val serviceIntent = Intent(this, MyServices::class.java)
        stopService(serviceIntent)
    }

    private fun mGetLocation(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION)
        } else {
            createAndBind()
        }
    }

    private fun createAndBind (){
        createLocationSettingsRequest()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    createLocationSettingsRequest()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
            else -> {
                //ignore
            }
        }
    }

    private fun createLocationRequest(){
        locationRequest = LocationRequest.create()?.apply {
            interval = 5000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun createLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener{ locationSettingsResponse ->
            val serviceIntent = Intent(this, MyServices::class.java)
            startService(serviceIntent)
            Intent(this, MyServices::class.java).also { intent ->
                bindService(intent, connection, Context.BIND_IMPORTANT)
            }
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    exception.startResolutionForResult(this@MainActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException){
                    requestingLocationUpdates = false
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CHECK_SETTINGS){
            if (resultCode == PackageManager.PERMISSION_GRANTED){
                createLocationSettingsRequest()
            }
        }
    }

//    private fun getLocation(){
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener {location : Location? ->
//               displayNotification("latitude" + location?.latitude.toString() + "\nlongitude" + location?.longitude)
//            }
//    }

    private fun displayNotification(message: String){
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent : PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon_background)
            .setContentTitle("Title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(message)
                .setSummaryText("Location")
            )

        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }

    fun sendNotification(v: View){
        val editText = findViewById<EditText>(R.id.editText)
        val message = editText.text.toString()
        // Send Notification
        displayNotification(message)



    }
}
