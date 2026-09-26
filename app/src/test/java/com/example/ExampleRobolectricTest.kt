package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.toCachedEntity
import com.example.data.local.toLibraryView
import com.example.data.local.toMediaItem
import com.example.data.model.JellyfinLibraryView
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.repository.JellyfinRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: JellyfinRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = JellyfinRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JellyStream", appName)
    }

    @Test
    fun `room database stores and retrieves cached media items and poster URLs`() = runBlocking {
        val serverId = "test_server_123"
        val mediaDao = database.mediaCacheDao()

        val item1 = MediaItem(
            id = "m1",
            title = "Blade Runner 2049",
            type = MediaType.MOVIE,
            overview = "A young blade runner unearths a long-buried secret.",
            year = 2017,
            communityRating = 8.9f,
            durationMs = 9800000L,
            genres = listOf("Sci-Fi", "Drama"),
            posterUrl = "https://image.tmdb.org/t/p/w500/blade_runner.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w1280/blade_runner_bg.jpg",
            streamUrl = "http://stream.local/m1.mp4"
        )

        val item2 = MediaItem(
            id = "s1",
            title = "Severance",
            type = MediaType.SERIES,
            overview = "Mark leads a team of office workers whose memories have been surgically divided.",
            year = 2022,
            communityRating = 8.7f,
            genres = listOf("Mystery", "Sci-Fi"),
            posterUrl = "https://image.tmdb.org/t/p/w500/severance.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w1280/severance_bg.jpg"
        )

        // Insert into Room
        mediaDao.insertMediaItems(listOf(
            item1.toCachedEntity(serverId),
            item2.toCachedEntity(serverId)
        ))

        // Check count
        val count = mediaDao.getCacheCount(serverId)
        assertEquals(2, count)

        // Retrieve all
        val cached = mediaDao.getAllCachedMedia(serverId)
        assertEquals(2, cached.size)

        // Verify poster URLs and metadata survived round-trip
        val cachedMovie = cached.first { it.itemId == "m1" }.toMediaItem()
        assertEquals("Blade Runner 2049", cachedMovie.title)
        assertEquals(MediaType.MOVIE, cachedMovie.type)
        assertEquals("https://image.tmdb.org/t/p/w500/blade_runner.jpg", cachedMovie.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w1280/blade_runner_bg.jpg", cachedMovie.backdropUrl)
        assertEquals(8.9f, cachedMovie.communityRating)

        // Retrieve filtered by type
        val moviesOnly = mediaDao.getCachedMediaByType(serverId, "MOVIE")
        assertEquals(1, moviesOnly.size)
        assertEquals("m1", moviesOnly[0].itemId)

        val seriesOnly = mediaDao.getCachedMediaByType(serverId, "SERIES")
        assertEquals(1, seriesOnly.size)
        assertEquals("s1", seriesOnly[0].itemId)
    }

    @Test
    fun `room database stores and retrieves library view folders`() = runBlocking {
        val serverId = "test_server_views"
        val viewDao = database.libraryViewCacheDao()

        val views = listOf(
            JellyfinLibraryView(id = "v_movies", name = "Movies", collectionType = "movies", type = MediaType.MOVIE, itemCount = 150),
            JellyfinLibraryView(id = "v_shows", name = "TV Shows", collectionType = "tvshows", type = MediaType.SERIES, itemCount = 35)
        )

        viewDao.insertViews(views.map { it.toCachedEntity(serverId) })

        val retrieved = viewDao.getCachedViews(serverId)
        assertEquals(2, retrieved.size)
        assertEquals("Movies", retrieved.first { it.viewId == "v_movies" }.name)
        assertEquals("tvshows", retrieved.first { it.viewId == "v_shows" }.collectionType)
    }

    @Test
    fun `repository loads cached library instantly without network connection`() = runBlocking {
        // Ensure default server seeds Room database cache
        repository.ensureDefaultServer()

        val active = database.serverDao().getActiveServer().firstOrNull()
        assertNotNull(active)

        // Verify that default demo items and library views are already in Room database
        val cachedLibraries = repository.getCachedUserLibraries(active!!.id)
        assertTrue("Room cache should have pre-populated libraries for instant offline start", cachedLibraries.isNotEmpty())

        val cachedItems = repository.getCachedLibraryItems(active.id)
        assertTrue("Room cache should have pre-populated media items for instant offline start", cachedItems.isNotEmpty())

        // Verify poster URLs exist on cached items
        val itemsWithPosters = cachedItems.filter { !it.posterUrl.isNullOrBlank() }
        assertEquals(cachedItems.size, itemsWithPosters.size)
    }
}
