package com.example.musicplayer.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.Playable
import com.example.musicplayer.R
import com.example.musicplayer.data.MediaViewModel
import com.example.musicplayer.data.Track
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.load
import com.example.musicplayer.notifications.CreateNotification
import com.example.musicplayer.notifications.OnClearFromRecentService


class MainActivity : AppCompatActivity(), Playable {
    private val NOTIFICATION_CHANNEL_NAME = "MediaPlayer"
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var binding: ActivityMainBinding
    private lateinit var mediaViewModel: MediaViewModel
    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var updater: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mediaViewModel = ViewModelProvider(this).get(MediaViewModel::class.java)

        mediaViewModel.currentTrack.observe(this, {
            changeView(it)
        })

        bindListeners()
        createNotificationChannel()
        registerReceiver(broadcastReceiver, IntentFilter("TRACKS_TRACKS"))
        startService(Intent(applicationContext, OnClearFromRecentService::class.java))
        prepareMediaPlayer()
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {

            when (intent?.extras?.getString("actionname")) {
                CreateNotification.ACTION_PREVIOUS -> onBtnPrev()
                CreateNotification.ACTION_PLAY -> {
                    if (mediaViewModel.isPlaying) {
                        onBtnPause()
                    } else {
                        onBtnPlay()
                    }
                }
                CreateNotification.ACTION_NEXT -> onBtnNext()
            }
        }
    }

    private fun bindListeners() {
        binding.apply {
            btnPlay.setOnClickListener {
                if (!mediaViewModel.isPlaying) {
                    onBtnPlay()
                    btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
                } else {
                    onBtnPause()
                    btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }
            }
            btnNext.setOnClickListener {
                onBtnNext()
                btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
            btnPrev.setOnClickListener {
                onBtnPrev()
                btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
            sbMain.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, pos: Int, chanded: Boolean) {
                    if (chanded) {
                        mediaViewModel.mediaPlayer.seekTo(pos)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    // pass
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    // pass
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        changeView(mediaViewModel.currentTrack.value)
        initializeUpdater()
        handler.postDelayed(updater, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CreateNotification.CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.createNotificationChannel(channel)
    }

    private fun prepareMediaPlayer() {
        try {
            val track = mediaViewModel.tracklist[mediaViewModel.position].also {
                mediaViewModel.currentTrack.value = it
            }
            mediaViewModel.mediaPlayer.setDataSource(track.trackUri)
            mediaViewModel.mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initializeUpdater() {
        updater = Runnable {
            changeTime(mediaViewModel.currentPosition)
            handler.postDelayed(updater, 1000)
        }
    }


    override fun onBtnPlay() {
        initializeUpdater()
        mediaViewModel.currentTrack.value?.let {
            CreateNotification.createNotification(
                applicationContext,
                it
            )
        }
        mediaViewModel.mediaPlayer.start()

        handler.postDelayed(updater, 1000)
    }

    override fun onBtnPause() {
        initializeUpdater()
        mediaViewModel.currentTrack.value?.let {
            CreateNotification.createNotification(
                applicationContext,
                it
            )
        }
        mediaViewModel.mediaPlayer.pause()

    }

    override fun onBtnNext() {
        initializeUpdater()
        handler.removeCallbacks(updater)
        mediaViewModel.mediaPlayer.reset()
        if (mediaViewModel.position == mediaViewModel.tracklist.size - 1) {
            mediaViewModel.position = 0
        } else mediaViewModel.position++
        mediaViewModel.currentTrack.value = mediaViewModel.tracklist[mediaViewModel.position]

        prepareMediaPlayer()
    }

    override fun onBtnPrev() {
        initializeUpdater()
        handler.removeCallbacks(updater)
        mediaViewModel.mediaPlayer.reset()
        if (mediaViewModel.position == 0) {
            mediaViewModel.position = mediaViewModel.tracklist.size - 1
        } else mediaViewModel.position--
        mediaViewModel.currentTrack.value = mediaViewModel.tracklist[mediaViewModel.position]

        prepareMediaPlayer()

        handler.postDelayed(updater, 1000)
    }

    private fun changeView(track: Track?) {
        binding.apply {
            tvTitle.text = track?.title
            tvAuthor.text = track?.artist
            if (track != null) {
                ivCover.load(track.bitmapUri)
                sbMain.max = track.duration
            }
            sbMain.progress = 0
            tvTimer.text =
                mediaViewModel.millisecondsToTimer(mediaViewModel.mediaPlayer.duration.toLong())
            if (mediaViewModel.isPlaying) {
                btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
            } else {
                btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
        }

    }

    private fun changeTime(i: Int) {
        binding.sbMain.progress = i
        binding.tvTimer.text =
            mediaViewModel.millisecondsToTimer(
                i.toLong()
            )
        if (binding.sbMain.progress == binding.sbMain.max){
            binding.btnPlay.setImageResource(R.drawable.ic_baseline_restart_24)
        }
    }
}