package com.example.vibecheck_dev.data.source.remote.dto

// Format response baku dari Spring Boot kita
data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T?
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class UserProfileDto(
    val firebaseUid: String,
    val email: String,
    val username: String,
    val role: String,
    val isPremium: Boolean
)

data class UpdateUsernameRequest(
    val newUsername: String
)

data class UpdatePasswordRequest(
    val newPassword: String
)