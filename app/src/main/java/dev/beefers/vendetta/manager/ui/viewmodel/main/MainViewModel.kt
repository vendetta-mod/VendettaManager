package dev.beefers.vendetta.manager.ui.viewmodel.main

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import dev.beefers.vendetta.manager.BuildConfig
import dev.beefers.vendetta.manager.domain.manager.DownloadManager
import dev.beefers.vendetta.manager.domain.manager.InstallMethod
import dev.beefers.vendetta.manager.domain.manager.PreferenceManager
import dev.beefers.vendetta.manager.domain.repository.RestRepository
import dev.beefers.vendetta.manager.installer.Installer
import dev.beefers.vendetta.manager.installer.session.SessionInstaller
import dev.beefers.vendetta.manager.installer.shizuku.IShizukuInstallerService
import dev.beefers.vendetta.manager.installer.shizuku.ShizukuInstaller
import dev.beefers.vendetta.manager.network.dto.Release
import dev.beefers.vendetta.manager.network.utils.dataOrNull
import dev.beefers.vendetta.manager.network.utils.ifSuccessful
import dev.beefers.vendetta.manager.utils.isMiui
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(
    private val repo: RestRepository,
    private val downloadManager: DownloadManager,
    private val preferenceManager: PreferenceManager,
    private val context: Context
) : ScreenModel {
    private val cacheDir = context.externalCacheDir ?: context.cacheDir
    var release by mutableStateOf<Release?>(null)
        private set

    var showUpdateDialog by mutableStateOf(false)

    var isUpdating by mutableStateOf(false)

    companion object {
        var serviceBinder: IShizukuInstallerService? = null
    }

    init {
        checkForUpdate()
    }

    private fun checkForUpdate() {
        coroutineScope.launch {
            release = repo.getLatestRelease("VendettaManager").dataOrNull
            release?.let {
                showUpdateDialog = it.tagName.toInt() > BuildConfig.VERSION_CODE
            }
            repo.getLatestRelease("VendettaXposed").ifSuccessful {
                if (preferenceManager.moduleVersion != it.tagName) {
                    preferenceManager.moduleVersion = it.tagName
                    val module = File(cacheDir, "vendetta.apk")
                    if (module.exists()) module.delete()
                }
            }
        }
    }

    fun downloadAndInstallUpdate() {
        coroutineScope.launch {
            val update = File(cacheDir, "update.apk")
            if (update.exists()) update.delete()
            isUpdating = true
            downloadManager.downloadUpdate(update)
            isUpdating = false

            val installer: Installer = if (preferenceManager.installMethod == InstallMethod.SHIZUKU) {
                ShizukuInstaller(context)
            } else {
                SessionInstaller(context)
            }

            installer.installApks(silent = !isMiui, update)
        }
    }

}