package com.app.desktop.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier

enum class MediaState{
    NON, LOADING, READY, START, RESTART, PLAYING, PAUSE, END, ERROR
}

data class Progress(val fraction: Float, val duration/* millis */: Long){
    companion object{
        val ZERO = Progress(0f, 0)
    }
}

@Composable
fun VideoPlayer(modifier: Modifier, url: String, state: MutableState<MediaState>, volume: Float = 0.5f,
                seek: Float = 0f): State<Progress>{
    return VideoPlayerImpl(modifier, url, state, volume, seek)
}

@Composable
internal expect fun VideoPlayerImpl(modifier: Modifier, url: String, state: MutableState<MediaState>,
                                    volume: Float, seek: Float): State<Progress>