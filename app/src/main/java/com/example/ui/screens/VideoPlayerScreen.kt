package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.MediaItem
import com.example.player.PlayerManager
import com.example.player.VideoScaleMode
import com.example.ui.components.VideoPlayerOverlay

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    item: MediaItem,
    playerManager: PlayerManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerState by playerManager.uiState.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    // Synchronize true Android immersive fullscreen and landscape orientation
    LaunchedEffect(playerState.isFullScreen) {
        activity?.let { act ->
            val insetsController = WindowCompat.getInsetsController(act.window, act.window.decorView)
            if (playerState.isFullScreen) {
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    // Always restore system bars and portrait/sensor orientation on exit
    DisposableEffect(Unit) {
        onDispose {
            activity?.let { act ->
                val insetsController = WindowCompat.getInsetsController(act.window, act.window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    // Hardware back: exit fullscreen first if active, otherwise navigate back
    BackHandler {
        if (playerState.isFullScreen) {
            playerManager.toggleFullScreen()
        } else {
            onBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("video_player_screen")
    ) {
        // Media3 ExoPlayer View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerManager.exoPlayer
                    useController = false // We provide our modern Compose HUD overlay!
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    resizeMode = when (playerState.scaleMode) {
                        VideoScaleMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        VideoScaleMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        VideoScaleMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                }
            },
            update = { view ->
                view.resizeMode = when (playerState.scaleMode) {
                    VideoScaleMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    VideoScaleMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    VideoScaleMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Glassmorphic Player Controls Overlay
        VideoPlayerOverlay(
            item = item,
            playerState = playerState,
            onBack = {
                if (playerState.isFullScreen) {
                    playerManager.toggleFullScreen()
                } else {
                    onBack()
                }
            },
            onTogglePlayPause = { playerManager.togglePlayPause() },
            onSeekTo = { playerManager.seekTo(it) },
            onSeekForward = { playerManager.seekForward(10000L) },
            onSeekBackward = { playerManager.seekBackward(10000L) },
            onSpeedChange = { playerManager.setPlaybackSpeed(it) },
            onScaleModeChange = { playerManager.setScaleMode(it) },
            onToggleLock = { playerManager.toggleControlsLock() },
            onToggleFullScreen = { playerManager.toggleFullScreen() },
            onNextEpisode = { playerManager.playNextInQueue() },
            onQualityChange = { playerManager.setVideoQuality(it) },
            onToggleDataSaver = { playerManager.setCellularDataSaver(it) },
            modifier = Modifier.fillMaxSize()
        )
    }
}
