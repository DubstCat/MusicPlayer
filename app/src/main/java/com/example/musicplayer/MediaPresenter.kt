package com.example.musicplayer

import android.content.Context
import android.content.res.Resources
import android.media.MediaPlayer
import android.os.Handler
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

class MediaPresenter(val context:Context, val resources: Resources):Playable {

    private lateinit var updater:Runnable
    lateinit var mediaPlayer: MediaPlayer
    lateinit var binding:ActivityMainBinding
    lateinit var handler:Handler
    var tracklist:List<Track>
    var position:Int

    init {
        tracklist = loadTracks()
        position = 0
    }

    private fun loadTracks():List<Track>{
        val inputStream: InputStream = resources.openRawResource(R.raw.tracklist)
        val scanner = Scanner(inputStream)
        val builder = StringBuilder()
        while (scanner.hasNextLine()){
            builder.append(scanner.nextLine())
        }

        return getTracklistFromJson(builder.toString())
    }

    private fun getTracklistFromJson(s:String):List<Track>{
        val gson = Gson()
        val listType: Type = object : TypeToken<List<Track>>(){}.type
        return gson.fromJson(s, listType)
    }

    private fun prepareMediaPlayer(track: Track){
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(track.trackUri)
            mediaPlayer.prepareAsync()
            binding.tvTimer?.text = millisecondsToTimer(mediaPlayer.duration.toLong())
            binding.tvTitle?.text = track.title
            binding.tvAuthor.text = track.artist
            Glide.with(context)
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

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun isPlaying():Boolean = mediaPlayer.isPlaying

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

    fun seekTo(i:Int){
        mediaPlayer.seekTo(i)
    }

    fun initPlayer(){
        prepareMediaPlayer(tracklist[0])
    }

    override fun onBtnPlay() {
        mediaPlayer.start()
    }

    override fun onBtnPause() {
        mediaPlayer.pause()
    }

    override fun onBtnNext() {
        handler.removeCallbacks(updater)
        mediaPlayer.release()
        prepareMediaPlayer(if(position==tracklist.size-1)tracklist[0].also { position = 0 }else tracklist[++position])
    }

    override fun onBtnPrev() {
        handler.removeCallbacks(updater)
        mediaPlayer.release()
        prepareMediaPlayer(if(position==0)tracklist[tracklist.size-1].also { position = tracklist.size-1 }else tracklist[--position])
    }


}