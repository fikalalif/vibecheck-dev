package com.hn.vibecheck.domain.repository

import com.hn.vibecheck.data.remote.dto.LogDto
import com.hn.vibecheck.data.remote.dto.MarketDataPayload
import com.hn.vibecheck.data.remote.dto.TelemetryData

interface UserRepository {
    suspend fun updateUsername(username: String): Result<String>
    suspend fun updateProfilePicture(photoUrl: String): Result<String> // 🔴 Tambahan
    suspend fun getUserLogs(): Result<List<LogDto>>
    suspend fun addClientLog(action: String, details: String, deviceName: String? = null): Result<Unit>
    suspend fun trackActivity(type: String, subtype: String? = null): Result<Unit>
    suspend fun getTelemetryStats(): Result<TelemetryData>
    suspend fun getMarketData(): Result<MarketDataPayload>}