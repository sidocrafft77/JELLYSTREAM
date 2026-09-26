package com.example

import com.example.data.remote.JellyfinClientFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testSanitizeUrl_addsHttpIfMissing() {
        val raw = "192.168.1.150:8096"
        val sanitized = JellyfinClientFactory.sanitizeUrl(raw)
        assertEquals("http://192.168.1.150:8096", sanitized)
    }

    @Test
    fun testSanitizeUrl_removesTrailingSlashes() {
        val raw = "https://jellyfin.local:8920///"
        val sanitized = JellyfinClientFactory.sanitizeUrl(raw)
        assertEquals("https://jellyfin.local:8920", sanitized)
    }

    @Test
    fun testBuildAuthHeader_formatCorrectness() {
        val headerWithoutToken = JellyfinClientFactory.buildAuthHeader(
            clientName = "JellyStream",
            version = "1.0.0",
            deviceName = "TestDevice",
            deviceId = "test-device-id-123"
        )
        assertTrue(headerWithoutToken.contains("MediaBrowser Client=\"JellyStream\""))
        assertTrue(headerWithoutToken.contains("Device=\"TestDevice\""))
        assertTrue(headerWithoutToken.contains("DeviceId=\"test-device-id-123\""))
        assertFalse(headerWithoutToken.contains("Token="))

        val headerWithToken = JellyfinClientFactory.buildAuthHeader(
            clientName = "JellyStream",
            version = "1.0.0",
            deviceName = "TestDevice",
            deviceId = "test-device-id-123",
            token = "secret-token-abc"
        )
        assertTrue(headerWithToken.contains("Token=\"secret-token-abc\""))
    }

    @Test
    fun testBuildStreamAndImageUrl() {
        val streamUrl = JellyfinClientFactory.buildStreamUrl(
            baseUrl = "http://localhost:8096/",
            itemId = "item123",
            token = "tok456"
        )
        assertEquals("http://localhost:8096/Videos/item123/stream?static=true&api_key=tok456", streamUrl)

        val imageUrl = JellyfinClientFactory.buildImageUrl(
            baseUrl = "http://localhost:8096/",
            itemId = "item123",
            imageType = "Primary",
            tag = "tag789"
        )
        assertEquals("http://localhost:8096/Items/item123/Images/Primary?tag=tag789", imageUrl)
    }

    @Test
    fun testQualityPreset_resolutionFromBitrate() {
        // > 25 Mbps -> 4K
        assertEquals(com.example.player.QualityPreset.UHD_4K, com.example.player.QualityPreset.resolveFromBitrate(30_000_000L))
        // 8 - 25 Mbps -> 1080p
        assertEquals(com.example.player.QualityPreset.FHD_1080P, com.example.player.QualityPreset.resolveFromBitrate(12_000_000L))
        // 3 - 8 Mbps -> 720p
        assertEquals(com.example.player.QualityPreset.HD_720P, com.example.player.QualityPreset.resolveFromBitrate(5_000_000L))
        // 1.2 - 3 Mbps -> 480p
        assertEquals(com.example.player.QualityPreset.SD_480P, com.example.player.QualityPreset.resolveFromBitrate(1_800_000L))
        // < 1.2 Mbps -> 360p Data Saver
        assertEquals(com.example.player.QualityPreset.SAVER_360P, com.example.player.QualityPreset.resolveFromBitrate(600_000L))
    }

    @Test
    fun testQualityPreset_cellularDataSaverCap() {
        // High speed on cellular with data saver enabled caps at 720p
        val quality = com.example.player.QualityPreset.resolveFromBitrate(
            speedBps = 45_000_000L,
            isCellularDataSaver = true
        )
        assertEquals(com.example.player.QualityPreset.HD_720P, quality)
    }

    @Test
    fun testNetworkSpeedMonitor_formatBitrate() {
        assertEquals("15.0 Mbps", com.example.player.NetworkSpeedMonitor.formatBitrate(15_000_000L))
        assertEquals("850 Kbps", com.example.player.NetworkSpeedMonitor.formatBitrate(850_000L))
        assertEquals("1.2 Gbps", com.example.player.NetworkSpeedMonitor.formatBitrate(1_200_000_000L))
    }

    @Test
    fun testRoomEntityConversion_preservesMetadataAndPosterUrls() {
        val item = com.example.data.model.MediaItem(
            id = "test_item_1",
            title = "Cyber Odyssey",
            type = com.example.data.model.MediaType.MOVIE,
            overview = "A neon-soaked journey through cyberspace.",
            year = 2025,
            communityRating = 9.4f,
            durationMs = 7200000L,
            genres = listOf("Sci-Fi", "Cyberpunk"),
            posterUrl = "https://images.unsplash.com/photo-cyber-poster.jpg",
            backdropUrl = "https://images.unsplash.com/photo-cyber-backdrop.jpg",
            streamUrl = "http://stream.local/video.mp4",
            resolutionTag = "4K HDR",
            hasHdr = true,
            audioChannels = "Dolby Atmos",
            cast = listOf(
                com.example.data.model.CastMember("Alex Rivera", "Pilot", "https://img.local/alex.jpg"),
                com.example.data.model.CastMember("Elena Cruz", "Engineer", null)
            )
        )

        // Convert to Room Cached Entity
        val entity = com.example.data.local.toCachedEntity(item, "server_42", "movies_folder")
        assertEquals("test_item_1", entity.itemId)
        assertEquals("server_42", entity.serverId)
        assertEquals("Cyber Odyssey", entity.title)
        assertEquals("MOVIE", entity.type)
        assertEquals("https://images.unsplash.com/photo-cyber-poster.jpg", entity.posterUrl)
        assertEquals("https://images.unsplash.com/photo-cyber-backdrop.jpg", entity.backdropUrl)
        assertEquals("movies_folder", entity.parentId)
        assertTrue(entity.hasHdr)

        // Convert back to domain MediaItem
        val restored = com.example.data.local.toMediaItem(entity)
        assertEquals(item.id, restored.id)
        assertEquals(item.title, restored.title)
        assertEquals(item.type, restored.type)
        assertEquals(item.overview, restored.overview)
        assertEquals(item.year, restored.year)
        assertEquals(item.communityRating, restored.communityRating)
        assertEquals(item.durationMs, restored.durationMs)
        assertEquals(item.posterUrl, restored.posterUrl)
        assertEquals(item.backdropUrl, restored.backdropUrl)
        assertEquals(2, restored.cast.size)
        assertEquals("Alex Rivera", restored.cast[0].name)
        assertEquals("Pilot", restored.cast[0].role)
        assertEquals("https://img.local/alex.jpg", restored.cast[0].avatarUrl)
    }

    @Test
    fun testLibraryViewConversion_preservesFolderData() {
        val view = com.example.data.model.JellyfinLibraryView(
            id = "movies_view",
            name = "Movies Collection",
            collectionType = "movies",
            type = com.example.data.model.MediaType.MOVIE,
            itemCount = 42,
            iconName = "LocalMovies"
        )

        val cachedEntity = com.example.data.local.toCachedEntity(view, "server_42")
        assertEquals("movies_view", cachedEntity.viewId)
        assertEquals("server_42", cachedEntity.serverId)
        assertEquals("Movies Collection", cachedEntity.name)
        assertEquals("movies", cachedEntity.collectionType)
        assertEquals("MOVIE", cachedEntity.type)
        assertEquals(42, cachedEntity.itemCount)

        val restoredView = com.example.data.local.toLibraryView(cachedEntity)
        assertEquals(view.id, restoredView.id)
        assertEquals(view.name, restoredView.name)
        assertEquals(view.collectionType, restoredView.collectionType)
        assertEquals(view.type, restoredView.type)
        assertEquals(view.itemCount, restoredView.itemCount)
    }

    @Test
    fun testGetEpisodesForShow() {
        val episodesNexus = com.example.data.remote.DemoMediaCatalog.getEpisodesForShow("demo_series_1", "Neon Nexus")
        assertTrue(episodesNexus.isNotEmpty())
        assertEquals("Neon Nexus", episodesNexus[0].seriesName)

        val episodesCosmic = com.example.data.remote.DemoMediaCatalog.getEpisodesForShow("demo_series_2", "Cosmic Frontier")
        assertTrue(episodesCosmic.isNotEmpty())
        assertEquals("Cosmic Frontier", episodesCosmic[0].seriesName)

        val fallbackEpisodes = com.example.data.remote.DemoMediaCatalog.getEpisodesForShow("random_id_999", "Unknown Anime")
        assertTrue(fallbackEpisodes.isNotEmpty())
        assertEquals(3, fallbackEpisodes.size)
    }
}
