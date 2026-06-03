package com.example.vibecheck_dev.domain.repository

import com.example.vibecheck_dev.data.source.remote.dto.RegisterRequest

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<String>
    suspend fun register(request: RegisterRequest): Result<Unit>
    suspend fun loginWithGoogle(idToken: String): Result<String>

    suspend fun updateUsername(newUsername: String): Result<Unit>
    // UBAH JADI GINI:
    suspend fun updatePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun logout(): Result<Unit>
}