package com.example.vibecheck_dev.data.remote.dto

data class TelemetryRequest(
    val type: String,
    val subtype: String? = null
)

data class TelemetryResponse(
    val success: Boolean,
    val data: TelemetryData? = null
)

data class TelemetryData(
    val purikura_shutter_count: Int? = 0,
    val p2p_connect_count: Int? = 0,
    // 🔥 BUG FIX: Gunakan Double agar Retrofit/Gson tidak crash saat parsing JSON Number
    val frames: Map<String, Double>? = null,
    val filters: Map<String, Double>? = null,
    // Array Waktu dari Backend
    val shutter_logs: List<String>? = emptyList(),
    val p2p_logs: List<String>? = emptyList()
)

data class MarketDataResponse(
    val success: Boolean,
    val data: MarketDataPayload? = null
)

data class MarketDataPayload(
    val wiki_trends: WikiTrendsDoc? = null,
    val youtube_trends: YoutubeTrendsDoc? = null
)

data class WikiTrendsDoc(
    val data: Map<String, Double>? = null // Sama, gunakan Double untuk keamanan
)

data class YoutubeTrendsDoc(
    val top_videos: List<YoutubeVideo>? = null
)

data class YoutubeVideo(
    val title: String = "",
    val channel: String = "",
    val views: Long = 0,
    val duration: String = "",
    val url: String = ""
)