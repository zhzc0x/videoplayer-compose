package com.app.desktop.media

import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import java.io.File

class MusicPlayer {

    private var mediaPlayer: MediaPlayer? = null

    fun load(url: String, onReady: (() -> Unit)? = null){
        dispose()
        val media = try {
            if(url.startsWith("http")){
                Media(url)
            } else {
                Media(File(url).toURI().toString())
            }
        } catch (ex: Exception){
            null
        } ?: return
        mediaPlayer = MediaPlayer(media)
        mediaPlayer!!.setOnReady {
            onReady?.invoke()
        }
    }

    fun setVolume(volume: Float){
        mediaPlayer?.volume = volume.toDouble()
    }

    fun play(){
        mediaPlayer?.play()
    }

    fun pause(){
        mediaPlayer?.pause()
    }

    fun dispose(){
        mediaPlayer?.dispose()
    }

}