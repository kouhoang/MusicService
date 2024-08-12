package com.example.musicapp.viewmodel

import android.app.Application
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicapp.R
import com.example.musicapp.model.Song

class MusicViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song> get() = _currentSong

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _currentTime = MutableLiveData<Int>()
    val currentTime: LiveData<Int> get() = _currentTime

    private val _totalTime = MutableLiveData<Int>()
    val totalTime: LiveData<Int> get() = _totalTime

    private val songs =
        listOf(
            Song(1, "Song 1", R.raw.bachnguyetquangvanotchusa),
            Song(2, "Song 2", R.raw.hongchieunguyen),
            Song(3, "Song 3", R.raw.nguoitheoduoianhsang),
            Song(4, "Song 4", R.raw.tinhve),
            Song(5, "Song 5", R.raw.vaygiu),
        )

    private var currentIndex = 0
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private val updateTimeTask =
        object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    _currentTime.value = player.currentPosition
                    handler.postDelayed(this, 1000)
                }
            }
        }

    init {
        _currentSong.value = songs[currentIndex]
    }

    fun playPauseSong() {
        if (mediaPlayer == null) {
            initializeMediaPlayer()
        } else {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _isPlaying.value = false
                handler.removeCallbacks(updateTimeTask)
            } else {
                mediaPlayer?.start()
                _isPlaying.value = true
                handler.post(updateTimeTask)
            }
        }
    }

    fun playNextSong() {
        mediaPlayer?.release()
        handler.removeCallbacks(updateTimeTask)
        currentIndex = (currentIndex + 1) % songs.size
        _currentSong.value = songs[currentIndex]
        initializeMediaPlayer()
    }

    fun playPreviousSong() {
        mediaPlayer?.release()
        handler.removeCallbacks(updateTimeTask)
        currentIndex = if (currentIndex - 1 < 0) songs.size - 1 else currentIndex - 1
        _currentSong.value = songs[currentIndex]
        initializeMediaPlayer()
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        _currentTime.value = position
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer.create(getApplication(), _currentSong.value?.resourceId ?: 0)
        mediaPlayer?.apply {
            setOnCompletionListener {
                _isPlaying.value = false
            }
            setOnPreparedListener {
                _totalTime.value = it.duration
                it.start()
                _isPlaying.value = true
                handler.post(updateTimeTask)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        handler.removeCallbacks(updateTimeTask)
    }
}
