package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JellyfinLibraryView
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.AppScreen
import com.example.ui.LibraryLayoutMode
import com.example.ui.MainViewModel
import com.example.ui.SortOption
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JellyAmber
import com.example.ui.theme.JellyCyan
import com.example.ui.theme.JellyDarkBg
import com.example.ui.theme.JellyEmerald
import com.example.ui.theme.JellyPurplePrimary
import com.example.ui.theme.JellySurface
import com.example.ui.theme.JellySurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Reusable Library Browsing Component that fetches and displays media content
 * from the Jellyfin API using Retrofit.
 *
 * Supports:
 * - Dynamic library view folders (/Users/{userId}/Views via Retrofit)
 * - Media type filtering (Movies, Series, Music)
 * - Full-text search queries via Retrofit SearchTerm
 * - Genre filtering
 * - Server-side sorting (Title, Rating, PremiereDate, DateCreated)
 * - Multiple layout modes (Standard Card Grid, Compact Poster Grid, Detailed List)
 * - Loading skeletons, Network Error banner with Retry, and Empty states
 * - Interactive media cards with Play, Detail, and Favorite actions
 */
@Composable
fun LibraryBrowserComponent(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    showTopBar: Boolean = true,
    title: String = "Media Library",
    onItemClick: ((MediaItem) -> Unit)? = null,
    onPlayClick: ((MediaItem) -> Unit)? = null
) {
    val items by viewModel.libraryItems.collectAsState()
    val userLibraries by viewModel.userLibraries.collectAsState()
    val selectedLibraryView by viewModel.selectedLibraryView.collectAsState()
    val selectedMediaType by viewModel.selectedMediaType.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val layoutMode by viewModel.layoutMode.collectAsState()
    val isLoading by viewModel.isLibraryLoading.collectAsState()
    val errorMessage by viewModel.libraryError.collectAsState()
    val activeServer by viewModel.activeServer.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val cachedCount by viewModel.cachedCount.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }

    val genres = listOf(
        "Sci-Fi", "Cyberpunk", "Action", "Animation", "Drama",
        "Fantasy", "Crime", "Thriller", "Adventure"
    )

    // Animated continuous rotation for refresh button during Retrofit fetch
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "spin_angle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JellyDarkBg)
            .testTag("library_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Header Bar
            if (showTopBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("library_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    }

                    // Title & Active Server Indicator
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = if (onBack != null) 4.dp else 12.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(AppScreen.ServerManager) }
                                .padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (activeServer != null) JellyEmerald else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = activeServer?.name ?: "Jellyfin Server",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Layout Mode Switcher (Standard Grid, Compact Grid, List)
                    IconButton(
                        onClick = {
                            val nextMode = when (layoutMode) {
                                LibraryLayoutMode.GRID_STANDARD -> LibraryLayoutMode.GRID_COMPACT
                                LibraryLayoutMode.GRID_COMPACT -> LibraryLayoutMode.LIST
                                LibraryLayoutMode.LIST -> LibraryLayoutMode.GRID_STANDARD
                            }
                            viewModel.setLayoutMode(nextMode)
                        },
                        modifier = Modifier.testTag("library_layout_toggle")
                    ) {
                        Icon(
                            imageVector = when (layoutMode) {
                                LibraryLayoutMode.GRID_STANDARD -> Icons.Default.GridView
                                LibraryLayoutMode.GRID_COMPACT -> Icons.Default.ViewModule
                                LibraryLayoutMode.LIST -> Icons.Default.ViewList
                            },
                            contentDescription = "Switch Layout",
                            tint = JellyCyan
                        )
                    }

                    // Sort Options Menu
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("library_sort_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort Options",
                                tint = if (sortOption != SortOption.TITLE_ASC) JellyAmber else TextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(JellySurfaceElevated)
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (sortOption == option) JellyCyan else TextPrimary,
                                                fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        showSortMenu = false
                                    },
                                    modifier = Modifier.testTag("sort_option_${option.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // Refresh Button (fetches via Retrofit)
                    IconButton(
                        onClick = { viewModel.refreshLibrary() },
                        modifier = Modifier.testTag("library_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Library",
                            tint = JellyCyan,
                            modifier = if (isLoading) Modifier.rotate(spinAngle) else Modifier
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        "Search movies, series, or genres...",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = JellyCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = JellySurfaceElevated,
                    unfocusedContainerColor = JellySurface,
                    focusedBorderColor = JellyCyan,
                    unfocusedBorderColor = Color(0x33A855F7),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("library_search_input")
            )

            // Offline Room Cache Status Banner
            AnimatedVisibility(visible = isOfflineMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF261A42))
                        .border(1.dp, JellyCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("offline_cache_banner"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = JellyCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Offline Mode: Instant Room Cache Active ($cachedCount items ready)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Jellyfin Library Views Row (Fetched from Retrofit /Users/{userId}/Views)
            if (userLibraries.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("library_views_row")
                ) {
                    item {
                        LibraryFolderChip(
                            label = "All Libraries",
                            count = null,
                            isSelected = selectedLibraryView == null,
                            icon = Icons.Default.CloudDone,
                            onClick = { viewModel.selectLibraryView(null) }
                        )
                    }

                    items(userLibraries) { view ->
                        val isSelected = selectedLibraryView?.id == view.id
                        val icon = when (view.collectionType?.lowercase()) {
                            "movies" -> Icons.Default.LocalMovies
                            "tvshows" -> Icons.Default.Tv
                            "music" -> Icons.Default.MusicNote
                            else -> Icons.Default.LocalMovies
                        }
                        LibraryFolderChip(
                            label = view.name,
                            count = view.itemCount,
                            isSelected = isSelected,
                            icon = icon,
                            onClick = { viewModel.selectLibraryView(view) }
                        )
                    }
                }
            }

            // Quick Media Filter Chips (All, Movies, Shows)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MediaTypeSegmentChip(
                    label = "All",
                    isSelected = selectedMediaType == null,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("filter_chip_all"),
                    onClick = { viewModel.setSelectedMediaType(null) }
                )
                MediaTypeSegmentChip(
                    label = "Movies",
                    isSelected = selectedMediaType == MediaType.MOVIE,
                    icon = Icons.Default.LocalMovies,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("filter_chip_movies"),
                    onClick = { viewModel.setSelectedMediaType(MediaType.MOVIE) }
                )
                MediaTypeSegmentChip(
                    label = "TV Shows",
                    isSelected = selectedMediaType == MediaType.SERIES,
                    icon = Icons.Default.Tv,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("filter_chip_shows"),
                    onClick = { viewModel.setSelectedMediaType(MediaType.SERIES) }
                )
            }

            // Genre Chips Scroller
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("genre_chips_row")
            ) {
                items(genres) { g ->
                    val isSelected = selectedGenre.equals(g, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) JellyPurplePrimary else Color(0xFF19122C))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) JellyPurplePrimary else Color(0x33A855F7),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.setSelectedGenre(g) }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                            .testTag("genre_chip_${g.lowercase()}")
                    ) {
                        Text(
                            text = g,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.White else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }

            // Summary Bar & Active Filters Reset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val mediaCountText = when (selectedMediaType) {
                    MediaType.MOVIE -> "${items.size} Movies"
                    MediaType.SERIES -> "${items.size} Shows"
                    MediaType.AUDIO -> "${items.size} Audio Tracks"
                    else -> "${items.size} Items"
                }

                val sortText = " • ${sortOption.label}"

                Text(
                    text = "$mediaCountText$sortText",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )

                if (searchQuery.isNotBlank() || selectedGenre != null || selectedMediaType != null || selectedLibraryView != null) {
                    Text(
                        text = "Reset Filters",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = JellyCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier
                            .clickable { viewModel.clearFilters() }
                            .testTag("clear_filters_button")
                    )
                }
            }

            // Network Error Notice Banner from Retrofit query failure
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33EF4444))
                        .border(1.dp, Color(0x66EF4444), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .testTag("library_error_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = errorMessage ?: "Unable to fetch library items",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        Text(
                            text = "Retry",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = JellyCyan,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .clickable { viewModel.refreshLibrary() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("retry_fetch_button")
                        )
                    }
                }
            }

            // Main Media Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (isLoading && items.isEmpty()) {
                    // Shimmer loading skeletons
                    LoadingGridSkeleton(layoutMode = layoutMode)
                } else if (items.isEmpty()) {
                    // Empty state
                    EmptyLibraryState(
                        searchQuery = searchQuery,
                        hasFilters = selectedGenre != null || selectedMediaType != null,
                        onReset = { viewModel.clearFilters() }
                    )
                } else {
                    // Grid / List Display
                    when (layoutMode) {
                        LibraryLayoutMode.GRID_STANDARD -> {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("library_grid")
                            ) {
                                items(items, key = { it.id }) { item ->
                                    MediaCard(
                                        item = item,
                                        cardWidth = null,
                                        aspectRatio = if (item.type == MediaType.AUDIO) 1.0f else 0.68f,
                                        onClick = {
                                            if (onItemClick != null) {
                                                onItemClick(item)
                                            } else {
                                                if (item.type == MediaType.AUDIO) {
                                                    viewModel.playMedia(item)
                                                } else {
                                                    viewModel.navigateTo(AppScreen.MediaDetail(item))
                                                }
                                            }
                                        },
                                        onFavoriteClick = { viewModel.toggleFavorite(item) }
                                    )
                                }
                            }
                        }

                        LibraryLayoutMode.GRID_COMPACT -> {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 100.dp),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("library_grid_compact")
                            ) {
                                items(items, key = { it.id }) { item ->
                                    CompactMediaCard(
                                        item = item,
                                        onClick = {
                                            if (onItemClick != null) {
                                                onItemClick(item)
                                            } else {
                                                if (item.type == MediaType.AUDIO) {
                                                    viewModel.playMedia(item)
                                                } else {
                                                    viewModel.navigateTo(AppScreen.MediaDetail(item))
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        LibraryLayoutMode.LIST -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(1),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("library_list_view")
                            ) {
                                items(items, key = { it.id }) { item ->
                                    ListMediaRow(
                                        item = item,
                                        onClick = {
                                            if (onItemClick != null) {
                                                onItemClick(item)
                                            } else {
                                                viewModel.navigateTo(AppScreen.MediaDetail(item))
                                            }
                                        },
                                        onPlayClick = {
                                            if (onPlayClick != null) {
                                                onPlayClick(item)
                                            } else {
                                                viewModel.playMedia(item)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryFolderChip(
    label: String,
    count: Int?,
    isSelected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) JellyPurplePrimary else JellySurfaceElevated)
            .border(
                1.dp,
                if (isSelected) JellyPurplePrimary else GlassBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .testTag("view_chip_${label.lowercase().replace(" ", "_")}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else JellyCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isSelected) Color.White else TextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            )
            if (count != null && count > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "($count)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun MediaTypeSegmentChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) JellyCyan else JellySurfaceElevated)
            .border(
                1.dp,
                if (isSelected) JellyCyan else GlassBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.Black else TextSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isSelected) Color.Black else TextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun LoadingGridSkeleton(layoutMode: LibraryLayoutMode) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing)
        ),
        label = "pulse_alpha"
    )

    when (layoutMode) {
        LibraryLayoutMode.GRID_STANDARD -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(6) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.68f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1B142F).copy(alpha = alpha))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF261D40).copy(alpha = alpha))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E1634).copy(alpha = alpha))
                        )
                    }
                }
            }
        }
        LibraryLayoutMode.GRID_COMPACT -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(9) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.68f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1B142F).copy(alpha = alpha))
                    )
                }
            }
        }
        LibraryLayoutMode.LIST -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1B142F).copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyLibraryState(
    searchQuery: String,
    hasFilters: Boolean,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(JellySurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = JellyCyan,
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (searchQuery.isNotBlank()) "No results for \"$searchQuery\"" else "No media found in library",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Try adjusting your search keywords, switching categories, or clearing filters.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            )

            if (searchQuery.isNotBlank() || hasFilters) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(JellyCyan)
                        .clickable { onReset() }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                        .testTag("empty_state_reset_button")
                ) {
                    Text(
                        text = "Reset All Filters",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

