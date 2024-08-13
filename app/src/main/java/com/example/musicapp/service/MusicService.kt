package com.example.musicservice

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.musicapp.R
import com.example.musicapp.receiver.NotificationReceiver
import com.example.musicapp.service.ActionPlaying
import kotlin.time.Duration.Companion.milliseconds

private const val MUSIC_ID = 1
private const val ACTION_PREV = "PREVIOUS"
private const val ACTION_PLAY_PAUSE = "PLAY_PAUSE"
private const val ACTION_NEXT = "NEXT"
private const val CHANNEL_ID = "channel_id"

class MusicService : Service() {
    private var trackPosInList = 0

    private var actionPlaying: ActionPlaying? = null
    private val binder = MusicBinder()

    private var isActivated = false
    private val mediaPlayer by lazy { MediaPlayer() }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
        )

        val actionName = intent?.getStringExtra("ActionName")

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(notificationManager)
        }

        startForeground(
            MUSIC_ID,
            createNotification(),
        )

        actionName?.let {
            when (it) {
                "Play" -> actionPlaying?.onPlayPauseButtonClicked()
                "Next" -> actionPlaying?.onNextButtonClicked()
                "Previous" -> actionPlaying?.onPrevButtonClicked()
                else -> {}
            }
        }
        return START_STICKY
    }

    fun setCallBack(actionPlaying: ActionPlaying) {
        this.actionPlaying = actionPlaying
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(notificationManager: NotificationManager) {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "Music notification",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.reset()
        stopSelf()
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val prevIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_PREV)
        val playPauseIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_PLAY_PAUSE)
        val nextIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_NEXT)

        val prevPendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE)
        val playPausePendingIntent = PendingIntent.getBroadcast(this, 0, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)
        val nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)

        val trackTitle = getTrackTitle() // Fetch the track title based on trackPosInList

        val notificationBuilder =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle(trackTitle)
                .addAction(R.drawable.baseline_chevron_left_mini, "Previous", prevPendingIntent)
                .setStyle(
                    androidx.media.app.NotificationCompat
                        .MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2),
                ).setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)

        return if (mediaPlayer.isPlaying) {
            notificationBuilder
                .addAction(R.drawable.baseline_pause_mini, "Pause", playPausePendingIntent)
                .addAction(R.drawable.baseline_chevron_right_mini, "Next", nextPendingIntent)
                .build()
        } else {
            notificationBuilder
                .addAction(R.drawable.baseline_play_arrow_mini, "Play", playPausePendingIntent)
                .addAction(R.drawable.baseline_chevron_right_mini, "Next", nextPendingIntent)
                .build()
        }
    }

    private fun getTrackTitle(): String {
        // Return the track title based on trackPosInList
        val trackTitles =
            listOf(
                "Hồng Chiêu Nguyện",
                "Bạch Nguyệt Quang và Nốt Chu Sa",
                "Người theo đuổi ánh sáng",
                "Tinh vệ",
                "Vây giữ",
            )
        return if (trackPosInList in trackTitles.indices) {
            trackTitles[trackPosInList]
        } else {
            "Unknown Track"
        }
    }

    fun showNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = createNotification()
        notificationManager.notify(MUSIC_ID, notification)
    }

    fun pauseTrack() {
        mediaPlayer.pause()
    }

    fun setTrack(trackId: AssetFileDescriptor) {
        with(mediaPlayer) {
            setDataSource(trackId)
            prepare()
        }
        isActivated = true
    }

    fun setNewTrack(currentTrack: AssetFileDescriptor) {
        with(mediaPlayer) {
            if (isPlaying) mediaPlayer.stop()
            mediaPlayer.reset()
            setTrack(currentTrack)
            mediaPlayer.start()
        }
    }

    fun playTrack() {
        mediaPlayer.start()
    }

    fun getState() = isActivated

    fun getDuration(): Int = mediaPlayer.duration

    fun getDurationInMilli() = getDuration().milliseconds.inWholeSeconds

    fun seekTo(progress: Int) {
        mediaPlayer.seekTo(progress)
    }

    fun isPlaying(): Boolean = mediaPlayer.isPlaying

    fun getCurrentPosition() = mediaPlayer.currentPosition

    fun getPlayer() = mediaPlayer

    fun incTrackPosInList() = trackPosInList++

    fun decTrackPosInList() = trackPosInList--

    fun getTrackPosInList() = trackPosInList
}
