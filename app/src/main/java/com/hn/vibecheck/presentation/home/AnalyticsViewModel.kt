// File: AnalyticsViewModel.kt
package com.hn.vibecheck.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hn.vibecheck.data.remote.dto.MarketDataPayload
import com.hn.vibecheck.data.remote.dto.TelemetryData
import com.hn.vibecheck.domain.repository.UserRepository
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val grandTotal: TelemetryData? = null,
    val marketData: MarketDataPayload? = null,

    // 🔥 STATE FILTER WAKTU (Hanya untuk Time-Series)
    val shutterTimeFilter: String = "MINGGU INI",
    val p2pTimeFilter: String = "MINGGU INI",

    // 🔥 STATE GRAFIK TIME-SERIES (Area)
    val shutterChartModel: ChartEntryModel? = null,
    val shutterXFormatter: (Float) -> String = { it.toString() },

    val p2pChartModel: ChartEntryModel? = null,
    val p2pXFormatter: (Float) -> String = { it.toString() },

    // 🔥 STATE GRAFIK KATEGORIKAL (Wikipedia)
    val wikiDataList: List<Pair<String, Int>> = emptyList(),

    val error: String? = null
)

class AnalyticsViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        startRealTimePolling()
    }

    fun setShutterFilter(filter: String) {
        _uiState.update { it.copy(shutterTimeFilter = filter) }
        processAllCharts()
    }

    fun setP2pFilter(filter: String) {
        _uiState.update { it.copy(p2pTimeFilter = filter) }
        processAllCharts()
    }

    private fun startRealTimePolling() {
        viewModelScope.launch {
            while (isActive) {
                val statResult = userRepository.getTelemetryStats()
                val marketResult = userRepository.getMarketData()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        grandTotal = statResult.getOrNull() ?: it.grandTotal,
                        marketData = marketResult.getOrNull() ?: it.marketData,
                        error = null
                    )
                }
                processAllCharts()
                delay(5000)
            }
        }
    }

    private fun processAllCharts() {
        val state = _uiState.value
        val gt = state.grandTotal

        val shutterResult = processTimeSeries(gt?.shutter_logs, state.shutterTimeFilter)
        val p2pResult = processTimeSeries(gt?.p2p_logs, state.p2pTimeFilter)
        val wikiResult = processWikiData(state.marketData?.wiki_trends?.data)

        _uiState.update {
            it.copy(
                shutterChartModel = shutterResult.first,
                shutterXFormatter = shutterResult.second,
                p2pChartModel = p2pResult.first,
                p2pXFormatter = p2pResult.second,
                wikiDataList = wikiResult
            )
        }
    }

    // 🔥 ENGINE PEMROSES TIME-SERIES DENGAN TIMEZONE CONVERSION & ZERO-FILLING
    private fun processTimeSeries(events: List<String>?, filter: String): Pair<ChartEntryModel?, (Float) -> String> {
        if (events.isNullOrEmpty()) return Pair(null, { "" })

        // 1. Ambil zona waktu lokal perangkat user
        val zoneId = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zoneId)

        // 2. Parse ISO (UTC) dan transformasikan ke waktu lokal (Local TimeZone)
        val zonedEvents = events.mapNotNull {
            try {
                ZonedDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME).withZoneSameInstant(zoneId)
            } catch (e: Exception) { null }
        }

        val (entries, formatter) = when (filter) {
            "HARI INI" -> {
                val todayEvents = zonedEvents.filter { it.toLocalDate() == now.toLocalDate() }
                val grouped = todayEvents.groupBy { it.hour }

                val e = (0..23).map { hour ->
                    FloatEntry(hour.toFloat(), (grouped[hour]?.size ?: 0).toFloat())
                }
                // Cegah Vico menggambar label di titik desimal (misal 1.5) menggunakan (v % 1f == 0f)
                Pair(e, { v: Float ->
                    if (v % 1f == 0f) String.format(Locale.getDefault(), "%02d:00", v.toInt()) else ""
                })
            }
            "MINGGU INI" -> {
                val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                val thisWeekEvents = zonedEvents.filter { !it.isBefore(startOfWeek.truncatedTo(ChronoUnit.DAYS)) }
                val grouped = thisWeekEvents.groupBy { it.dayOfWeek.value }

                val days = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                val e = (1..7).map { day ->
                    FloatEntry(day.toFloat(), (grouped[day]?.size ?: 0).toFloat())
                }
                Pair(e, { v: Float ->
                    if (v % 1f == 0f) days.getOrElse(v.toInt() - 1) { "" } else ""
                })
            }
            "BULAN INI" -> {
                val thisMonthEvents = zonedEvents.filter { it.month == now.month && it.year == now.year }
                val grouped = thisMonthEvents.groupBy { it.dayOfMonth }

                val maxDays = now.month.length(now.toLocalDate().isLeapYear)
                val e = (1..maxDays).map { date ->
                    FloatEntry(date.toFloat(), (grouped[date]?.size ?: 0).toFloat())
                }
                Pair(e, { v: Float ->
                    if (v % 1f == 0f) v.toInt().toString() else ""
                })
            }
            else -> { // TAHUN INI
                val thisYearEvents = zonedEvents.filter { it.year == now.year }
                val grouped = thisYearEvents.groupBy { it.monthValue }

                val months = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
                val e = (1..12).map { month ->
                    FloatEntry(month.toFloat(), (grouped[month]?.size ?: 0).toFloat())
                }
                Pair(e, { v: Float ->
                    if (v % 1f == 0f) months.getOrElse(v.toInt() - 1) { "" } else ""
                })
            }
        }

        if (entries.all { it.y == 0f }) return Pair(null, { "" })
        return Pair(entryModelOf(entries), formatter)
    }

    private fun processWikiData(wikiMap: Map<String, Double>?): List<Pair<String, Int>> {
        if (wikiMap.isNullOrEmpty()) return emptyList()
        return wikiMap.map { (key, value) ->
            Pair(key.replace("_", " ").uppercase(), value.toInt())
        }.sortedByDescending { it.second }
    }
}