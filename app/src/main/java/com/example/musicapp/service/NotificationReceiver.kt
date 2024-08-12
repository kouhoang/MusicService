package com.example.musicapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context?,
        intent: Intent?,
    ) {
        val action = intent?.action
        val serviceIntent =
            Intent(context, PlayMusicService::class.java).apply {
                this.action = action
            }
        context?.startService(serviceIntent)
    }
}
