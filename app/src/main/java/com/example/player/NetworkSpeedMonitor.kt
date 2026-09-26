package com.example.player

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.upstream.BandwidthMeter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class NetworkTransportType(val displayName: String) {
    WIFI("Wi-Fi"),
    CELLULAR("Cellular Mobile"),
    ETHERNET("Ethernet"),
    OTHER("Network"),
    DISCONNECTED("Offline")
}

enum class NetworkQualityTier(val label: String) {
    EXCELLENT("Ultra High Speed (4K Ready)"),
    GOOD("High Speed (1080p Ready)"),
    MODERATE("Standard Speed (720p Ready)"),
    FAIR("Fair Speed (480p Ready)"),
    LOW("Low Speed (Data Saver)"),
    OFFLINE("No Connection")
}

data class NetworkSpeedState(
    val speedBps: Long = 15_000_000L, // 15 Mbps baseline default
    val formattedSpeed: String = "15.0 Mbps",
    val transportType: NetworkTransportType = NetworkTransportType.WIFI,
    val tier: NetworkQualityTier = NetworkQualityTier.GOOD,
    val isMetered: Boolean = false,
    val recommendedQuality: QualityPreset = QualityPreset.FHD_1080P
)

@OptIn(UnstableApi::class)
class NetworkSpeedMonitor(
    context: Context,
    private val scope: CoroutineScope
) : BandwidthMeter.EventListener {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _speedState = MutableStateFlow(NetworkSpeedState())
    val speedState: StateFlow<NetworkSpeedState> = _speedState.asStateFlow()

    private var smoothedBitrate: Long = 15_000_000L
    private var isCellularSaverActive: Boolean = false

    private val mainHandler = Handler(Looper.getMainLooper())

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateFromConnectivityManager()
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            updateFromCapabilities(capabilities)
        }

        override fun onLost(network: Network) {
            mainHandler.post {
                _speedState.value = _speedState.value.copy(
                    speedBps = 0L,
                    formattedSpeed = "0 Kbps",
                    transportType = NetworkTransportType.DISCONNECTED,
                    tier = NetworkQualityTier.OFFLINE,
                    recommendedQuality = QualityPreset.SAVER_360P
                )
            }
        }
    }

    init {
        registerNetworkCallback()
        updateFromConnectivityManager()
    }

    private fun registerNetworkCallback() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) {
            // Fallback gracefully if permissions or restricted
        }
    }

    fun unregister() {
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {}
    }

    fun setCellularDataSaver(enabled: Boolean) {
        isCellularSaverActive = enabled
        recomputeState(smoothedBitrate, _speedState.value.transportType, _speedState.value.isMetered)
    }

    private fun updateFromConnectivityManager() {
        val cm = connectivityManager ?: return
        val activeNetwork = cm.activeNetwork ?: return
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return
        updateFromCapabilities(capabilities)
    }

    private fun updateFromCapabilities(capabilities: NetworkCapabilities) {
        val transport = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkTransportType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkTransportType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkTransportType.ETHERNET
            else -> NetworkTransportType.OTHER
        }

        val isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

        // Downstream bandwidth estimate from carrier / link speed (in Kbps)
        val downstreamKbps = capabilities.linkDownstreamBandwidthKbps
        val estimatedBps = if (downstreamKbps > 0) {
            (downstreamKbps.toLong() * 1000L).coerceIn(400_000L, 100_000_000L)
        } else {
            // Sensible fallback based on transport
            when (transport) {
                NetworkTransportType.WIFI -> 20_000_000L
                NetworkTransportType.ETHERNET -> 50_000_000L
                NetworkTransportType.CELLULAR -> 10_000_000L
                else -> 8_000_000L
            }
        }

        // Blend with existing smoothed bitrate
        if (smoothedBitrate == 15_000_000L) {
            smoothedBitrate = estimatedBps
        }

        mainHandler.post {
            recomputeState(smoothedBitrate, transport, isMetered)
        }
    }

    /**
     * Bandwidth sample callback from ExoPlayer's DefaultBandwidthMeter.
     * Real byte transfer measurements from active video streaming.
     */
    override fun onBandwidthSample(elapsedMs: Int, bytesTransferred: Long, bitrateEstimate: Long) {
        if (bitrateEstimate <= 0) return

        // Exponential smoothing (alpha = 0.35) to prevent rapid quality oscillations
        val alpha = 0.35
        smoothedBitrate = (alpha * bitrateEstimate + (1.0 - alpha) * smoothedBitrate).toLong()

        mainHandler.post {
            recomputeState(smoothedBitrate, _speedState.value.transportType, _speedState.value.isMetered)
        }
    }

    private fun recomputeState(
        speedBps: Long,
        transport: NetworkTransportType,
        isMetered: Boolean
    ) {
        val tier = when {
            speedBps >= 25_000_000L -> NetworkQualityTier.EXCELLENT
            speedBps >= 8_000_000L -> NetworkQualityTier.GOOD
            speedBps >= 3_000_000L -> NetworkQualityTier.MODERATE
            speedBps >= 1_200_000L -> NetworkQualityTier.FAIR
            speedBps > 0L -> NetworkQualityTier.LOW
            else -> NetworkQualityTier.OFFLINE
        }

        val formatted = formatBitrate(speedBps)
        val isCellular = transport == NetworkTransportType.CELLULAR || isMetered
        val recommended = QualityPreset.resolveFromBitrate(
            speedBps = speedBps,
            isCellularDataSaver = isCellularSaverActive && isCellular
        )

        _speedState.value = NetworkSpeedState(
            speedBps = speedBps,
            formattedSpeed = formatted,
            transportType = transport,
            tier = tier,
            isMetered = isMetered,
            recommendedQuality = recommended
        )
    }

    companion object {
        fun formatBitrate(bps: Long): String {
            return when {
                bps >= 1_000_000_000L -> String.format(Locale.US, "%.1f Gbps", bps / 1_000_000_000f)
                bps >= 1_000_000L -> String.format(Locale.US, "%.1f Mbps", bps / 1_000_000f)
                bps >= 1_000L -> String.format(Locale.US, "%d Kbps", bps / 1_000)
                bps > 0L -> "$bps bps"
                else -> "0 Kbps"
            }
        }
    }
}
