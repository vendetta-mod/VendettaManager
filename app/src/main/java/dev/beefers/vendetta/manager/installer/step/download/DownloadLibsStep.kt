package dev.beefers.vendetta.manager.installer.step.download

import android.os.Build
import androidx.compose.runtime.Stable
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.installer.step.download.base.DownloadStep
import java.io.File

/**
 * Downloads the split containing the native libraries for the current devices architecture
 */
@Stable
class DownloadLibsStep(
    dir: File,
    workingDir: File,
    version: String
): DownloadStep() {

    /**
     * Supported CPU architecture for this device, used to download the correct library split
     */
    private val arch = Build.SUPPORTED_ABIS.first().replace("-v", "_v")

    override val nameRes = R.string.step_dl_lib

    override val url: String = "$baseUrl/tracker/download/$version/config.$arch"
    override val destination = dir.resolve("config.$arch-$version.apk")
    override val workingCopy = workingDir.resolve("config.$arch-$version.apk")

}