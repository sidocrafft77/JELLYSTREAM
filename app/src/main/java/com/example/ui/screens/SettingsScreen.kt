package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.NetworkQualityTier
import com.example.player.PlayerManager
import com.example.player.QualityPreset
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JellyAmber
import com.example.ui.theme.JellyCyan
import com.example.ui.theme.JellyDarkBg
import com.example.ui.theme.JellyGreen
import com.example.ui.theme.JellyPink
import com.example.ui.theme.JellyPurplePrimary
import com.example.ui.theme.JellySurface
import com.example.ui.MainViewModel
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    playerManager: PlayerManager? = null,
    viewModel: MainViewModel? = null,
    modifier: Modifier = Modifier
) {
    var hardwareAcceleration by remember { mutableStateOf(true) }
    var volumeNormalization by remember { mutableStateOf(true) }
    var highBitrateDirectPlay by remember { mutableStateOf(true) }

    val cachedCount = viewModel?.cachedCount?.collectAsState()?.value ?: 0

    val playerUiState = playerManager?.uiState?.collectAsState()?.value

    var selectedDefaultQuality by remember {
        mutableStateOf(playerUiState?.selectedQuality ?: QualityPreset.AUTO)
    }
    var cellularDataSaver by remember {
        mutableStateOf(playerUiState?.isCellularDataSaver ?: false)
    }
    var showSpeedHud by remember {
        mutableStateOf(playerUiState?.showSpeedInPlayer ?: true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JellyDarkBg)
            .statusBarsPadding()
            .testTag("settings_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Settings & Playback",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
            }

            // Real-Time Adaptive Video Quality & Network
            item {
                SettingsSectionTitle("ADAPTIVE VIDEO QUALITY & NETWORK")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = JellySurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Live Network Diagnostics Pill
                        val tierColor = when (playerUiState?.networkTier) {
                            NetworkQualityTier.EXCELLENT -> JellyGreen
                            NetworkQualityTier.GOOD -> JellyCyan
                            NetworkQualityTier.MODERATE -> JellyAmber
                            NetworkQualityTier.FAIR -> JellyAmber
                            NetworkQualityTier.LOW, NetworkQualityTier.OFFLINE, null -> JellyPink
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
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = tierColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${playerUiState?.connectionType ?: "Wi-Fi"} • ${playerUiState?.formattedSpeed ?: "15.0 Mbps"}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = playerUiState?.networkTier?.label ?: "High Speed (1080p Ready)",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = tierColor,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Optimal Quality",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    )
                                    Text(
                                        text = playerUiState?.effectiveQuality?.resolutionName ?: "1080p",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = JellyCyan
                                        )
                                    )
                                }
                            }
                        }

                        // Quality Preset Selection Row
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Default Streaming Quality",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Auto mode continuously adjusts video bitrate depending on internet speed",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    QualityPreset.AUTO,
                                    QualityPreset.UHD_4K,
                                    QualityPreset.FHD_1080P,
                                    QualityPreset.HD_720P,
                                    QualityPreset.SD_480P
                                ).forEach { preset ->
                                    val isSelected = selectedDefaultQuality == preset
                                    val bg = if (isSelected) JellyCyan.copy(alpha = 0.2f) else Color(0xFF1B1530)
                                    val border = if (isSelected) JellyCyan else Color(0x22FFFFFF)
                                    val textColor = if (isSelected) JellyCyan else TextSecondary

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bg)
                                            .border(1.dp, border, RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedDefaultQuality = preset
                                                playerManager?.setVideoQuality(preset)
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = preset.shortLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = textColor,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Cellular Data Saver
                        SettingsToggleRow(
                            icon = Icons.Default.DataSaverOn,
                            title = "Cellular Data Saver",
                            subtitle = "Limit adaptive video quality to 720p on mobile data plans",
                            checked = cellularDataSaver,
                            onCheckedChange = {
                                cellularDataSaver = it
                                playerManager?.setCellularDataSaver(it)
                            }
                        )

                        // Show Speed in Player HUD
                        SettingsToggleRow(
                            icon = Icons.Default.Speed,
                            title = "Display Speed Badge in Player",
                            subtitle = "Show real-time Mbps indicator and network health in the playback HUD",
                            checked = showSpeedHud,
                            onCheckedChange = {
                                showSpeedHud = it
                                playerManager?.setShowSpeedInPlayer(it)
                            }
                        )
                    }
                }
            }

            // Video Playback Section
            item {
                SettingsSectionTitle("DIRECT STREAMING & ACCELERATION")
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = JellySurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingsToggleRow(
                            icon = Icons.Default.HighQuality,
                            title = "Direct Stream Original Quality",
                            subtitle = "Stream video in native container without server-side transcoding when bandwidth allows",
                            checked = highBitrateDirectPlay,
                            onCheckedChange = { highBitrateDirectPlay = it }
                        )

                        SettingsToggleRow(
                            icon = Icons.Default.Memory,
                            title = "Hardware Video Acceleration",
                            subtitle = "Use GPU hardware decoders for ultra-low latency 4K HDR playback",
                            checked = hardwareAcceleration,
                            onCheckedChange = { hardwareAcceleration = it }
                        )
                    }
                }
            }

            // Audio & Subtitles Section
            item {
                SettingsSectionTitle("AUDIO & SUBTITLES")
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = JellySurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingsToggleRow(
                            icon = Icons.Default.VolumeUp,
                            title = "Volume Dynamic Range Normalization",
                            subtitle = "Enhance quiet dialog and compress loud action explosions",
                            checked = volumeNormalization,
                            onCheckedChange = { volumeNormalization = it }
                        )

                        SettingsToggleRow(
                            icon = Icons.Default.Subtitles,
                            title = "Embedded Subtitles Support",
                            subtitle = "Render SSA/ASS, SRT and WebVTT styling in player",
                            checked = true,
                            onCheckedChange = {}
                        )
                    }
                }
            }

            // Room Database & Offline Cache Management
            item {
                SettingsSectionTitle("ROOM DATABASE & OFFLINE CACHE")
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = JellySurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = JellyCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Instant Offline Library",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Metadata and poster URLs cached in Room database for instant offline load",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0x1FFFFFFF))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Cached Media Items", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                            Text(
                                text = "$cachedCount items",
                                style = MaterialTheme.typography.bodyMedium.copy(color = JellyCyan, fontWeight = FontWeight.Bold)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Poster Disk Storage", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                            Text(
                                text = "Coil Disk Cache (250 MB max)",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.Medium)
                            )
                        }

                        if (viewModel != null) {
                            Button(
                                onClick = { viewModel.clearLocalCache() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33EF4444)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("clear_cache_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clear Local Room Cache", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // About JellyStream
            item {
                SettingsSectionTitle("ABOUT JELLYSTREAM")
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = JellySurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Client Version", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                            Text("1.0.0 (Release)", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Adaptive Engine", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                            Text("Real-Time Bandwidth Estimator", style = MaterialTheme.typography.bodyMedium.copy(color = JellyCyan, fontWeight = FontWeight.Medium))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Media Player Engine", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                            Text("AndroidX Media3 ExoPlayer", style = MaterialTheme.typography.bodyMedium.copy(color = JellyCyan, fontWeight = FontWeight.Medium))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Jellyfin API Compatibility", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                            Text("10.8.x – 10.10.x", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.Medium))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            color = JellyCyan,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = JellyPurplePrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = JellyPurplePrimary,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = JellyDarkBg
            )
        )
    }
}
