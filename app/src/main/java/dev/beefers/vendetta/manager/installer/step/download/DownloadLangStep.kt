package dev.beefers.vendetta.manager.installer.step.download

import android.os.Build
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.installer.step.download.base.DownloadStep
import java.io.File

class DownloadLangStep(
    dir: File,
    version: String
): DownloadStep() {

    override val nameRes = R.string.step_dl_base

    override val destination = dir.resolve("config.en-$version.apk")

    override val url: String = "$baseUrl/tracker/download/$version/config.en"

}