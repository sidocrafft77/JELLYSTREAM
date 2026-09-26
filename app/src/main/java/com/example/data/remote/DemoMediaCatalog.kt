package com.example.data.remote

import com.example.data.model.CastMember
import com.example.data.model.MediaItem
import com.example.data.model.MediaType

object DemoMediaCatalog {

    val demoServerId = "demo_jellyfin_server"

    val movies = listOf(
        MediaItem(
            id = "demo_movie_1",
            title = "Tears of Steel",
            type = MediaType.MOVIE,
            overview = "In a dystopian cyberpunk future, a group of scientists and warriors in Amsterdam attempt to stage an alternate reality reset to prevent a catastrophic robotic apocalypse.",
            year = 2024,
            communityRating = 8.7f,
            durationMs = 734000L, // ~12m
            genres = listOf("Sci-Fi", "Cyberpunk", "Action"),
            posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            resolutionTag = "4K UHD",
            hasHdr = true,
            audioChannels = "Dolby 5.1",
            cast = listOf(
                CastMember("Derek de Lint", "Old Thom", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80"),
                CastMember("Sergio Hasselbaink", "Barley", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop&q=80"),
                CastMember("Rogier Schippers", "Captain", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&auto=format&fit=crop&q=80")
            )
        ),
        MediaItem(
            id = "demo_movie_2",
            title = "Big Buck Bunny",
            type = MediaType.MOVIE,
            overview = "A large and lovable rabbit deals with bullying woodland creatures in a lush forest with comedic and heartwarming outcomes.",
            year = 2023,
            communityRating = 8.2f,
            durationMs = 596000L, // ~10m
            genres = listOf("Animation", "Comedy", "Family"),
            posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            resolutionTag = "1080p 60fps",
            hasHdr = false,
            audioChannels = "Stereo",
            cast = listOf(
                CastMember("Sacha Goedegebure", "Director", null),
                CastMember("Ton Roosendaal", "Producer", null)
            )
        ),
        MediaItem(
            id = "demo_movie_3",
            title = "Sintel: Dragonheart",
            type = MediaType.MOVIE,
            overview = "A lonely young woman searches the dangerous wilderness for a baby dragon she nurtured back to health after it was abducted by a fearsome predator.",
            year = 2024,
            communityRating = 9.0f,
            durationMs = 888000L,
            genres = listOf("Fantasy", "Adventure", "Drama"),
            posterUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            resolutionTag = "4K HDR",
            hasHdr = true,
            audioChannels = "Dolby Atmos",
            cast = listOf(
                CastMember("Halina Reijn", "Sintel (voice)", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop&q=80"),
                CastMember("Thom Hoffman", "Shaman (voice)", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=200&auto=format&fit=crop&q=80")
            )
        ),
        MediaItem(
            id = "demo_movie_4",
            title = "Elephants Dream",
            type = MediaType.MOVIE,
            overview = "Two explorers navigate the bizarre and mechanized internal labyrinth of an endless machine that manifests surreal biological transformations.",
            year = 2022,
            communityRating = 7.9f,
            durationMs = 654000L,
            genres = listOf("Sci-Fi", "Surreal", "Animation"),
            posterUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            resolutionTag = "1080p",
            hasHdr = false,
            audioChannels = "5.1 Surround"
        ),
        MediaItem(
            id = "demo_movie_5",
            title = "For Bigger Blazes",
            type = MediaType.MOVIE,
            overview = "Adrenaline-fueled footage tracing extreme outdoor exploration across rugged alpine terrain and deep canyon trails.",
            year = 2024,
            communityRating = 8.4f,
            durationMs = 150000L,
            genres = listOf("Documentary", "Adventure", "Nature"),
            posterUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            resolutionTag = "4K 60fps",
            hasHdr = true,
            audioChannels = "Spatial Audio"
        )
    )

    val tvSeries = listOf(
        MediaItem(
            id = "demo_series_1",
            title = "Neon Nexus",
            type = MediaType.SERIES,
            overview = "Underneath the monolithic spires of Megacity Zero, cybernetic detectives solve high-profile AI crimes in an interconnected underworld.",
            year = 2024,
            communityRating = 9.3f,
            durationMs = 0L,
            genres = listOf("Sci-Fi", "Mystery", "Cyberpunk"),
            posterUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=1280&auto=format&fit=crop&q=80",
            resolutionTag = "4K HDR",
            hasHdr = true
        ),
        MediaItem(
            id = "demo_series_2",
            title = "Cosmic Frontier",
            type = MediaType.SERIES,
            overview = "Follow the crew of the exploration vessel Wanderer as they chart unknown wormholes on the outer rim of the galaxy.",
            year = 2023,
            communityRating = 8.8f,
            durationMs = 0L,
            genres = listOf("Sci-Fi", "Drama", "Space"),
            posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1280&auto=format&fit=crop&q=80",
            resolutionTag = "1080p",
            hasHdr = false
        )
    )

    val episodes = listOf(
        MediaItem(
            id = "demo_ep_1",
            title = "Ghost in the Machine",
            type = MediaType.EPISODE,
            overview = "Detective Miller investigates an anomalous power surge at the orbital mainframe, uncovering an unauthorized protocol.",
            year = 2024,
            communityRating = 9.1f,
            durationMs = 734000L,
            seriesName = "Neon Nexus",
            seasonName = "Season 1",
            seasonNumber = 1,
            episodeNumber = 1,
            posterUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            resolutionTag = "4K UHD"
        ),
        MediaItem(
            id = "demo_ep_2",
            title = "Sub-Level Echoes",
            type = MediaType.EPISODE,
            overview = "A chase through the subterranean transit tunnels forces the team to confront their rogue synth counterpart.",
            year = 2024,
            communityRating = 8.9f,
            durationMs = 596000L,
            seriesName = "Neon Nexus",
            seasonName = "Season 1",
            seasonNumber = 1,
            episodeNumber = 2,
            posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            resolutionTag = "4K UHD"
        ),
        MediaItem(
            id = "demo_ep_3",
            title = "Algorithmic Deception",
            type = MediaType.EPISODE,
            overview = "A rogue cybernetic syndicate threatens the central cloud grid with an encrypted virus payload.",
            year = 2024,
            communityRating = 9.2f,
            durationMs = 654000L,
            seriesName = "Neon Nexus",
            seasonName = "Season 1",
            seasonNumber = 1,
            episodeNumber = 3,
            posterUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            resolutionTag = "4K UHD"
        ),
        MediaItem(
            id = "demo_ep_4",
            title = "Zero Sum Protocol",
            type = MediaType.EPISODE,
            overview = "The season finale brings the investigation to the atmospheric edge as the true architect of the anomaly is revealed.",
            year = 2024,
            communityRating = 9.5f,
            durationMs = 734000L,
            seriesName = "Neon Nexus",
            seasonName = "Season 1",
            seasonNumber = 1,
            episodeNumber = 4,
            posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            resolutionTag = "4K UHD"
        ),
        MediaItem(
            id = "demo_ep_5",
            title = "The Quantum Lattice",
            type = MediaType.EPISODE,
            overview = "With their shields compromised, the crew must decrypt ancient orbital coordinates before the solar flare hits.",
            year = 2023,
            communityRating = 8.7f,
            durationMs = 888000L,
            seriesName = "Cosmic Frontier",
            seasonName = "Season 1",
            seasonNumber = 1,
            episodeNumber = 1,
            posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            resolutionTag = "1080p"
        ),
        MediaItem(
            id = "demo_ep_6",
            title = "Event Horizon Signals",
            type = MediaType.EPISODE,
            overview = "A mysterious beacon from inside an uncharted asteroid belt draws the Wanderer into deep gravitational resonance.",
            year = 2023,
            communityRating = 8.8f,
            durationMs = 596000L,
            seriesName = "Cosmic Frontier",
            seasonName = "Season 1",
            seasonNumber = 1,
            episodeNumber = 2,
            posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            resolutionTag = "1080p"
        ),
        MediaItem(
            id = "demo_ep_7",
            title = "Silent Orbit",
            type = MediaType.EPISODE,
            overview = "Tensions rise as long-range communications fail and the crew encounters an abandoned crystalline spacecraft.",
            year = 2023,
            communityRating = 8.9f,
            durationMs = 654000L,
            seriesName = "Cosmic Frontier",
            seasonName = "Season 1",
            seasonNumber = 1,
            episodeNumber = 3,
            posterUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            resolutionTag = "1080p"
        )
    )

    val musicTracks = listOf(
        MediaItem(
            id = "demo_song_1",
            title = "Midnight Cyberdrive",
            type = MediaType.AUDIO,
            overview = "Driving synthwave beats with nostalgic 80s analog synthesizers and pulsing basslines.",
            year = 2024,
            durationMs = 210000L,
            genres = listOf("Synthwave", "Electronic", "Retrowave"),
            artistName = "Hyperion Beats",
            albumName = "Neon Horizons",
            posterUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            resolutionTag = "FLAC 24-bit",
            audioChannels = "Stereo"
        ),
        MediaItem(
            id = "demo_song_2",
            title = "Starlight Lofi Chill",
            type = MediaType.AUDIO,
            overview = "Mellow vinyl crackles paired with smooth electric piano chords for late-night study or coding.",
            year = 2024,
            durationMs = 185000L,
            genres = listOf("Lo-Fi", "Chillhop", "Ambient"),
            artistName = "Aura Bloom",
            albumName = "Dusk Diaries",
            posterUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            resolutionTag = "MP3 320kbps",
            audioChannels = "Stereo"
        ),
        MediaItem(
            id = "demo_song_3",
            title = "Orbital Resonance",
            type = MediaType.AUDIO,
            overview = "Atmospheric deep house rhythms echoing across ethereal pad textures.",
            year = 2023,
            durationMs = 240000L,
            genres = listOf("Deep House", "Progressive", "Electronic"),
            artistName = "Kavinsky Pulse",
            albumName = "Solaris Dreams",
            posterUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1280&auto=format&fit=crop&q=80",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            resolutionTag = "Hi-Res Audio",
            audioChannels = "Spatial"
        )
    )

    fun getAllItems(): List<MediaItem> = movies + tvSeries + episodes + musicTracks

    fun getContinueWatching(): List<MediaItem> = listOf(
        movies[0].copy(playbackPositionMs = 280000L),
        movies[1].copy(playbackPositionMs = 150000L),
        episodes[0].copy(playbackPositionMs = 410000L)
    )

    fun getEpisodesForShow(seriesId: String, seriesName: String): List<MediaItem> {
        val cleanName = seriesName.trim()
        val matched = episodes.filter {
            it.seriesName?.equals(cleanName, ignoreCase = true) == true ||
            cleanName.contains(it.seriesName ?: "~~~", ignoreCase = true) ||
            (it.seriesName ?: "").contains(cleanName, ignoreCase = true) ||
            it.id.contains(seriesId, ignoreCase = true)
        }
        if (matched.isNotEmpty()) return matched

        return listOf(
            MediaItem(
                id = "${seriesId}_ep_1",
                title = "Genesis Protocol",
                type = MediaType.EPISODE,
                overview = "The premiere episode establishes the world and uncovers the initiating mystery.",
                year = 2024,
                communityRating = 9.0f,
                durationMs = 734000L,
                seriesName = seriesName,
                seasonName = "Season 1",
                seasonNumber = 1,
                episodeNumber = 1,
                posterUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=1280&auto=format&fit=crop&q=80",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                resolutionTag = "1080p"
            ),
            MediaItem(
                id = "${seriesId}_ep_2",
                title = "Shadow Crossing",
                type = MediaType.EPISODE,
                overview = "The stakes escalate as the team encounters hidden adversaries in unknown territory.",
                year = 2024,
                communityRating = 8.8f,
                durationMs = 596000L,
                seriesName = seriesName,
                seasonName = "Season 1",
                seasonNumber = 1,
                episodeNumber = 2,
                posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1280&auto=format&fit=crop&q=80",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                resolutionTag = "1080p"
            ),
            MediaItem(
                id = "${seriesId}_ep_3",
                title = "Convergence",
                type = MediaType.EPISODE,
                overview = "A pivotal confrontation reveals the true motives behind the overarching crisis.",
                year = 2024,
                communityRating = 9.2f,
                durationMs = 654000L,
                seriesName = seriesName,
                seasonName = "Season 1",
                seasonNumber = 1,
                episodeNumber = 3,
                posterUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1280&auto=format&fit=crop&q=80",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                resolutionTag = "1080p"
            )
        )
    }
}
