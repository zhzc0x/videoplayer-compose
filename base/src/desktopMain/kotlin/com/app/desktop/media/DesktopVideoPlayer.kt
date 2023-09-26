package com.app.desktop.media

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.media.MediaView
import javafx.util.Duration
import kotlinx.coroutines.*
import java.io.File
import java.util.*

private val jfxPanel = JFXPanel()
private val mediaView = MediaView().apply {
    Platform.runLater {
        initJFXPanelScene()
    }
}
private var currentUrl = ""

@Composable
internal actual fun VideoPlayerImpl(modifier: Modifier, url: String, state: MutableState<MediaState>,
                                    volume: Float, seek: Float): State<Progress>{
    if(currentUrl != url){
        currentUrl = url
        if(mediaView.mediaPlayer != null){
            state.value = MediaState.NON
            mediaView.mediaPlayer.dispose()
            mediaView.mediaPlayer = null
        }
        if(currentUrl.isNotEmpty()){
            state.value = MediaState.LOADING
            Platform.runLater {
                loadMediaComponent(currentUrl, state)
            }
        }
    }
    LaunchedEffect(state.value){
        when (state.value) {
            MediaState.START -> {
                mediaView.mediaPlayer.play()
            }
            MediaState.RESTART -> {
                mediaView.mediaPlayer.seek(Duration.ZERO)
                state.value = MediaState.PLAYING
            }
            MediaState.PAUSE -> {
                mediaView.mediaPlayer.pause()
            }
            else -> {}
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            currentUrl = ""
            mediaView.mediaPlayer.dispose()
        }
    }
    LaunchedEffect(seek) {
        mediaView.mediaPlayer.seek(Duration(seek * mediaView.mediaPlayer.totalDuration.toMillis()))
        if(state.value == MediaState.PAUSE){
            mediaView.mediaPlayer.pause()
        } else if(state.value == MediaState.END){
            state.value = MediaState.PLAYING
        }
    }
    LaunchedEffect(volume) {
        mediaView.mediaPlayer.volume = volume.toDouble()
    }
    SwingPanel(Color.Transparent, factory = {
        jfxPanel
    }, modifier.onGloballyPositioned{ layoutCoordinates ->
        println("onGloballyPositioned: ${layoutCoordinates.size}")
    })
    return mediaView.mediaPlayer?.produceProgressFor(state.value, seek) ?: mutableStateOf(Progress.ZERO)
}

private fun initJFXPanelScene(){
    val pane = BorderPane()
    val scene = Scene(pane).apply {
        fill = javafx.scene.paint.Color.BLACK
    }
    mediaView.fitWidthProperty().bind(pane.widthProperty())
    mediaView.fitHeightProperty().bind(pane.heightProperty())
    pane.center = mediaView
    jfxPanel.scene = scene
}

private fun loadMediaComponent(url: String, state: MutableState<MediaState>) {
    println("loadMediaComponent: url=$url")
    val media = try {
        if(url.startsWith("http")){
            Media(url)
        } else {
            Media(File(url).toURI().toString())
        }
    } catch (ex: Exception){
        state.value = MediaState.ERROR
        null
    } ?: return

    val mediaPlayer = MediaPlayer(media)
    mediaView.mediaPlayer = mediaPlayer
    mediaPlayer.setOnReady {
        println("onReady: videoWidth=${mediaPlayer.media.width}, videoHeight=${mediaPlayer.media.height}")
        state.value = MediaState.READY
    }
    mediaPlayer.setOnPlaying {
        println("onPlaying")
        state.value = MediaState.PLAYING
    }
    mediaPlayer.setOnPaused {
        println("onPaused")
    }
    mediaPlayer.setOnEndOfMedia {
        println("onEndOfMedia")
        state.value = MediaState.END
    }
    mediaPlayer.setOnError {
        println("onError")
        state.value = MediaState.ERROR
    }
}

@Composable
private fun MediaPlayer.produceProgressFor(state: MediaState, seek: Float) =
    produceState(Progress(0f, 0L), state, seek) {
        do {
            if(totalDuration != Duration.UNKNOWN){
                val currentTime = currentTime.toMillis()
                val totalDuration = totalDuration.toMillis()
                value = Progress((currentTime / totalDuration).toFloat(), totalDuration.toLong())
            }
            delay(100)
        } while (state == MediaState.PLAYING)
    }

private fun isMacOS(): Boolean {
    val os = System.getProperty("os.name", "generic").lowercase(Locale.ENGLISH)
    return os.indexOf("mac") >= 0 || os.indexOf("darwin") >= 0
}