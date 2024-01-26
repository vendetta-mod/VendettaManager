package dev.beefers.vendetta.manager.installer.step.download

import android.os.Build
import androidx.compose.runtime.Stable
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.installer.step.download.base.DownloadStep
import java.io.File

@Stable
class DownloadLangStep(
    dir: File,
    workingDir: File,
    version: String
): DownloadStep() {

    override val nameRes = R.string.step_dl_lang

    override val url: String = "$baseUrl/tracker/download/$version/config.en"
    override val destination = dir.resolve("config.en-$version.apk")
    override val workingCopy = workingDir.resolve("config.en-$version.apk")

}