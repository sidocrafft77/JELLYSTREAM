package com.example.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class VideoScaleMode {
    FIT,    // Standard aspect ratio letterbox
    FILL,   // Stretched to screen
    ZOOM    // Cropped center zoom
}

data class PlayerUiState(
    val currentItem: MediaItem? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val scaleMode: VideoScaleMode = VideoScaleMode.FIT,
    val isControlsLocked: Boolean = false,
    val sleepTimerMinutesLeft: Int? = null,
    val isFullScreen: Boolean = false,
    val queue: List<MediaItem> = emptyList(),
    val isAudioPlayerExpanded: Boolean = false,
    val selectedQuality: QualityPreset = QualityPreset.AUTO,
    val effectiveQuality: QualityPreset = QualityPreset.FHD_1080P,
    val networkSpeedBps: Long = 15_000_000L,
    val formattedSpeed: String = "15.0 Mbps",
    val connectionType: String = "Wi-Fi",
    val networkTier: NetworkQualityTier = NetworkQualityTier.GOOD,
    val isCellularDataSaver: Boolean = false,
    val qualityNotification: String? = null,
    val showSpeedInPlayer: Boolean = true
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}

@OptIn(UnstableApi::class)
class PlayerManager(context: Context) {

    private val appContext = context.applicationContext
    private val playerScope = CoroutineScope(Dispatchers.Main)

    val bandwidthMeter: DefaultBandwidthMeter = DefaultBandwidthMeter.Builder(appContext).build()
    val trackSelector: DefaultTrackSelector = DefaultTrackSelector(appContext)

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(appContext)
        .setTrackSelector(trackSelector)
        .setBandwidthMeter(bandwidthMeter)
        .build()

    val networkSpeedMonitor = NetworkSpeedMonitor(appContext, playerScope)

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressTrackerJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var notificationJob: Job? = null
    private var adaptiveStabilizerCount = 0
    private var candidateQuality: QualityPreset? = null

    var onProgressUpdateListener: ((MediaItem, Long, Long) -> Unit)? = null

    init {
        // Register bandwidth sample callback
        bandwidthMeter.addEventListener(Handler(Looper.getMainLooper()), networkSpeedMonitor)

        // Observe dynamic network speed and adapt quality
        playerScope.launch {
            networkSpeedMonitor.speedState.collect { speedState ->
                val prevEffective = _uiState.value.effectiveQuality
                val currentSelected = _uiState.value.selectedQuality

                _uiState.value = _uiState.value.copy(
                    networkSpeedBps = speedState.speedBps,
                    formattedSpeed = speedState.formattedSpeed,
                    connectionType = speedState.transportType.displayName,
                    networkTier = speedState.tier
                )

                if (currentSelected == QualityPreset.AUTO) {
                    val recommended = speedState.recommendedQuality
                    if (recommended != prevEffective) {
                        if (candidateQuality == recommended) {
                            adaptiveStabilizerCount++
                            // Debounce for 2 consecutive samples before switching to prevent thrashing
                            if (adaptiveStabilizerCount >= 2) {
                                applyEffectiveQuality(recommended, isAutoTriggered = true)
                                candidateQuality = null
                                adaptiveStabilizerCount = 0
                            }
                        } else {
                            candidateQuality = recommended
                            adaptiveStabilizerCount = 1
                        }
                    } else {
                        candidateQuality = null
                        adaptiveStabilizerCount = 0
                    }
                }
            }
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                val duration = if (exoPlayer.duration > 0) exoPlayer.duration else _uiState.value.currentItem?.durationMs ?: 0L
                _uiState.value = _uiState.value.copy(
                    isBuffering = isBuffering,
                    durationMs = duration
                )
            }
        })

        // Initial track parameters
        applyTrackSelectorLimits(_uiState.value.effectiveQuality)
    }

    fun play(mediaItem: MediaItem, startPositionMs: Long = 0L, playlist: List<MediaItem> = emptyList()) {
        val streamUri = mediaItem.streamUrl
        if (streamUri.isBlank()) return

        val exoItem = ExoMediaItem.fromUri(streamUri)
        exoPlayer.setMediaItem(exoItem)
        exoPlayer.prepare()

        if (startPositionMs > 0) {
            exoPlayer.seekTo(startPositionMs)
        }
        exoPlayer.play()

        _uiState.value = _uiState.value.copy(
            currentItem = mediaItem,
            currentPositionMs = startPositionMs,
            durationMs = mediaItem.durationMs,
            queue = if (playlist.isNotEmpty()) playlist else listOf(mediaItem),
            isAudioPlayerExpanded = mediaItem.type == MediaType.AUDIO
        )

        // Apply current active quality constraints
        applyTrackSelectorLimits(_uiState.value.effectiveQuality)
    }

    fun setVideoQuality(quality: QualityPreset) {
        if (_uiState.value.selectedQuality == quality) return

        _uiState.value = _uiState.value.copy(selectedQuality = quality)

        if (quality == QualityPreset.AUTO) {
            val speedState = networkSpeedMonitor.speedState.value
            val recommended = speedState.recommendedQuality
            applyEffectiveQuality(recommended, isAutoTriggered = false)
            showNotification("Quality set to Auto (${recommended.resolutionName} • ${speedState.formattedSpeed})")
        } else {
            applyEffectiveQuality(quality, isAutoTriggered = false)
            showNotification("Quality set to ${quality.label}")
        }
    }

    private fun applyEffectiveQuality(quality: QualityPreset, isAutoTriggered: Boolean) {
        _uiState.value = _uiState.value.copy(effectiveQuality = quality)
        applyTrackSelectorLimits(quality)

        if (isAutoTriggered) {
            val speed = _uiState.value.formattedSpeed
            showNotification("⚡ Auto adapted to ${quality.resolutionName} ($speed)")
        }
    }

    private fun applyTrackSelectorLimits(quality: QualityPreset) {
        try {
            val maxBitrate = if (quality.maxBitrateBps == Long.MAX_VALUE) Int.MAX_VALUE else quality.maxBitrateBps.toInt()
            val parameters = trackSelector.buildUponParameters()
                .setMaxVideoBitrate(maxBitrate)
                .setMaxVideoSize(quality.maxWidth, quality.maxHeight)
                .build()
            trackSelector.parameters = parameters
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setCellularDataSaver(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isCellularDataSaver = enabled)
        networkSpeedMonitor.setCellularDataSaver(enabled)
        if (enabled) {
            showNotification("Mobile Data Saver enabled (Max 720p)")
        } else {
            showNotification("Mobile Data Saver disabled")
        }
    }

    fun setShowSpeedInPlayer(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSpeedInPlayer = show)
    }

    private fun showNotification(msg: String) {
        notificationJob?.cancel()
        _uiState.value = _uiState.value.copy(qualityNotification = msg)
        notificationJob = playerScope.launch {
            delay(3500L)
            _uiState.value = _uiState.value.copy(qualityNotification = null)
        }
    }

    fun clearQualityNotification() {
        notificationJob?.cancel()
        _uiState.value = _uiState.value.copy(qualityNotification = null)
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun resume() {
        exoPlayer.play()
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))
        exoPlayer.seekTo(clamped)
        _uiState.value = _uiState.value.copy(currentPositionMs = clamped)
    }

    fun seekForward(deltaMs: Long = 10000L) {
        val newPos = (exoPlayer.currentPosition + deltaMs).coerceAtMost(exoPlayer.duration.coerceAtLeast(0L))
        seekTo(newPos)
    }

    fun seekBackward(deltaMs: Long = 10000L) {
        val newPos = (exoPlayer.currentPosition - deltaMs).coerceAtLeast(0L)
        seekTo(newPos)
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun setScaleMode(mode: VideoScaleMode) {
        _uiState.value = _uiState.value.copy(scaleMode = mode)
    }

    fun toggleControlsLock() {
        _uiState.value = _uiState.value.copy(isControlsLocked = !_uiState.value.isControlsLocked)
    }

    fun toggleFullScreen() {
        _uiState.value = _uiState.value.copy(isFullScreen = !_uiState.value.isFullScreen)
    }

    fun setAudioPlayerExpanded(expanded: Boolean) {
        _uiState.value = _uiState.value.copy(isAudioPlayerExpanded = expanded)
    }

    fun stopAndClear() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        stopProgressTracker()
        notificationJob?.cancel()
        _uiState.value = PlayerUiState()
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _uiState.value = _uiState.value.copy(sleepTimerMinutesLeft = null)
            return
        }

        _uiState.value = _uiState.value.copy(sleepTimerMinutesLeft = minutes)
        sleepTimerJob = playerScope.launch {
            var remainingMinutes = minutes
            while (remainingMinutes > 0 && isActive) {
                delay(60000L)
                remainingMinutes--
                _uiState.value = _uiState.value.copy(sleepTimerMinutesLeft = remainingMinutes)
            }
            if (isActive) {
                pause()
                _uiState.value = _uiState.value.copy(sleepTimerMinutesLeft = null)
            }
        }
    }

    fun playNextInQueue() {
        val q = _uiState.value.queue
        val current = _uiState.value.currentItem ?: return
        val currentIndex = q.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && currentIndex < q.lastIndex) {
            play(q[currentIndex + 1], 0L, q)
        }
    }

    fun playPreviousInQueue() {
        val q = _uiState.value.queue
        val current = _uiState.value.currentItem ?: return
        val currentIndex = q.indexOfFirst { it.id == current.id }
        if (currentIndex > 0) {
            play(q[currentIndex - 1], 0L, q)
        } else {
            seekTo(0L)
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressTrackerJob = playerScope.launch {
            while (isActive) {
                val current = exoPlayer.currentPosition
                val dur = if (exoPlayer.duration > 0) exoPlayer.duration else _uiState.value.durationMs
                val buffered = exoPlayer.bufferedPosition
                _uiState.value = _uiState.value.copy(
                    currentPositionMs = current,
                    durationMs = dur,
                    bufferedPositionMs = buffered
                )

                _uiState.value.currentItem?.let { item ->
                    onProgressUpdateListener?.invoke(item, current, dur)
                }
                delay(800L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = null
    }

    fun release() {
        stopProgressTracker()
        sleepTimerJob?.cancel()
        notificationJob?.cancel()
        try {
            bandwidthMeter.removeEventListener(networkSpeedMonitor)
            networkSpeedMonitor.unregister()
        } catch (_: Exception) {}
        exoPlayer.release()
    }
}
