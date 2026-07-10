package com.hn.vibecheck.data.remote.network

import com.hn.vibecheck.data.remote.dto.AddLogRequest
import com.hn.vibecheck.data.remote.dto.BaseResponse
import com.hn.vibecheck.data.remote.dto.LogDto
import com.hn.vibecheck.data.remote.dto.MarketDataResponse
import com.hn.vibecheck.data.remote.dto.TelemetryRequest
import com.hn.vibecheck.data.remote.dto.TelemetryResponse
import com.hn.vibecheck.data.remote.dto.UpdateUsernameRequest
import com.hn.vibecheck.data.remote.dto.UpdateProfilePictureRequest // Tambahan
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT

interface VibeCheckApi {
    @PUT("api/users/username")
    suspend fun updateUsername(@Body request: UpdateUsernameRequest): Response<BaseResponse<Any>>

    @PUT("api/users/profile-picture")
    suspend fun updateProfilePicture(@Body request: UpdateProfilePictureRequest): Response<BaseResponse<Any>>

    @GET("api/users/logs")
    suspend fun getUserLogs(): Response<BaseResponse<List<LogDto>>>

    @POST("api/users/logs")
    suspend fun addClientLog(@Body request: AddLogRequest): Response<BaseResponse<Any>>

    @POST("api/telemetry/track")
    suspend fun trackActivity(@Body request: TelemetryRequest): Response<BaseResponse<Any>>

    @Headers("Cache-Control: no-cache")
    @GET("api/telemetry/stats")
    suspend fun getTelemetryStats(): Response<TelemetryResponse>

    // 🔴 JALUR BARU BUAT DATA MARKET (WIKI + YOUTUBE)
    @Headers("Cache-Control: no-cache")
    @GET("api/external/market-data")
    suspend fun getMarketData(): Response<MarketDataResponse>
}