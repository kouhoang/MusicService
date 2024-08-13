package com.example.musicservice.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.AssetFileDescriptor
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.musicapp.R
import com.example.musicapp.model.Track
import com.example.musicapp.service.ActionPlaying
import com.example.musicapp.utils.secondsToMinutesSeconds
import com.example.musicservice.MusicService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val LAST_TRACK_POS = 4
private const val FIRST_TRACK_POS = 0

class TracksFragment :
    Fragment(R.layout.fragment_tracks),
    ActionPlaying {
    private lateinit var musicService: MusicService
    private var isServiceBound: Boolean = false
    private val tracks =
        listOf(
            Track(R.raw.hongchieunguyen, "Hồng Chiêu Nguyện", R.drawable.image_1),
            Track(R.raw.bachnguyetquangvanotchusa, "Bạch Nguyệt Quang và Nốt Chu Sa", R.drawable.image_2),
            Track(R.raw.nguoitheoduoianhsang, "Người theo đuổi ánh sáng", R.drawable.image_3),
            Track(R.raw.tinhve, "Tinh vệ", R.drawable.image_4),
            Track(R.raw.vaygiu, "Vây giữ", R.drawable.image_5),
        )

    private lateinit var playImage: ImageView
    private lateinit var pauseImage: ImageView
    private lateinit var nextTrack: ImageView
    private lateinit var previousTrack: ImageView
    private lateinit var seekBar: SeekBar
    private lateinit var trackTitle: TextView
    private lateinit var currentTime: TextView
    private lateinit var trackLength: TextView
    private lateinit var trackImage: ImageView

    private val connection =
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?,
            ) {
                val binder = service as MusicService.MusicBinder
                musicService = binder.getService()
                isServiceBound = true

                musicService.setCallBack(this@TracksFragment)

                musicService.getPlayer().setOnCompletionListener {
                    onTrackEnd()
                }

                // Gọi restoreScreen ngay sau khi kết nối dịch vụ
                restoreScreen()

                seekBar.setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean,
                        ) {
                            if (fromUser) {
                                musicService.seekTo(progress)
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    },
                )
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isServiceBound = false
            }
        }

    private fun onTrackEnd() {
        pauseImage.visibility = View.INVISIBLE
        playImage.visibility = View.VISIBLE
        currentTime.text =
            musicService
                .getDuration()
                .milliseconds.inWholeSeconds
                .secondsToMinutesSeconds()
        seekBar.progress = musicService.getCurrentPosition()
    }

    private fun restoreScreen() {
        if (isServiceBound) {
            initializeSeekBar()
            seekBar.progress = musicService.getCurrentPosition()

            val currentTrackPos = musicService.getTrackPosInList()
            val trackName =
                if (currentTrackPos in tracks.indices) {
                    tracks[currentTrackPos].name
                } else {
                    "Unknown Track" // Tên bài hát mặc định nếu vị trí không hợp lệ
                }
            trackTitle.text = trackName

            val imageResId =
                if (currentTrackPos in tracks.indices) {
                    tracks[currentTrackPos].imageResId
                } else {
                    R.drawable.image_1
                }
            trackImage.setImageResource(imageResId)

            if (musicService.isPlaying()) {
                pauseImage.visibility = View.VISIBLE
                playImage.visibility = View.INVISIBLE
            } else {
                pauseImage.visibility = View.INVISIBLE
                playImage.visibility = View.VISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(requireContext(), MusicService::class.java)
        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)

        // Gọi restoreScreen ngay sau khi dịch vụ được kết nối
        if (isServiceBound) {
            restoreScreen()
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        playImage = view.findViewById(R.id.play_image)
        pauseImage = view.findViewById(R.id.pause_image)
        nextTrack = view.findViewById(R.id.next_track)
        previousTrack = view.findViewById(R.id.previous_track)
        seekBar = view.findViewById(R.id.seek_bar)
        trackTitle = view.findViewById(R.id.track_title)
        currentTime = view.findViewById(R.id.current_time)
        trackLength = view.findViewById(R.id.track_length)
        trackImage = view.findViewById(R.id.track_image) // Khởi tạo biến này

        val intent = Intent(requireContext(), MusicService::class.java)

        playImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }

            if (isServiceBound) {
                onPlayPauseButtonClicked()
            }
        }

        pauseImage.setOnClickListener {
            if (isServiceBound) {
                onPlayPauseButtonClicked()
            }
        }

        nextTrack.setOnClickListener {
            if (isServiceBound) {
                if (!musicService.getState()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        requireContext().startForegroundService(intent)
                    } else {
                        requireContext().startService(intent)
                    }
                }
                onNextButtonClicked()
            }
        }

        previousTrack.setOnClickListener {
            if (isServiceBound) {
                onPrevButtonClicked()
            }
        }

        // Set default track
        if (isServiceBound) {
            setTrackToPlay(tracks[FIRST_TRACK_POS])
        }
    }

    private fun setTrackToPlay(track: Track) {
        musicService.setTrack(resources.openRawResourceFd(track.resourceId))
        trackTitle.text = track.name
        trackImage.setImageResource(track.imageResId) // Cập nhật hình ảnh
        initializeSeekBar()
        musicService.playTrack()
        pauseImage.visibility = View.VISIBLE
        playImage.visibility = View.INVISIBLE
        musicService.showNotification()
    }

    private fun initializeSeekBar() {
        MainScope().cancel()
        currentTime.text =
            musicService
                .getCurrentPosition()
                .milliseconds.inWholeSeconds
                .secondsToMinutesSeconds()
        seekBar.progress = 0
        seekBar.max = musicService.getDuration()
        trackLength.text = musicService.getDurationInMilli().secondsToMinutesSeconds()

        lifecycleScope.launch(Dispatchers.Main) {
            while (musicService.isPlaying()) {
                val currentTimeMilli = musicService.getCurrentPosition().milliseconds
                seekBar.progress = musicService.getCurrentPosition()
                currentTime.text = currentTimeMilli.inWholeSeconds.secondsToMinutesSeconds()
                delay(1000)
            }
        }
    }

    private fun setTrack(openRawResourceFd: AssetFileDescriptor) {
        musicService.setTrack(openRawResourceFd)
    }

    private fun onPlayButtonClicked() {
        if (!musicService.getState()) {
            setTrack(resources.openRawResourceFd(tracks[0].resourceId))
            initializeSeekBar()
        }
        musicService.playTrack()
        pauseImage.visibility = View.VISIBLE
        playImage.visibility = View.INVISIBLE
        musicService.showNotification()
    }

    override fun onPlayPauseButtonClicked() {
        if (musicService.isPlaying()) {
            onPauseButtonClicked()
        } else {
            onPlayButtonClicked()
        }

        lifecycleScope.launch(Dispatchers.Main) {
            while (musicService.isPlaying()) {
                val currentTimeMilli = musicService.getCurrentPosition().milliseconds
                seekBar.progress = musicService.getCurrentPosition()
                currentTime.text = currentTimeMilli.inWholeSeconds.secondsToMinutesSeconds()
                delay(1000)
            }
        }
    }

    private fun onPauseButtonClicked() {
        pauseImage.visibility = View.INVISIBLE
        playImage.visibility = View.VISIBLE
        musicService.pauseTrack()
        musicService.showNotification()
    }

    override fun onNextButtonClicked() {
        if (musicService.getTrackPosInList() == LAST_TRACK_POS) {
            Toast.makeText(requireContext(), R.string.last_track, Toast.LENGTH_SHORT).show()
        } else {
            musicService.incTrackPosInList()
            val currentTrackPosInList = musicService.getTrackPosInList()
            trackTitle.text = tracks[currentTrackPosInList].name
            trackImage.setImageResource(tracks[currentTrackPosInList].imageResId)
            val currentTrack = resources.openRawResourceFd(tracks[currentTrackPosInList].resourceId)
            musicService.setNewTrack(currentTrack)
            initializeSeekBar()
            pauseImage.visibility = View.VISIBLE
            playImage.visibility = View.INVISIBLE
            musicService.showNotification()
        }
    }

    override fun onPrevButtonClicked() {
        if (musicService.getTrackPosInList() == FIRST_TRACK_POS) {
            Toast.makeText(requireContext(), R.string.first_track, Toast.LENGTH_SHORT).show()
        } else {
            musicService.decTrackPosInList()
            val currentTrackPosInList = musicService.getTrackPosInList()
            trackTitle.text = tracks[currentTrackPosInList].name
            trackImage.setImageResource(tracks[currentTrackPosInList].imageResId)
            val currentTrack = resources.openRawResourceFd(tracks[currentTrackPosInList].resourceId)
            musicService.setNewTrack(currentTrack)
            initializeSeekBar()
            pauseImage.visibility = View.VISIBLE
            playImage.visibility = View.INVISIBLE
            musicService.showNotification()
        }
    }
}
