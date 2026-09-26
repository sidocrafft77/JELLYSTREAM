package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.MainViewModel
import com.example.ui.components.LibraryBrowserComponent

/**
 * Screen container for the Jellyfin Library Browser.
 * Embeds the Retrofit-backed LibraryBrowserComponent.
 */
@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LibraryBrowserComponent(
        viewModel = viewModel,
        onBack = onBack,
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        showTopBar = true,
        title = "Media Library"
    )
}
