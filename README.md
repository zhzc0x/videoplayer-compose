# Compose desktop video player

## 基于JavaFx组件MediaPlayer开发，独立运行，不依赖任何其它视频播放器

![](https://github.com/zhzc0x/compose-video-player/blob/master/raw/demo1.png)

![](https://github.com/zhzc0x/compose-video-player/blob/master/raw/demo2.png)

## 代码示例

```kotlin
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
```

