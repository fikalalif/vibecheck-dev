package com.example.vibecheck_dev.data.source.remote.dto

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface VibeCheckApi {
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<UserProfileDto>>

    @PUT("api/v1/auth/username")
    suspend fun updateUsername(
        @Header("Authorization") token: String,
        @Body request: UpdateUsernameRequest
    ): Response<Any>

    @PUT("api/v1/auth/password")
    suspend fun updatePassword(
        @Header("Authorization") token: String,
        @Body request: UpdatePasswordRequest
    ): Response<Any>
}