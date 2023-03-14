package dev.beefers.vendetta.manager.ui.viewmodel.settings

import android.content.Context
import cafe.adriel.voyager.core.model.ScreenModel
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.utils.showToast

class SettingsViewModel(
    private val context: Context
) : ScreenModel {
    private val cacheDir = context.externalCacheDir!!

    fun clearCache() {
        cacheDir.deleteRecursively()
        context.showToast(R.string.msg_cleared_cache)
    }

}