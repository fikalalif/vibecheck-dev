package com.example.vibecheck_dev.data.repository_impl

import com.example.vibecheck_dev.data.remote.dto.AddLogRequest
import com.example.vibecheck_dev.data.remote.dto.LogDto
import com.example.vibecheck_dev.data.remote.dto.MarketDataPayload
import com.example.vibecheck_dev.data.remote.dto.TelemetryData
import com.example.vibecheck_dev.data.remote.dto.TelemetryRequest
import com.example.vibecheck_dev.data.remote.dto.UpdateProfilePictureRequest
import com.example.vibecheck_dev.data.remote.dto.UpdateUsernameRequest
import com.example.vibecheck_dev.data.remote.network.VibeCheckApi
import com.example.vibecheck_dev.domain.repository.UserRepository

class UserRepositoryImpl(
    private val api: VibeCheckApi
) : UserRepository {

    override suspend fun updateUsername(username: String): Result<String> {
        return try {
            val response = api.updateUsername(UpdateUsernameRequest(username))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Sukses update di server")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Terjadi kesalahan di server"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfilePicture(photoUrl: String): Result<String> {
        return try {
            val response = api.updateProfilePicture(UpdateProfilePictureRequest(photoUrl))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Foto sukses update di server")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal update foto di server"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserLogs(): Result<List<LogDto>> {
        return try {
            val response = api.getUserLogs()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal mengambil log dari server"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addClientLog(action: String, details: String, deviceName: String?): Result<Unit> {
        return try {
            val response = api.addClientLog(AddLogRequest(action, details, deviceName))
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Gagal push log"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun trackActivity(type: String, subtype: String?): Result<Unit> {
        return try {
            val response = api.trackActivity(TelemetryRequest(type, subtype))
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Gagal ngirim telemetri"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTelemetryStats(): Result<TelemetryData> {
        return try {
            val response = api.getTelemetryStats()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: TelemetryData())
            } else {
                Result.failure(Exception("Gagal narik data statistik"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMarketData(): Result<MarketDataPayload> {
        return try {
            val response = api.getMarketData()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: MarketDataPayload())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}