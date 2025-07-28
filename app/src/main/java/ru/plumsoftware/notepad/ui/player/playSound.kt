package ru.plumsoftware.notepad.ui.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

fun playSound(context: Context, exoPlayer: ExoPlayer, soundResId: Int) {
    exoPlayer.apply {
        setMediaItem(MediaItem.fromUri("android.resource://${context.packageName}/$soundResId"))
        prepare()
        play()
    }
}