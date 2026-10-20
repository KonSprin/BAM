package com.example.lab1

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class ActivityService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d("BAM", "ActivityService onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BAM", "ActivityService onStartCommand")
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("BAM", "ActivityService onBind")
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("BAM", "ActivityService onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BAM", "ActivityService onDestroy")
    }
}
