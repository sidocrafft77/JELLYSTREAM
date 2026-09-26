package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy

/**
 * Custom Application class that configures global Coil ImageLoader with
 * high-performance memory and persistent disk caching for media posters,
 * backdrops, and avatars to ensure instant offline visual presentation.
 */
class JellyStreamApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 25% of available app memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("poster_image_cache"))
                    .maxSizeBytes(250L * 1024L * 1024L) // 250 MB disk cache for offline posters
                    .build()
            }
            // Retain image cache even when offline or when cache-control headers are absent
            .respectCacheHeaders(false)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }
}
