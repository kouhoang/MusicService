package com.example.musicapp.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.ViewModelProvider
import com.example.musicapp.R
import com.example.musicapp.service.PlayMusicService
import com.example.musicapp.viewmodel.MusicViewModel

class MusicPlayerActivity : AppCompatActivity() {
    private lateinit var viewModel: MusicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MusicViewModel::class.java)

        val playPauseButton: AppCompatButton = findViewById(R.id.playPauseButton)
        val nextButton: AppCompatButton = findViewById(R.id.nextButton)
        val prevButton: AppCompatButton = findViewById(R.id.prevButton)
        val seekBar: SeekBar = findViewById(R.id.seekBar)
        val currentTimeTextView: TextView = findViewById(R.id.currentTime)
        val totalTimeTextView: TextView = findViewById(R.id.totalTime)

        // Start the PlayMusicService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, PlayMusicService::class.java))
        } else {
            startService(Intent(this, PlayMusicService::class.java))
        }

        playPauseButton.setOnClickListener {
            viewModel.playPauseSong()
        }

        nextButton.setOnClickListener {
            viewModel.playNextSong()
        }

        prevButton.setOnClickListener {
            viewModel.playPreviousSong()
        }

        viewModel.isPlaying.observe(this) { isPlaying ->
            playPauseButton.setBackgroundResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
            )
        }

        viewModel.currentTime.observe(this) { currentTime ->
            seekBar.progress = currentTime
            currentTimeTextView.text = formatTime(currentTime)
        }

        viewModel.totalTime.observe(this) { totalTime ->
            seekBar.max = totalTime
            totalTimeTextView.text = formatTime(totalTime)
        }

        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    if (fromUser) {
                        viewModel.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            },
        )
    }

    private fun formatTime(timeInMillis: Int): String {
        val minutes = (timeInMillis / 1000) / 60
        val seconds = (timeInMillis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
