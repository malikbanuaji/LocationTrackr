package com.example.locationtrackingk

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi

public const val CHANNEL_ID = "Normal Notification"


class App : Application () {

    override fun onCreate() {
        super.onCreate()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(){
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Channel 1",
            NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "This is channel 1"
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }
}