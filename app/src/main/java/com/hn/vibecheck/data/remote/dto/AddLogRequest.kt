package com.hn.vibecheck.data.remote.dto

data class AddLogRequest(
    val action: String,
    val details: String,
    val deviceName: String? = null // 🔴 Tambahan Opsional
)