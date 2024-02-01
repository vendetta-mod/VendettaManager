package dev.beefers.vendetta.manager.ui.viewmodel.installer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diamondminer88.zip.ZipCompression
import com.github.diamondminer88.zip.ZipReader
import com.github.diamondminer88.zip.ZipWriter
import dev.beefers.vendetta.manager.BuildConfig
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.domain.manager.DownloadManager
import dev.beefers.vendetta.manager.domain.manager.DownloadResult
import dev.beefers.vendetta.manager.domain.manager.InstallManager
import dev.beefers.vendetta.manager.domain.manager.InstallMethod
import dev.beefers.vendetta.manager.domain.manager.PreferenceManager
import dev.beefers.vendetta.manager.installer.Installer
import dev.beefers.vendetta.manager.installer.session.SessionInstaller
import dev.beefers.vendetta.manager.installer.shizuku.ShizukuInstaller
import dev.beefers.vendetta.manager.installer.step.Step
import dev.beefers.vendetta.manager.installer.step.StepGroup
import dev.beefers.vendetta.manager.installer.step.StepRunner
import dev.beefers.vendetta.manager.installer.step.installing.InstallStep
import dev.beefers.vendetta.manager.installer.util.ManifestPatcher
import dev.beefers.vendetta.manager.installer.util.Patcher
import dev.beefers.vendetta.manager.installer.util.Signer
import dev.beefers.vendetta.manager.utils.DiscordVersion
import dev.beefers.vendetta.manager.utils.copyText
import dev.beefers.vendetta.manager.utils.isMiui
import dev.beefers.vendetta.manager.utils.mainThread
import dev.beefers.vendetta.manager.utils.showToast
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lsposed.patch.util.Logger
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.measureTimedValue

class InstallerViewModel(
    private val context: Context,
    private val installManager: InstallManager,
    discordVersion: DiscordVersion
) : ScreenModel {

    val runner = StepRunner(discordVersion)

    val groupedSteps: ImmutableMap<StepGroup, List<Step>> = StepGroup.entries
        .associateWith { group ->
            runner.steps.filter { step -> step.group == group }
        }
        .toImmutableMap()

    private val installationRunning = AtomicBoolean(false)

    private val job = screenModelScope.launch(Dispatchers.Main) {
        if (installationRunning.getAndSet(true)) {
            return@launch
        }

        withContext(Dispatchers.IO) {
            runner.runAll()
        }
    }

    var backDialogOpened by mutableStateOf(false)
        private set

    var failedOnDownload by mutableStateOf(false)
        private set

    var expandedGroup by mutableStateOf<StepGroup?>(null)
        private set

    fun logError(msg: String?) {
        runner.logger.e("")
        runner.logger.e(msg)
    }

    fun copyDebugInfo() {
        context.copyText(runner.logger.logs.joinToString("\n"))
    }

    fun clearCache() {
        runner.clearCache()
        context.showToast(R.string.msg_cleared_cache)
    }

    fun openBackDialog() {
        backDialogOpened = true
    }

    fun closeBackDialog() {
        backDialogOpened = false
    }

    fun dismissDownloadFailedDialog() {
        failedOnDownload = false
    }

    fun expandGroup(group: StepGroup?) {
        expandedGroup = group
    }

    fun launchVendetta() {
        installManager.current?.let {
            val intent = context.packageManager.getLaunchIntentForPackage(it.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun cancelInstall() {
        runCatching {
            job.cancel("User exited the installer")
        }
    }

}