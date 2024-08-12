package com.example.musicapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.example.musicapp.R
import com.example.musicapp.view.MusicPlayerActivity
import com.example.musicapp.viewmodel.MusicViewModel

class PlayMusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false // Changed to 'var'

    private lateinit var viewModel: MusicViewModel

    override fun onCreate() {
        super.onCreate()
        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(MusicViewModel::class.java)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        // Start foreground service
        createNotificationChannel()
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        intent?.let {
            val action = it.action
            handleAction(action)
        }

        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val playPauseIntent =
            Intent(this, NotificationReceiver::class.java).apply {
                action = ACTION_PLAY_PAUSE
            }
        val previousIntent =
            Intent(this, NotificationReceiver::class.java).apply {
                action = ACTION_PREVIOUS
            }
        val nextIntent =
            Intent(this, NotificationReceiver::class.java).apply {
                action = ACTION_NEXT
            }

        val playPausePendingIntent = PendingIntent.getBroadcast(this, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val previousPendingIntent = PendingIntent.getBroadcast(this, 1, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val nextPendingIntent = PendingIntent.getBroadcast(this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle("Playing Music")
            .setContentText("Tap to control playback")
            .setSmallIcon(R.drawable.ic_music)
            .addAction(R.drawable.ic_previous, "Previous", previousPendingIntent)
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                "Play/Pause",
                playPausePendingIntent,
            ).addAction(R.drawable.ic_next, "Next", nextPendingIntent)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MusicPlayerActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            ).build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW,
                )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun handleAction(action: String?) {
        when (action) {
            ACTION_PLAY_PAUSE -> {
                if (isPlaying) {
                    mediaPlayer?.pause()
                    isPlaying = false
                } else {
                    mediaPlayer?.start()
                    isPlaying = true
                }
                updateNotification()
            }
            ACTION_PREVIOUS -> {
                playPreviousSong()
                updateNotification()
            }
            ACTION_NEXT -> {
                playNextSong()
                updateNotification()
            }
        }
    }

    private fun playNextSong() {
        viewModel.playNextSong()
        initializeMediaPlayer()
    }

    private fun playPreviousSong() {
        viewModel.playPreviousSong()
        initializeMediaPlayer()
    }

    private fun initializeMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(applicationContext, viewModel.currentSong.value?.resourceId ?: 0)
        mediaPlayer?.apply {
            setOnCompletionListener {
                viewModel.playNextSong()
                initializeMediaPlayer()
            }
            setOnPreparedListener {
                start()
                this@PlayMusicService.isPlaying = true
                updateNotification()
            }
        }
    }

    private fun updateNotification() {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "MusicPlayerChannel"
        private const val NOTIFICATION_ID = 101
        private const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
        private const val ACTION_PREVIOUS = "ACTION_PREVIOUS"
        private const val ACTION_NEXT = "ACTION_NEXT"
    }
}
