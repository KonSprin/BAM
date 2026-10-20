package com.example.lab1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PowerConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> "power connected"
            Intent.ACTION_POWER_DISCONNECTED -> "power disconnected"
            else -> return
        }

        Log.d("BAM", "PowerConnectionReceiver $event")
        context.startActivity(
            Intent(context, SecondActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        )
    }
}
