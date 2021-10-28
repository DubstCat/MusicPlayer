package com.example.musicplayer.data

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.musicplayer.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.lang.reflect.Type
import java.util.Scanner

class MediaViewModel(val context: Application) : AndroidViewModel(context) {


    var currentTrack = MutableLiveData<Track>()
    var currentTime = MutableLiveData<Int>()
    var tracklist: List<Track>
    var position: Int

    var mediaPlayer:MediaPlayer = MediaPlayer()

    init {
        tracklist = loadTracks()
        position = 0
    }

    private fun loadTracks(): List<Track> {
        val inputStream: InputStream = context.resources.openRawResource(R.raw.tracklist)
        val scanner = Scanner(inputStream)
        val builder = StringBuilder()
        while (scanner.hasNextLine()) {
            builder.append(scanner.nextLine())
        }
        return getTracklistFromJson(builder.toString())
    }

    private fun getTracklistFromJson(s: String): List<Track> {
        val gson = Gson()
        val listType: Type = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(s, listType)
    }



    fun millisecondsToTimer(milliseconds: Long): String {
        var timerString = ""
        var secondString = ""
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = ((milliseconds % (1000 * 60 * 60)) / (1000 * 60)).toInt()
        val seconds = ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000).toInt()

        if (hours > 0) {
            timerString = "$hours:"
        }
        if (seconds < 10) {
            secondString = "0$seconds"
        } else {
            secondString = "$seconds"
        }
        timerString = "$timerString$minutes:$secondString"
        return timerString
    }

    val isPlaying:Boolean
    get() = mediaPlayer.isPlaying

    val currentPosition:Int
    get() = mediaPlayer.currentPosition
}