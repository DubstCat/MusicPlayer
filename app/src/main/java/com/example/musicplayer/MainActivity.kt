package com.example.musicplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.data.Track
import com.example.musicplayer.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.lang.StringBuilder
import java.lang.reflect.Type
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var mediaPlayer:MediaPlayer
    var position = 0
    val handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val tracklist = loadTracks()

        mediaPlayer = MediaPlayer()

        binding.btnPlay.setOnClickListener{
            if(mediaPlayer.isPlaying){
                handler.removeCallbacks(updater)
                mediaPlayer.pause()
                binding.btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
            }else{
                mediaPlayer.start()
                binding.btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                updateSeekBar()
            }
            binding.btnNext.setOnClickListener{
                mediaPlayer.setDataSource(tracklist[++position].trackUri)
                mediaPlayer.prepare()
                binding.tvTimer?.text = millisecondsToTimer(mediaPlayer.duration.toLong())
            }
        }
    }



    fun loadTracks():List<Track>{
        val inputStream:InputStream = resources.openRawResource(R.raw.tracklist)
        val scanner = Scanner(inputStream)
        val builder = StringBuilder()
        while (scanner.hasNextLine()){
            builder.append(scanner.nextLine())
        }

        return getTracklistFromJson(builder.toString())
    }

    fun getTracklistFromJson(s:String):List<Track>{
        val gson = Gson()
        val listType:Type = object :TypeToken<List<Track>>(){}.type
        return gson.fromJson(s, listType)
    }

    private val updater = Runnable {
        kotlin.run {
            updateSeekBar()
            val currendDuration = mediaPlayer.currentPosition
            binding.tvTimer?.text = millisecondsToTimer(currendDuration.toLong())
        }
    }

    private fun updateSeekBar(){
        if(mediaPlayer.isPlaying){
            binding.seekBar.progress = (((mediaPlayer.currentPosition).toFloat()) / (mediaPlayer.duration).toFloat()).toInt()
            handler.postDelayed(updater, 1000)
        }
    }

    fun millisecondsToTimer(milliseconds: Long):String{
        var timerString = ""
        var secondString = ""
        var hours = (milliseconds/(1000*60*60)).toInt()
        var minutes = ((milliseconds%(1000*60*60))/(1000*60)).toInt()
        var seconds = ((milliseconds % (1000 * 60 * 60)) % (1000*60)/1000).toInt()

        if(hours>0){
            timerString = "$hours:"
        }
        if(seconds<10){
            secondString = "0$seconds"
        }else{
            secondString = "$seconds"
        }

        timerString = timerString + minutes + ":" + secondString
        return timerString

    }

}