package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.data.model.MediaType
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.AudioPlayerSheet
import com.example.ui.components.NowPlayingMiniBar
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MediaDetailScreen
import com.example.ui.screens.ServerSelectScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.JellyDarkBg
import com.example.ui.theme.JellyStreamTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JellyStreamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = JellyDarkBg
                ) {
                    JellyStreamApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun JellyStreamApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val playerState by viewModel.playerUiState.collectAsState()

    // Handle Android system back gestures
    BackHandler(enabled = currentScreen !is AppScreen.Home || playerState.isAudioPlayerExpanded) {
        viewModel.handleBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(JellyDarkBg)) {
        // Main Screen Switcher
        when (val screen = currentScreen) {
            is AppScreen.Home -> {
                HomeScreen(viewModel = viewModel)
            }
            is AppScreen.Library -> {
                LibraryScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.handleBack() }
                )
            }
            is AppScreen.MediaDetail -> {
                MediaDetailScreen(
                    item = screen.item,
                    viewModel = viewModel,
                    onBack = { viewModel.handleBack() }
                )
            }
            is AppScreen.VideoPlayer -> {
                VideoPlayerScreen(
                    item = screen.item,
                    playerManager = viewModel.playerManager,
                    onBack = { viewModel.handleBack() }
                )
            }
            is AppScreen.ServerManager -> {
                ServerSelectScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.handleBack() }
                )
            }
            is AppScreen.Settings -> {
                SettingsScreen(
                    onBack = { viewModel.handleBack() },
                    playerManager = viewModel.playerManager,
                    viewModel = viewModel
                )
            }
        }

        // Persistent Mini Player (shown whenever a track/video is loaded, except in VideoPlayerScreen)
        val showMiniPlayer = playerState.currentItem != null &&
                currentScreen !is AppScreen.VideoPlayer &&
                !playerState.isAudioPlayerExpanded

        AnimatedVisibility(
            visible = showMiniPlayer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            NowPlayingMiniBar(
                playerState = playerState,
                onBarClick = {
                    val item = playerState.currentItem ?: return@NowPlayingMiniBar
                    if (item.type == MediaType.AUDIO) {
                        viewModel.playerManager.setAudioPlayerExpanded(true)
                    } else {
                        viewModel.navigateTo(AppScreen.VideoPlayer(item))
                    }
                },
                onTogglePlayPause = { viewModel.playerManager.togglePlayPause() },
                onNext = { viewModel.playerManager.playNextInQueue() },
                onDismiss = { viewModel.playerManager.stopAndClear() }
            )
        }

        // Fullscreen Audio Player Sheet
        AnimatedVisibility(
            visible = playerState.isAudioPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            AudioPlayerSheet(
                playerState = playerState,
                onClose = { viewModel.playerManager.setAudioPlayerExpanded(false) },
                onTogglePlayPause = { viewModel.playerManager.togglePlayPause() },
                onSeekTo = { viewModel.playerManager.seekTo(it) },
                onNext = { viewModel.playerManager.playNextInQueue() },
                onPrevious = { viewModel.playerManager.playPreviousInQueue() },
                onSetSleepTimer = { viewModel.playerManager.setSleepTimer(it) },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onSelectQueueItem = { viewModel.playMedia(it, playerState.queue) }
            )
        }
    }
}
