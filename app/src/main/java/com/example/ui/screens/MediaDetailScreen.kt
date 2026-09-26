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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.MainViewModel
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JellyAmber
import com.example.ui.theme.JellyCyan
import com.example.ui.theme.JellyDarkBg
import com.example.ui.theme.JellyPink
import com.example.ui.theme.JellyPurplePrimary
import com.example.ui.theme.JellySurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MediaDetailScreen(
    item: MediaItem,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val episodes by viewModel.episodes.collectAsState()
    val isEpisodesLoading by viewModel.isEpisodesLoading.collectAsState()
    var isFavorite by remember { mutableStateOf(item.isFavorite) }
    var overviewExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(item.id, item.title) {
        if (item.type == MediaType.SERIES || item.type == MediaType.SEASON) {
            viewModel.loadEpisodes(seriesId = item.id, seriesName = item.title)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JellyDarkBg)
            .testTag("media_detail_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Backdrop Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    val bgImg = item.backdropUrl ?: item.posterUrl
                    if (!bgImg.isNullOrBlank()) {
                        AsyncImage(
                            model = bgImg,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF2E1065), Color(0xFF0F172A))
                                    )
                                )
                        )
                    }

                    // Vignette Gradient Fade
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x99000000),
                                        Color.Transparent,
                                        Color(0x80090710),
                                        JellyDarkBg
                                    )
                                )
                            )
                    )

                    // Top Action Bar: Back and Favorite buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x99000000))
                                .testTag("detail_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                isFavorite = !isFavorite
                                viewModel.toggleFavorite(item)
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x99000000))
                                .testTag("detail_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) JellyPink else TextPrimary
                            )
                        }
                    }
                }
            }

            // Media Info & Title
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    // Title
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Meta Row: Rating, Year, Duration, Resolution, Audio
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (item.communityRating != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color(0xFF231B3A), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = JellyAmber,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = String.format("%.1f", item.communityRating),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        if (item.year != null) {
                            Text(
                                text = item.year.toString(),
                                style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                            )
                        }

                        if (item.durationMs > 0) {
                            Text(
                                text = "•  ${item.formattedDuration}",
                                style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                            )
                        }

                        // Badge 4K / HD
                        Text(
                            text = item.resolutionTag,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = JellyCyan,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .background(JellyCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        if (item.hasHdr) {
                            Text(
                                text = "HDR",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = JellyPurplePrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .background(JellyPurplePrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Genres
                    if (item.genres.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item.genres.forEach { g ->
                                Text(
                                    text = g,
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                                    modifier = Modifier
                                        .background(Color(0xFF1E1736), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Action Button (Play Now)
                    Button(
                        onClick = { viewModel.playMedia(item) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("detail_play_now_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JellyPurplePrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (item.playbackPositionMs > 0) "Resume (${item.progressFraction.times(100).toInt()}%)" else "Play",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Synopsis / Overview
                    if (item.overview.isNotBlank()) {
                        Text(
                            text = "Storyline",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.overview,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                lineHeight = 22.sp
                            ),
                            maxLines = if (overviewExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { overviewExpanded = !overviewExpanded }
                        )
                        if (item.overview.length > 140) {
                            Text(
                                text = if (overviewExpanded) "Show less" else "Read more",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = JellyCyan,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .clickable { overviewExpanded = !overviewExpanded }
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // TV Series & Seasons: Episodes Shelf
            if (item.type == MediaType.SERIES || item.type == MediaType.SEASON) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Episodes (${if (isEpisodesLoading) "..." else episodes.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        if (episodes.isNotEmpty()) {
                            Text(
                                text = "Season 1",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = JellyCyan,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .background(JellyCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (isEpisodesLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = JellyCyan,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Loading episodes...",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                        }
                    }
                } else if (episodes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1B1430))
                                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No episodes currently available for this show.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }
                } else {
                    items(episodes, key = { it.id }) { ep ->
                        EpisodeRowItem(
                            episode = ep,
                            onPlay = { viewModel.playMedia(ep, episodes) }
                        )
                    }
                }
            }

            // Cast & Crew
            if (item.cast.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Top Cast & Crew",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(item.cast) { castMember ->
                            CastMemberCard(castMember)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EpisodeRowItem(
    episode: MediaItem,
    onPlay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Episode Thumbnail
        Box(
            modifier = Modifier
                .width(110.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(JellySurfaceElevated)
                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            val epImg = episode.backdropUrl ?: episode.posterUrl
            if (!epImg.isNullOrBlank()) {
                AsyncImage(
                    model = epImg,
                    contentDescription = episode.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0x80000000))
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${episode.episodeNumber ?: 1}. ${episode.title}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = episode.overview,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = episode.formattedDuration,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = JellyCyan,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun CastMemberCard(cast: com.example.data.model.CastMember) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF261D42))
                .border(1.dp, GlassBorder, CircleShape)
        ) {
            if (!cast.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = cast.avatarUrl,
                    contentDescription = cast.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = cast.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = cast.role,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontSize = 10.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
