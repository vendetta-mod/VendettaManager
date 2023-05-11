package dev.beefers.vendetta.manager.ui.viewmodel.settings

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import cafe.adriel.voyager.core.model.ScreenModel
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.domain.manager.UpdateCheckerDuration
import dev.beefers.vendetta.manager.updatechecker.worker.UpdateWorker
import dev.beefers.vendetta.manager.utils.showToast

class SettingsViewModel(
    private val context: Context
) : ScreenModel {
    private val cacheDir = context.externalCacheDir!!

    fun clearCache() {
        cacheDir.deleteRecursively()
        context.showToast(R.string.msg_cleared_cache)
    }

    fun updateCheckerDuration(updateCheckerDuration: UpdateCheckerDuration) {
        val wm = WorkManager.getInstance(context)
        when (updateCheckerDuration) {
            UpdateCheckerDuration.DISABLED -> wm.cancelUniqueWork("dev.beefers.vendetta.manager.UPDATE_CHECK")
            else -> wm.enqueueUniquePeriodicWork(
                "dev.beefers.vendetta.manager.UPDATE_CHECK",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                PeriodicWorkRequestBuilder<UpdateWorker>(
                    updateCheckerDuration.time,
                    updateCheckerDuration.unit
                ).build()
            )
        }
    }

}