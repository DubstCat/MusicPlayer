package com.example.musicplayer

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
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.data.MediaViewModel
import com.example.musicplayer.data.Track
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.notifications.CreateNotification
import com.example.musicplayer.notifications.OnClearFromRecentService


class MainActivity : AppCompatActivity(), Playable {
    private val handler = Handler(Looper.getMainLooper())
    private val NOTIFICATION_CHANNEL_NAME = "MediaPlayer"
    private lateinit var binding: ActivityMainBinding
    private lateinit var mediaViewModel: MediaViewModel
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var updater: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mediaViewModel = ViewModelProvider(this).get(MediaViewModel::class.java)
        mediaPlayer = MediaPlayer()

        mediaViewModel.currentTrack.observe(this, {
            binding.apply {
                changeView(it)
                seekBar?.progress = 0
                seekBar?.max = it.duration
                tvTimer?.text =
                    mediaViewModel.millisecondsToTimer(mediaPlayer.duration.toLong())
            }
        })

        mediaViewModel.currentTime.observe(this, {
            binding.sbMain?.progress = mediaViewModel.currentTime.value ?: 0
            binding.tvTimer?.text =
                mediaViewModel.currentTime.value?.toLong()?.let { it1 ->
                    mediaViewModel.millisecondsToTimer(
                        it1
                    )
                }
        })

        binding.apply {
            btnPlay.setOnClickListener {
                if (!mediaPlayer.isPlaying) {
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
        }

        binding.sbMain?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, pos: Int, chanded: Boolean) {
                if (chanded) {
                    mediaPlayer.seekTo(pos)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                // pass
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                // pass
            }
        })

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
                    if (mediaPlayer.isPlaying) {
                        onBtnPause()
                    } else {
                        onBtnPlay()
                    }
                }
                CreateNotification.ACTION_NEXT -> onBtnNext()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        changeView(mediaViewModel.currentTrack.value)
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

            mediaPlayer.setDataSource(track.trackUri)
            mediaPlayer.prepareAsync()
            mediaViewModel.currentTime.value = 0
            updater = Runnable {
                mediaViewModel.currentTime.value = mediaPlayer.currentPosition
                handler.postDelayed(updater, 1000)
            }
            handler.postDelayed(updater, 1000)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBtnPlay() {
        mediaViewModel.currentTrack.value?.let {
            CreateNotification.createNotification(
                applicationContext,
                it
            )
        }
        mediaPlayer.start()
    }


    override fun onBtnPause() {
        mediaViewModel.currentTrack.value?.let {
            CreateNotification.createNotification(
                applicationContext,
                it
            )
        }
        mediaPlayer.pause()
    }

    override fun onBtnNext() {
        handler.removeCallbacks(updater)
        mediaPlayer.reset()
        mediaPlayer.release()
        if (mediaViewModel.position == mediaViewModel.tracklist.size - 1) {
            mediaViewModel.position = 0
        } else mediaViewModel.position++
        mediaViewModel.currentTrack.value = mediaViewModel.tracklist[mediaViewModel.position]
        mediaViewModel.currentTrack.value?.let {
            CreateNotification.createNotification(
                applicationContext,
                it
            )
        }
        prepareMediaPlayer()
    }

    override fun onBtnPrev() {
        handler.removeCallbacks(updater)
        mediaPlayer.reset()
        mediaPlayer.release()
        if (mediaViewModel.position == 0) {
            mediaViewModel.position = mediaViewModel.tracklist.size - 1
        } else mediaViewModel.position++
        mediaViewModel.currentTrack.value = mediaViewModel.tracklist[mediaViewModel.position]
        mediaViewModel.currentTrack.value?.let {
            CreateNotification.createNotification(
                applicationContext,
                it
            )
        }
        prepareMediaPlayer()
    }

    private fun changeView(track: Track?) {
        binding.apply {
            tvTitle.text = track?.title
            tvAuthor.text = track?.artist
            if (track != null) {
                ivCover.load(track.bitmapUri)
            }
        }
    }
}