package com.example.musicplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

import com.example.musicplayer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var position = 0
    lateinit var updater:Runnable
    lateinit var mediaPresenter: MediaPresenter
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaPresenter = MediaPresenter(this, resources)
        mediaPresenter.binding = binding
        mediaPresenter.handler = handler


        binding.btnPlay.setOnClickListener {
            if (!mediaPresenter.isPlaying()) {
                mediaPresenter.onBtnPlay()
                binding.btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
            } else {
                mediaPresenter.onBtnPause()
                binding.btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
        }
        binding.btnNext.setOnClickListener {
            mediaPresenter.onBtnNext()
            binding.btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
        binding.btnPrev.setOnClickListener{
            mediaPresenter.onBtnPrev()
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
        mediaPresenter.initPlayer()
    }
}