package dev.beefers.vendetta.manager.installer.step.download

import android.os.Build
import androidx.compose.runtime.Stable
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.installer.step.download.base.DownloadStep
import java.io.File

@Stable
class DownloadResourcesStep(
    dir: File,
    version: String
): DownloadStep() {

    override val nameRes = R.string.step_dl_res

    override val destination = dir.resolve("config.xxhdpi-$version.apk")

    override val url: String = "$baseUrl/tracker/download/$version/config.xxhdpi"

}