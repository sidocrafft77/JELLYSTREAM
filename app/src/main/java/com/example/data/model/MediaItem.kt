package com.example.data.model

enum class MediaType {
    MOVIE,
    SERIES,
    SEASON,
    EPISODE,
    AUDIO
}

data class MediaItem(
    val id: String,
    val title: String,
    val type: MediaType,
    val overview: String = "",
    val year: Int? = null,
    val communityRating: Float? = null,
    val durationMs: Long = 0L,
    val genres: List<String> = emptyList(),
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val streamUrl: String = "",
    val seriesName: String? = null,
    val seasonName: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val playbackPositionMs: Long = 0L,
    val isFavorite: Boolean = false,
    val resolutionTag: String = "1080p",
    val hasHdr: Boolean = false,
    val audioChannels: String = "5.1",
    val artistName: String? = null,
    val albumName: String? = null,
    val cast: List<CastMember> = emptyList()
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (playbackPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m ${seconds}s"
                else -> "${seconds}s"
            }
        }
}

data class CastMember(
    val name: String,
    val role: String,
    val avatarUrl: String? = null
)

data class LibraryCategory(
    val id: String,
    val title: String,
    val iconName: String,
    val type: MediaType? = null
)
