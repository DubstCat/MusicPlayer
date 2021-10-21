package com.example.musicplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

import com.example.musicplayer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), Playable {
    private val handler = Handler(Looper.getMainLooper())
    private val POSITION_PARAM = "POSITION_PARAM"
    lateinit var binding: ActivityMainBinding
    lateinit var mediaPresenter: MediaPresenter
    lateinit var notificationManager: NotificationManagerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaPresenter = MediaPresenter(baseContext, resources)
        mediaPresenter.binding = binding
        mediaPresenter.handler = handler

        createNotificationChannel()
        registerReceiver(broadcastReceiver, IntentFilter("TRACKS_TRACKS"))
        startService(Intent(baseContext, OnClearFromRecentService::class.java))


        binding.btnPlay.setOnClickListener {
            if (!mediaPresenter.isPlaying()) {
                onBtnPlay()
                binding.btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
            } else {
                onBtnPause()
                binding.btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
        }
        binding.btnNext.setOnClickListener {
            onBtnNext()
            binding.btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
        binding.btnPrev.setOnClickListener{
            onBtnPrev()
            binding.btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }

        binding.sbMain?.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, pos: Int, chanded: Boolean) {
                if (chanded){
                    mediaPresenter.seekTo(pos)
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
                // pass
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                // pass
            }
        })
        mediaPresenter.prepareMediaPlayer()
    }

    val broadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            val action = intent?.extras?.getString("actionname")

            when(action){
                CreateNotification.ACTION_PREVIOUS -> onBtnPrev()
                CreateNotification.ACTION_PLAY -> {
                    if(mediaPresenter.isPlaying()){
                        onBtnPause()
                    }else{
                        onBtnPlay()
                    }
                }
                CreateNotification.ACTION_NEXT -> onBtnNext()
            }
        }
    }

    fun createNotificationChannel(){
        val channel = NotificationChannel(CreateNotification.CHANNEL_ID, "MusicPlayer", NotificationManager.IMPORTANCE_LOW)
        notificationManager = NotificationManagerCompat.from(baseContext)
        notificationManager.createNotificationChannel(channel)
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onBtnPlay() {
        mediaPresenter.onBtnPlay()
    }

    override fun onBtnPause() {
        mediaPresenter.onBtnPause()
    }

    override fun onBtnNext() {
        mediaPresenter.onBtnNext()
    }

    override fun onBtnPrev() {
        mediaPresenter.onBtnPrev()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(POSITION_PARAM, mediaPresenter.position)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mediaPresenter.position = savedInstanceState.getInt(POSITION_PARAM)
    }
}