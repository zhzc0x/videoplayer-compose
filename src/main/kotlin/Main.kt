import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.app.desktop.image.Blank
import com.app.desktop.image.animate
import com.app.desktop.image.loadAnimatedImage
import com.app.desktop.image.loadResourceAnimatedImage
import com.app.desktop.media.MediaState
import com.app.desktop.media.MusicPlayer
import com.app.desktop.media.Progress
import com.app.desktop.media.VideoPlayer
import com.app.desktop.resource.LoadState
import com.app.desktop.resource.load
import com.app.desktop.resource.loadOrNull
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.swing.JFileChooser
import javax.swing.JPanel
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.filechooser.FileSystemView


private val VIDEO_URL_LIST = arrayListOf("raw/1.mp4", "raw/2.mp4", "raw/3.mp4", "raw/240p.mp4", "raw/480p.mp4", "raw/720p.mp4",
    "https://sf1-hscdn-tos.pstatp.com/obj/media-fe/xgplayer_doc_video/mp4/xgplayer-demo-720p.mp4")

fun formatSeconds(seconds: Long): String {
    val hour = seconds / (60 * 60)
    val minute = seconds / 60
    val toSecond = seconds % 60
    return if(hour <= 0){
        "${minute.toDoubleString()}:${toSecond.toDoubleString()}"
    } else {
        "${hour.toDoubleString()}:${minute.toDoubleString()}:${toSecond.toDoubleString()}"
    }
}

fun Long.toDoubleString(): String{
    return if(this > 9){
        this.toString()
    } else {
        "0$this"
    }
}

fun main() = singleWindowApplication(state= WindowState(position= WindowPosition.Aligned(Alignment.Center)),
        title = "MediaPlayer") {
    VideoPlayer()
//    AnimatedImage()
    LaunchedEffect(showChooseVideoFile){
        if(showChooseVideoFile){
            chooseVideoFile(window)
        }
    }
}

private val mediaState = mutableStateOf(MediaState.NON)
private var volume by mutableStateOf(1f)
private var videoUrl by mutableStateOf(VIDEO_URL_LIST[0])
private var videoUrlIndex = 0
private var showChooseVideoFile by mutableStateOf(false)

@Composable
private fun VideoPlayer() {
    Column(Modifier.fillMaxSize(), horizontalAlignment=Alignment.CenterHorizontally){
        var seek by remember { mutableStateOf(0f) }
        var progress by remember { mutableStateOf(Progress.ZERO) }
        var showLoading by remember{ mutableStateOf(false) }
        var showErrorHint by remember{ mutableStateOf(false) }
        var videoModifier by remember { mutableStateOf(Modifier.size(DpSize.Zero)) }
        Box(Modifier.fillMaxWidth().weight(1f).background(Color.Black), contentAlignment= Alignment.Center) {
            progress = VideoPlayer(videoModifier, videoUrl, mediaState, volume, seek).value
            if(showLoading){
                CircularProgressIndicator(Modifier.size(80.dp), Color.White, strokeWidth = 6.dp)
            }
            if(showErrorHint){
                Column(Modifier.clickable {
                    MainScope().launch {
                        videoUrl = ""
                        delay(500)
                        videoUrl = VIDEO_URL_LIST[videoUrlIndex]
                    }
                }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Refresh, "", Modifier.size(60.dp), tint = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Text("加载失败", color=Color.White, fontSize = 18.sp)
                }
            }
        }
        Row(Modifier.padding(start = 16.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(formatSeconds((progress.duration * progress.fraction).toLong() / 1000), color = Color.Black)
            Slider(value = progress.fraction,
                onValueChange = { seek = it },
                modifier = Modifier.weight(1f),
                colors =  SliderDefaults.colors(thumbColor= Color.Black, activeTrackColor=Color.Black))
            Text(formatSeconds(progress.duration / 1000), color = Color.Black)
        }
        ControlLayout()
        Spacer(Modifier.height(16.dp))
        LaunchedEffect(mediaState.value){
            println("out mediaState=${mediaState.value}")
            when (mediaState.value) {
                MediaState.LOADING -> {
                    showLoading = true
                    showErrorHint = false
                    //由于覆盖在JavaFx组件上面的UI无法显示出来，所以先将其尺寸设置为0，等加载完成后恢复
                    videoModifier = Modifier.size(DpSize.Zero)
                }
                MediaState.READY -> {
                    delay(1000)
                    showLoading = false
                    videoModifier = Modifier.fillMaxSize()
                }
                MediaState.ERROR -> {
                    showErrorHint = true
                    showLoading = false
                    videoModifier = Modifier.size(DpSize.Zero)
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun ControlLayout() = Row(Modifier.fillMaxWidth()) {
    Spacer(Modifier.width(16.dp))
    val playText by remember(mediaState.value) {
        mutableStateOf(
            if (mediaState.value == MediaState.END) {
                "重播"
            } else {
                "播放"
            }
        )
    }
    Button(onClick = {
        if (playText == "播放") {
            mediaState.value = MediaState.START
        } else {
            mediaState.value = MediaState.RESTART
        }
    }) {
        Text(playText)
    }
    Spacer(Modifier.width(16.dp))
    Button(onClick = {
        videoUrlIndex = if(videoUrlIndex == 0){
            VIDEO_URL_LIST.size - 1
        } else {
            videoUrlIndex - 1
        }
        videoUrl = VIDEO_URL_LIST[videoUrlIndex]
    }) {
        Text("上一个")
    }
    Spacer(Modifier.width(16.dp))
    Button(onClick = {
        videoUrlIndex = if(videoUrlIndex < VIDEO_URL_LIST.size - 1){
            videoUrlIndex + 1
        } else {
            0
        }
        videoUrl = VIDEO_URL_LIST[videoUrlIndex]
    }) {
        Text("下一个")
    }
    Spacer(Modifier.width(16.dp))
    Button(onClick = {
        mediaState.value = MediaState.PAUSE
    }) {
        Text("暂停", Modifier)
    }
    Spacer(Modifier.width(16.dp))
    Slider(
        value = volume,
        onValueChange = {
            volume = it
            println("volume=$volume")
        },
        modifier = Modifier.width(100.dp)
    )
    Spacer(Modifier.width(16.dp))
    Button(onClick = {
        showChooseVideoFile = true
    }) {
        Text("选择本地视频", Modifier)
    }
    Spacer(Modifier.weight(1f))
    var playMusic by remember { mutableStateOf(false) }
    val musicPlayer = remember { MusicPlayer() }
    Button(onClick = {
        playMusic = !playMusic
        if(playMusic){
            musicPlayer.load("raw/houlai.mp3"){
                musicPlayer.play()
            }
        } else {
            musicPlayer.dispose()
        }
    }) {
        Text("播放音乐", Modifier)
    }
    Spacer(Modifier.width(16.dp))
}

private fun chooseVideoFile(window: ComposeWindow) {
    val fileChooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory)
    val filter = FileNameExtensionFilter("Video", "mp4", "3gp", "flv")
    fileChooser.dialogTitle = "选择本地视频"
    fileChooser.fileFilter = filter
    val returnVal = fileChooser.showOpenDialog(window)
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        val selectedPath = fileChooser.selectedFile.path
        VIDEO_URL_LIST.add(selectedPath)
        videoUrlIndex = VIDEO_URL_LIST.size - 1
        videoUrl = VIDEO_URL_LIST[videoUrlIndex]
    }
    showChooseVideoFile = false
}

private const val gifUrl = "https://raw.githubusercontent.com/JetBrains/skija/ccf303ebcf926e5ef000fc42d1a6b5b7f1e0b2b5/examples/scenes/images/codecs/animated.gif"
@Composable
private fun AnimatedImage(){
    Column {
        // Load an image async
        // use "load { loadResourceAnimatedImage(url) }" for resources
        when (val animatedImage = load { loadAnimatedImage(gifUrl) }) {
            is LoadState.Success -> Image(
                bitmap = animatedImage.value.animate(),
                contentDescription = null,
            )
            is LoadState.Loading -> CircularProgressIndicator()
            is LoadState.Error -> Text("Error!")
        }

        Image(
            loadOrNull { loadResourceAnimatedImage("demo.webp") }?.animate() ?: ImageBitmap.Blank,
            contentDescription = null,
            Modifier.size(100.dp)
        )
    }
}