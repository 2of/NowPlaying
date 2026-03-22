package com.noahk.NowPlayingCarView

import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noahk.NowPlayingCarView.ui.theme.NowPlayingCarViewTheme

fun sendMediaKey(context: Context, keyCode: Int) {
    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
    am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide status and navigation bars for immersive full-screen experience
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            NowPlayingCarViewTheme {
                PlayerHome()
            }
        }
    }
}

@Composable
fun PlayerHome() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var hasPermission by remember {
        mutableStateOf(MediaPermissionHelper.hasNotificationAccess(context))
    }

    val track by MediaListenerService.trackState.collectAsStateWithLifecycle()
    val isPlaying by MediaListenerService.isPlaying.collectAsStateWithLifecycle()

    if (!hasPermission) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Media Access Required") },
            text = { Text("Grant notification access so the player can read what's playing.") },
            confirmButton = {
                TextButton(onClick = {
                    MediaPermissionHelper.openNotificationAccessSettings(context)
                    hasPermission = MediaPermissionHelper.hasNotificationAccess(context)
                }) { Text("Open Settings") }
            }
        )
    }

    val onPlayPause: () -> Unit = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) }
    val onPrev: () -> Unit      = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS) }
    val onNext: () -> Unit      = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NowPlayingPanel(
                    track = track,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Column(
                    modifier = Modifier.weight(1.2f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ControlsPanel(
                        isPlaying = isPlaying,
                        hasTrack = track != null,
                        onPlayPause = onPlayPause,
                        onPrev = onPrev,
                        onNext = onNext,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                    ScreenControls(modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NowPlayingPanel(
                    track = track,
                    modifier = Modifier.weight(2f).fillMaxWidth()
                )
                ControlsPanel(
                    isPlaying = isPlaying,
                    hasTrack = track != null,
                    onPlayPause = onPlayPause,
                    onPrev = onPrev,
                    onNext = onNext,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
                ScreenControls(modifier = Modifier.fillMaxWidth().height(44.dp))
            }
        }
    }
}