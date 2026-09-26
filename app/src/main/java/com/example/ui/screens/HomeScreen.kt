package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.HeroBanner
import com.example.ui.components.MediaCard
import com.example.ui.components.TopNavigationBar
import com.example.ui.components.WideMediaCard
import com.example.ui.theme.JellyCyan
import com.example.ui.theme.JellyDarkBg
import com.example.ui.theme.JellyPurplePrimary
import com.example.ui.theme.JellySurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeServer by viewModel.activeServer.collectAsState()
    val featuredItems by viewModel.featuredItems.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()
    val latestMovies by viewModel.latestMovies.collectAsState()
    val tvSeries by viewModel.tvSeries.collectAsState()
    val musicTracks by viewModel.musicTracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val topHero = featuredItems.firstOrNull() ?: latestMovies.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JellyDarkBg)
            .statusBarsPadding()
            .testTag("home_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Top App Bar
            item {
                TopNavigationBar(
                    activeServer = activeServer,
                    onServerClick = { viewModel.navigateTo(AppScreen.ServerManager) },
                    onLibraryClick = { viewModel.navigateTo(AppScreen.Library) },
                    onSearchClick = { viewModel.navigateTo(AppScreen.Library) },
                    onSettingsClick = { viewModel.navigateTo(AppScreen.Settings) }
                )
            }

            // Hero Showcase Banner
            if (topHero != null) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        HeroBanner(
                            item = topHero,
                            onPlayClick = { viewModel.playMedia(topHero) },
                            onDetailsClick = { viewModel.navigateTo(AppScreen.MediaDetail(topHero)) }
                        )
                    }
                }
            }

            // Quick Category Pills
            item {
                CategoryPillsRow(
                    onSelectCategory = { type ->
                        viewModel.setSelectedMediaType(type)
                        viewModel.navigateTo(AppScreen.Library)
                    }
                )
            }

            // Continue Watching Shelf
            if (continueWatching.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Continue Watching",
                        subtitle = "Resume where you left off",
                        onSeeAll = null
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("continue_watching_row")
                    ) {
                        items(continueWatching) { item ->
                            WideMediaCard(
                                item = item,
                                onClick = { viewModel.playMedia(item) }
                            )
                        }
                    }
                }
            }

            // Latest Movies Shelf
            if (latestMovies.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(
                        title = "Latest Movies",
                        subtitle = "Cinema releases & blockbusters",
                        onSeeAll = {
                            viewModel.setSelectedMediaType(MediaType.MOVIE)
                            viewModel.navigateTo(AppScreen.Library)
                        }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("latest_movies_row")
                    ) {
                        items(latestMovies) { movie ->
                            MediaCard(
                                item = movie,
                                onClick = { viewModel.navigateTo(AppScreen.MediaDetail(movie)) }
                            )
                        }
                    }
                }
            }

            // TV Series Shelf
            if (tvSeries.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(
                        title = "TV Series",
                        subtitle = "Binge-worthy shows & seasons",
                        onSeeAll = {
                            viewModel.setSelectedMediaType(MediaType.SERIES)
                            viewModel.navigateTo(AppScreen.Library)
                        }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("tv_series_row")
                    ) {
                        items(tvSeries) { series ->
                            MediaCard(
                                item = series,
                                onClick = { viewModel.navigateTo(AppScreen.MediaDetail(series)) }
                            )
                        }
                    }
                }
            }

            // Music & Audio Shelf
            if (musicTracks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(
                        title = "Music & Soundtracks",
                        subtitle = "Hi-Res streaming & playlists",
                        onSeeAll = {
                            viewModel.setSelectedMediaType(MediaType.AUDIO)
                            viewModel.navigateTo(AppScreen.Library)
                        }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("music_tracks_row")
                    ) {
                        items(musicTracks) { song ->
                            MediaCard(
                                item = song,
                                aspectRatio = 1.0f, // Square for music
                                onClick = { viewModel.playMedia(song, musicTracks) }
                            )
                        }
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Loading overlay
        if (isLoading && featuredItems.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = JellyCyan
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    onSeeAll: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
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

        if (onSeeAll != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onSeeAll() }
                    .padding(4.dp)
            ) {
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = JellyCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = JellyCyan,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryPillsRow(
    onSelectCategory: (MediaType?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CategoryPill(
            label = "Movies",
            icon = Icons.Default.LocalMovies,
            onClick = { onSelectCategory(MediaType.MOVIE) }
        )
        CategoryPill(
            label = "TV Shows",
            icon = Icons.Default.Tv,
            onClick = { onSelectCategory(MediaType.SERIES) }
        )
        CategoryPill(
            label = "Music",
            icon = Icons.Default.MusicNote,
            onClick = { onSelectCategory(MediaType.AUDIO) }
        )
    }
}

@Composable
fun CategoryPill(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(JellySurfaceElevated)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = JellyPurplePrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
