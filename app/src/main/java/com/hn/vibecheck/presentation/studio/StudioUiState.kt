package com.hn.vibecheck.presentation.studio

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import com.hn.vibecheck.domain.model.Y2KPreset

data class StudioUiState(
    val selectedImageUri: Uri? = null,
    val loadedAndroidBitmap: Bitmap? = null,
    val loadedImageBitmap: ImageBitmap? = null,
    val currentBrightness: Float = 1f,
    val currentContrast: Float = 1f,
    val currentSaturation: Float = 1f,
    val currentWarmth: Float = 0f,
    val selectedTab: Int = 0,
    val savedCustomPresets: List<Y2KPreset> = emptyList(),
    val showSaveDialog: Boolean = false,
    val newPresetName: String = "",
    val isSaving: Boolean = false,
    val appliedPresetName: String = "NORMAL"
)