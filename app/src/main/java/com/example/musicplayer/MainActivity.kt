package com.example.musicplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.musicplayer.data.Track
import com.example.musicplayer.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.lang.Exception
import java.lang.StringBuilder
import java.lang.reflect.Type
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var mediaPlayer:MediaPlayer
    private var position = 0
    lateinit var updater:Runnable
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val tracklist = loadTracks()

        binding.btnPlay.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                binding.btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
            } else {
                mediaPlayer.pause()
                binding.btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
        }

        binding.btnNext.setOnClickListener {
            handler.removeCallbacks(updater)
            mediaPlayer.release()
            prepareMediaPlayer(if(position==tracklist.size-1)tracklist[0].also { position = 0 }else tracklist[++position])
            binding.btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
        binding.btnPrev.setOnClickListener{
            handler.removeCallbacks(updater)
            mediaPlayer.release()
            prepareMediaPlayer(if(position==0)tracklist[tracklist.size-1].also { position = tracklist.size-1 }else tracklist[--position])
            binding.btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }


        binding.sbMain?.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, pos: Int, chanded: Boolean) {
                if (chanded){
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
        prepareMediaPlayer(tracklist[0])
    }

    private fun prepareMediaPlayer(track: Track){
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(track.trackUri)
            mediaPlayer.prepareAsync()
            binding.tvTimer?.text = millisecondsToTimer(mediaPlayer.duration.toLong())
            binding.tvTitle?.text = track.title
            binding.tvAuthor.text = track.artist
            Glide.with(this)
                .load(track.bitmapUri)
                .centerCrop()
                .into(binding.ivCover)
            binding.sbMain?.progress = 0
            binding.sbMain?.max = track.duration
            mediaPlayer.setOnBufferingUpdateListener { _, progress ->
                binding.sbMain?.secondaryProgress = progress
            }
            updater = Runnable {
                binding.sbMain?.progress = mediaPlayer.currentPosition
                binding.tvTimer?.text = millisecondsToTimer(mediaPlayer.currentPosition.toLong())
                handler.postDelayed(updater, 1000)
            }
            handler.postDelayed(updater, 1000)

        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun loadTracks():List<Track>{
        val inputStream:InputStream = resources.openRawResource(R.raw.tracklist)
        val scanner = Scanner(inputStream)
        val builder = StringBuilder()
        while (scanner.hasNextLine()){
            builder.append(scanner.nextLine())
        }

        return getTracklistFromJson(builder.toString())
    }

    private fun getTracklistFromJson(s:String):List<Track>{
        val gson = Gson()
        val listType:Type = object :TypeToken<List<Track>>(){}.type
        return gson.fromJson(s, listType)
    }

    fun millisecondsToTimer(milliseconds: Long):String{
        var timerString = ""
        var secondString = ""
        val hours = (milliseconds/(1000*60*60)).toInt()
        val minutes = ((milliseconds%(1000*60*60))/(1000*60)).toInt()
        val seconds = ((milliseconds % (1000 * 60 * 60)) % (1000*60)/1000).toInt()

        if(hours>0){
            timerString = "$hours:"
        }
        if(seconds<10){
            secondString = "0$seconds"
        }else{
            secondString = "$seconds"
        }
        timerString = "$timerString$minutes:$secondString"
        return timerString
    }
}