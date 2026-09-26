package com.example.player

enum class QualityPreset(
    val id: String,
    val label: String,
    val shortLabel: String,
    val resolutionName: String,
    val maxBitrateBps: Long,
    val maxWidth: Int,
    val maxHeight: Int,
    val minSpeedRequiredBps: Long,
    val description: String
) {
    AUTO(
        id = "auto",
        label = "Auto (Adaptive)",
        shortLabel = "Auto",
        resolutionName = "Adaptive",
        maxBitrateBps = Long.MAX_VALUE,
        maxWidth = 3840,
        maxHeight = 2160,
        minSpeedRequiredBps = 0L,
        description = "Dynamically adjusts quality to match your real-time internet speed"
    ),
    UHD_4K(
        id = "4k",
        label = "4K Ultra HD",
        shortLabel = "4K",
        resolutionName = "2160p",
        maxBitrateBps = 40_000_000L,
        maxWidth = 3840,
        maxHeight = 2160,
        minSpeedRequiredBps = 25_000_000L,
        description = "Highest fidelity • Requires ultra-fast connection (25+ Mbps)"
    ),
    FHD_1080P(
        id = "1080p",
        label = "1080p Full HD",
        shortLabel = "1080p",
        resolutionName = "1080p",
        maxBitrateBps = 10_000_000L,
        maxWidth = 1920,
        maxHeight = 1080,
        minSpeedRequiredBps = 8_000_000L,
        description = "Crisp high-definition • Recommended for fast Wi-Fi / 5G (8+ Mbps)"
    ),
    HD_720P(
        id = "720p",
        label = "720p HD",
        shortLabel = "720p",
        resolutionName = "720p",
        maxBitrateBps = 4_000_000L,
        maxWidth = 1280,
        maxHeight = 720,
        minSpeedRequiredBps = 3_000_000L,
        description = "Smooth HD playback • Great balance of speed & quality (3+ Mbps)"
    ),
    SD_480P(
        id = "480p",
        label = "480p Standard",
        shortLabel = "480p",
        resolutionName = "480p",
        maxBitrateBps = 1_500_000L,
        maxWidth = 854,
        maxHeight = 480,
        minSpeedRequiredBps = 1_200_000L,
        description = "Standard definition • Smooth streaming on moderate networks (1.2+ Mbps)"
    ),
    SAVER_360P(
        id = "360p",
        label = "360p Data Saver",
        shortLabel = "360p",
        resolutionName = "360p",
        maxBitrateBps = 700_000L,
        maxWidth = 640,
        maxHeight = 360,
        minSpeedRequiredBps = 400_000L,
        description = "Ultra data saving • Plays without buffering on slow or spotty networks"
    );

    companion object {
        fun resolveFromBitrate(speedBps: Long, isCellularDataSaver: Boolean = false): QualityPreset {
            if (isCellularDataSaver && speedBps > HD_720P.maxBitrateBps) {
                return HD_720P
            }
            return when {
                speedBps >= UHD_4K.minSpeedRequiredBps -> UHD_4K
                speedBps >= FHD_1080P.minSpeedRequiredBps -> FHD_1080P
                speedBps >= HD_720P.minSpeedRequiredBps -> HD_720P
                speedBps >= SD_480P.minSpeedRequiredBps -> SD_480P
                else -> SAVER_360P
            }
        }
    }
}
