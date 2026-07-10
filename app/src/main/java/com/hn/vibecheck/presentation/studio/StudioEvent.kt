package com.hn.vibecheck.presentation.studio

import android.content.Context
import android.net.Uri
import com.hn.vibecheck.domain.model.Y2KPreset

sealed class StudioEvent {
    data class LoadImage(val uri: Uri, val context: Context) : StudioEvent()
    data class ChangeTab(val index: Int) : StudioEvent()
    data class UpdateBrightness(val value: Float) : StudioEvent()
    data class UpdateContrast(val value: Float) : StudioEvent()
    data class UpdateSaturation(val value: Float) : StudioEvent()
    data class UpdateWarmth(val value: Float) : StudioEvent()
    data class ApplyPreset(val preset: Y2KPreset) : StudioEvent()
    data class ToggleSaveDialog(val show: Boolean) : StudioEvent()
    data class UpdateNewPresetName(val name: String) : StudioEvent()
    object SaveCustomPreset : StudioEvent()
    data class SaveImage(val context: Context, val onResult: (Boolean) -> Unit) : StudioEvent()
}