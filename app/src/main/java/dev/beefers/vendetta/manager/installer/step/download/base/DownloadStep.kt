package dev.beefers.vendetta.manager.installer.step.download.base

import android.content.Context
import androidx.compose.runtime.Stable
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.domain.manager.DownloadManager
import dev.beefers.vendetta.manager.domain.manager.DownloadResult
import dev.beefers.vendetta.manager.domain.manager.PreferenceManager
import dev.beefers.vendetta.manager.installer.step.Step
import dev.beefers.vendetta.manager.installer.step.StepGroup
import dev.beefers.vendetta.manager.installer.step.StepRunner
import dev.beefers.vendetta.manager.installer.step.StepStatus
import dev.beefers.vendetta.manager.utils.mainThread
import dev.beefers.vendetta.manager.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import java.io.File

@Stable
abstract class DownloadStep: Step() {

    private val downloadManager: DownloadManager by inject()
    private val context: Context by inject()

    abstract val url: String
    abstract val destination: File

    override val group: StepGroup = StepGroup.DL

    val preferenceManager: PreferenceManager by inject()
    val baseUrl = preferenceManager.mirror.baseUrl

    open suspend fun verify() {
        if (!destination.exists())
            error("Downloaded file is missing: ${destination.absolutePath}")

        if (destination.length() <= 0)
            error("Downloaded file is empty: ${destination.absolutePath}")
    }

    override suspend fun run(runner: StepRunner) {
        val fileName = destination.name
        runner.logger.i("Checking if $fileName is cached")
        if (destination.exists()) {
            runner.logger.i("Checking if $fileName isn't empty")
            if (destination.length() > 0) {
                runner.logger.i("vendetta.apk is cached")
                status = StepStatus.SUCCESSFUL
                return
            }

            runner.logger.i("Deleting empty file: $fileName")
            destination.delete()
        }

        runner.logger.i("$fileName was not properly cached, downloading now")
        val result = downloadManager.download(url, destination) { newProgress ->
            progress = newProgress
            runner.logger.d("$fileName download progress: $newProgress")
        }

        when (result) {
            is DownloadResult.Success -> {
                try {
                    runner.logger.i("Verifying downloaded file")
                    verify()
                    runner.logger.i("$fileName downloaded successfully")
                } catch (t: Throwable) {
                    mainThread {
                        context.showToast(R.string.msg_download_verify_failed)
                    }

                    throw t
                }
            }

            is DownloadResult.Error -> {
                withContext(Dispatchers.Main) {
                    context.showToast(R.string.msg_download_failed)
                }

                throw Error("Failed to download: ${result.debugReason}")
            }

            is DownloadResult.Cancelled -> {
                runner.logger.e("$fileName download cancelled")
                status = StepStatus.UNSUCCESSFUL
            }
        }
    }

}