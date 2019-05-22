package com.example.locationtrackingk

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi

const val CHANNEL_ID = "Normal Notification"
const val CHANNEL_ID_SERVICES = "Services Notification"



class App : Application () {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(){
        val notificationList: MutableList<NotificationChannel> = ArrayList()

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Channel 1",
            NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "This is channel 1"
        }


        val serviceChannel = NotificationChannel(
            CHANNEL_ID_SERVICES,
            "Services Channel",
            NotificationManager.IMPORTANCE_MIN).apply {
            description = "This is service channel"
        }

        notificationList.add(channel)
        notificationList.add(serviceChannel)

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannels(notificationList)
    }
}