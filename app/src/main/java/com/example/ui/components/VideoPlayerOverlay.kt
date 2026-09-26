package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MediaItem
import com.example.player.NetworkQualityTier
import com.example.player.PlayerUiState
import com.example.player.QualityPreset
import com.example.player.VideoScaleMode
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JellyAmber
import com.example.ui.theme.JellyCyan
import com.example.ui.theme.JellyDarkBg
import com.example.ui.theme.JellyGreen
import com.example.ui.theme.JellyPink
import com.example.ui.theme.JellyPurplePrimary
import com.example.ui.theme.JellySurface
import com.example.ui.theme.JellySurfaceElevated
import com.example.ui.theme.PlayerControlsBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerOverlay(
    item: MediaItem,
    playerState: PlayerUiState,
    onBack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onScaleModeChange: (VideoScaleMode) -> Unit,
    onToggleLock: () -> Unit,
    onToggleFullScreen: () -> Unit,
    onNextEpisode: () -> Unit,
    onQualityChange: (QualityPreset) -> Unit = {},
    onToggleDataSaver: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }

    // Auto-hide controls after 4.5 seconds of playback
    LaunchedEffect(controlsVisible, playerState.isPlaying, showQualityDialog, showSpeedMenu) {
        if (controlsVisible && playerState.isPlaying && !showQualityDialog && !showSpeedMenu) {
            delay(4500L)
            controlsVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                controlsVisible = !controlsVisible
            }
            .testTag("video_player_overlay")
    ) {
        // Locked mode indicator
        if (playerState.isControlsLocked) {
            AnimatedVisibility(
                visible = controlsVisible,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(20.dp)
            ) {
                IconButton(
                    onClick = onToggleLock,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC000000))
                        .testTag("video_unlock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Unlock Controls",
                        tint = JellyCyan
                    )
                }
            }
            return@Box
        }

        // Buffering indicator
        if (playerState.isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.Center),
                color = JellyCyan,
                strokeWidth = 3.dp
            )
        }

        // Dynamic Quality / Speed Notification Toast
        AnimatedVisibility(
            visible = playerState.qualityNotification != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp)
        ) {
            playerState.qualityNotification?.let { msg ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xEE1E1538))
                        .border(1.dp, JellyCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = null,
                        tint = JellyCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Full Controls HUD
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PlayerControlsBg)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (playerState.isFullScreen) Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            else Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back + Title + Live Speed Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("video_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val sub = if (item.seriesName != null) {
                                    "${item.seriesName} • S${item.seasonNumber ?: 1}:E${item.episodeNumber ?: 1}"
                                } else {
                                    "${item.year ?: ""} • ${item.resolutionTag}"
                                }
                                Text(
                                    text = sub,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary
                                    )
                                )

                                // Live Speed & Quality Pill in HUD
                                if (playerState.showSpeedInPlayer) {
                                    val tierColor = when (playerState.networkTier) {
                                        NetworkQualityTier.EXCELLENT -> JellyGreen
                                        NetworkQualityTier.GOOD -> JellyCyan
                                        NetworkQualityTier.MODERATE -> JellyAmber
                                        NetworkQualityTier.FAIR -> JellyAmber
                                        NetworkQualityTier.LOW, NetworkQualityTier.OFFLINE -> JellyPink
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0x40000000))
                                            .border(1.dp, tierColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                            .testTag("video_speed_indicator")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(tierColor)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${playerState.formattedSpeed} • ${playerState.effectiveQuality.shortLabel}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Top Right Action Buttons: Adaptive Quality, Aspect Ratio, Speed, Lock
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Adaptive Quality Selector Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x3300E5FF))
                                .border(1.dp, JellyCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable { showQualityDialog = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("video_quality_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Video Quality",
                                tint = JellyCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val qualityLabel = if (playerState.selectedQuality == QualityPreset.AUTO) {
                                "Auto (${playerState.effectiveQuality.shortLabel})"
                            } else {
                                playerState.selectedQuality.shortLabel
                            }
                            Text(
                                text = qualityLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Aspect ratio toggle
                        IconButton(
                            onClick = {
                                val nextMode = when (playerState.scaleMode) {
                                    VideoScaleMode.FIT -> VideoScaleMode.FILL
                                    VideoScaleMode.FILL -> VideoScaleMode.ZOOM
                                    VideoScaleMode.ZOOM -> VideoScaleMode.FIT
                                }
                                onScaleModeChange(nextMode)
                            },
                            modifier = Modifier.testTag("video_aspect_ratio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = "Aspect Ratio: ${playerState.scaleMode.name}",
                                tint = TextPrimary
                            )
                        }

                        // Playback Speed dropdown
                        Box {
                            IconButton(
                                onClick = { showSpeedMenu = true },
                                modifier = Modifier.testTag("video_speed_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Speed",
                                    tint = if (playerState.playbackSpeed != 1.0f) JellyCyan else TextPrimary
                                )
                            }

                            DropdownMenu(
                                expanded = showSpeedMenu,
                                onDismissRequest = { showSpeedMenu = false },
                                modifier = Modifier.background(JellySurfaceElevated)
                            ) {
                                listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${spd}x",
                                                color = if (playerState.playbackSpeed == spd) JellyCyan else TextPrimary,
                                                fontWeight = if (playerState.playbackSpeed == spd) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            onSpeedChange(spd)
                                            showSpeedMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Lock controls button
                        IconButton(
                            onClick = onToggleLock,
                            modifier = Modifier.testTag("video_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Lock",
                                tint = TextPrimary
                            )
                        }
                    }
                }

                // Center Transport Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = onSeekBackward,
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("video_rewind_10_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Large Play / Pause button
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(JellyPurplePrimary, Color(0xFF6366F1))
                                )
                            )
                            .clickable { onTogglePlayPause() }
                            .testTag("video_center_play_pause"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = onSeekForward,
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("video_forward_10_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Bottom Bar: Progress Slider + Fullscreen toggle
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (playerState.isFullScreen) Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
                            else Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                        .align(Alignment.BottomCenter)
                ) {
                    val durationMs = playerState.durationMs.coerceAtLeast(1L)
                    val currentPos = playerState.currentPositionMs.coerceIn(0L, durationMs)

                    Slider(
                        value = currentPos.toFloat(),
                        onValueChange = { onSeekTo(it.toLong()) },
                        valueRange = 0f..durationMs.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = JellyCyan,
                            activeTrackColor = JellyCyan,
                            inactiveTrackColor = Color(0x44FFFFFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_seek_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatTime(currentPos)} / ${formatTime(durationMs)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (playerState.queue.size > 1) {
                                IconButton(
                                    onClick = onNextEpisode,
                                    modifier = Modifier.size(36.dp).testTag("video_next_episode_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Next Episode",
                                        tint = TextPrimary
                                    )
                                }
                            }

                            IconButton(
                                onClick = onToggleFullScreen,
                                modifier = Modifier.size(36.dp).testTag("video_fullscreen_button")
                            ) {
                                Icon(
                                    imageVector = if (playerState.isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = if (playerState.isFullScreen) "Exit Fullscreen" else "Enter Fullscreen",
                                    tint = if (playerState.isFullScreen) JellyCyan else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Adaptive Video Quality Modal Dialog
        if (showQualityDialog) {
            VideoQualityDialog(
                playerState = playerState,
                onDismiss = { showQualityDialog = false },
                onSelectQuality = { quality ->
                    onQualityChange(quality)
                    showQualityDialog = false
                },
                onToggleDataSaver = onToggleDataSaver
            )
        }
    }
}

@Composable
fun VideoQualityDialog(
    playerState: PlayerUiState,
    onDismiss: () -> Unit,
    onSelectQuality: (QualityPreset) -> Unit,
    onToggleDataSaver: (Boolean) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .testTag("video_quality_dialog"),
            colors = CardDefaults.cardColors(containerColor = JellySurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = JellyCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Video Stream Quality",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Network Speed Diagnostics Card
                val tierColor = when (playerState.networkTier) {
                    NetworkQualityTier.EXCELLENT -> JellyGreen
                    NetworkQualityTier.GOOD -> JellyCyan
                    NetworkQualityTier.MODERATE -> JellyAmber
                    NetworkQualityTier.FAIR -> JellyAmber
                    NetworkQualityTier.LOW, NetworkQualityTier.OFFLINE -> JellyPink
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, tierColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1433)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (playerState.connectionType.contains("Cellular", ignoreCase = true)) {
                                    Icons.Default.SignalCellularAlt
                                } else {
                                    Icons.Default.Wifi
                                },
                                contentDescription = null,
                                tint = tierColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${playerState.connectionType} • ${playerState.formattedSpeed}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = playerState.networkTier.label,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = tierColor,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Current Active Output Badge
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Current Feed",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = playerState.effectiveQuality.resolutionName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = JellyCyan
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quality Options
                QualityPreset.values().forEach { preset ->
                    val isSelected = playerState.selectedQuality == preset
                    QualityOptionRow(
                        preset = preset,
                        isSelected = isSelected,
                        currentEffective = playerState.effectiveQuality,
                        onClick = { onSelectQuality(preset) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cellular Data Saver Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF18122B))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mobile Data Saver",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Limit auto quality to 720p on cellular networks",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Switch(
                        checked = playerState.isCellularDataSaver,
                        onCheckedChange = onToggleDataSaver,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = JellyCyan,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = JellyDarkBg
                        ),
                        modifier = Modifier.testTag("video_data_saver_switch")
                    )
                }
            }
        }
    }
}

@Composable
fun QualityOptionRow(
    preset: QualityPreset,
    isSelected: Boolean,
    currentEffective: QualityPreset,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) Color(0xFF2E1F54) else Color(0x331E1736)
    val borderColor = if (isSelected) JellyCyan else Color(0x22FFFFFF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("quality_item_${preset.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = preset.label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) JellyCyan else TextPrimary
                    )
                )

                if (preset == QualityPreset.AUTO) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Playing: ${currentEffective.resolutionName}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = JellyGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier
                            .background(JellyGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = preset.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = JellyCyan,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
