package com.example.musicapp.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.R
import com.example.musicservice.ui.TracksFragment

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, TracksFragment())
            .commit()
    }
}
