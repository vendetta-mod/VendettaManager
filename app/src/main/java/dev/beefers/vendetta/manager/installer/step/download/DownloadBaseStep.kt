package dev.beefers.vendetta.manager.installer.step.download

import androidx.compose.runtime.Stable
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.installer.step.download.base.DownloadStep
import java.io.File

@Stable
class DownloadBaseStep(
    dir: File,
    version: String
): DownloadStep() {

    override val nameRes = R.string.step_dl_base

    override val destination = dir.resolve("base-$version.apk")

    override val url: String = "$baseUrl/tracker/download/$version/base"

}