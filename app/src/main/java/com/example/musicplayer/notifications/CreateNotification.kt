package com.example.musicplayer.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.musicplayer.R
import com.example.musicplayer.data.Track

object CreateNotification {
    const val CHANNEL_ID = "channel1"
    val ACTION_PREVIOUS = "ACTION_PREVIOUS"
    val ACTION_PLAY = "ACTION_PLAY"
    val ACTION_NEXT = "ACTION_NEXT"

    lateinit var notification: Notification

    fun createNotification(context: Context, track: Track) {
        val notificationManagerCompat = NotificationManagerCompat.from(context.applicationContext)
        val mediaSession = MediaSessionCompat(context, "tag")

        val intentPrevious = Intent(context, NotificationActionService::class.java)
            .setAction(ACTION_PREVIOUS)
        val pendingIntentPrevious = PendingIntent.getBroadcast(
            context, 0,
            intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val drwPrevious = R.drawable.ic_baseline_skip_previous_24

        val intentPlay = Intent(context, NotificationActionService::class.java)
            .setAction(ACTION_PLAY)
        val pendingIntentPlay = PendingIntent.getBroadcast(
            context, 0,
            intentPlay, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val drwPlay = R.drawable.ic_baseline_play_arrow_24

        val intentNext = Intent(context, NotificationActionService::class.java)
            .setAction(ACTION_NEXT)
        val pendingIntentNext = PendingIntent.getBroadcast(
            context, 0,
            intentNext, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val drwNext = R.drawable.ic_baseline_skip_next_24

        val token = mediaSession.sessionToken

        notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(track.title)
            .setContentText(track.artist)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(drwPrevious, "Previous", pendingIntentPrevious)
            .addAction(drwPlay, "Play", pendingIntentPlay)
            .addAction(drwNext, "Next", pendingIntentNext)
            .setSmallIcon(R.drawable.ic_baseline_play_arrow_24)
            .setChannelId(CHANNEL_ID)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(token)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManagerCompat.notify(null, 0, notification)
    }
}