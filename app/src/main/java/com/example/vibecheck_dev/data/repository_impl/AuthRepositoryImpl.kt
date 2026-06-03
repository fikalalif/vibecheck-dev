package com.example.vibecheck_dev.data.repository_impl

import com.example.vibecheck_dev.data.source.remote.dto.RegisterRequest
import com.example.vibecheck_dev.data.source.remote.dto.UpdateUsernameRequest
import com.example.vibecheck_dev.data.source.remote.dto.UpdatePasswordRequest
import com.example.vibecheck_dev.data.source.remote.dto.VibeCheckApi
import com.example.vibecheck_dev.domain.repository.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val api: VibeCheckApi,
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<String> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val tokenResult = authResult.user?.getIdToken(true)?.await()
            val token = tokenResult?.token ?: throw Exception("Gagal mendapatkan token")
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(request: RegisterRequest): Result<Unit> {
        return try {
            val response = api.register(request)
            if (response.isSuccessful && response.body()?.status == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal Register"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            Result.success(authResult.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUsername(newUsername: String): Result<Unit> {
        return try {
            // 1. Ambil Token JWT saat ini
            val user = firebaseAuth.currentUser ?: throw Exception("User belum login")
            val tokenResult = user.getIdToken(false).await()
            val token = "Bearer ${tokenResult.token}"

            // 2. Tembak Spring Boot bawa Token
            val request = UpdateUsernameRequest(newUsername)
            val response = api.updateUsername(token, request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gagal: Backend menolak request (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("User belum login")

            // 1. RE-AUTH (Cek password lama bener apa ngga)
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            user.reauthenticate(credential).await()

            // 2. Ambil Token JWT
            val tokenResult = user.getIdToken(false).await()
            val token = "Bearer ${tokenResult.token}"

            // 3. Tembak Spring Boot
            val request = UpdatePasswordRequest(newPassword)
            val response = api.updatePassword(token, request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gagal update password di server"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Password lama lu salah bro!"))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}