package dev.beefers.vendetta.manager.installer.step.download

import android.os.Build
import androidx.compose.runtime.Stable
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.installer.step.download.base.DownloadStep
import java.io.File

@Stable
class DownloadVendettaStep: DownloadStep() {

    override val nameRes = R.string.step_dl_vd

    override val destination = preferenceManager.moduleLocation

    override val url: String = "https://github.com/vendetta-mod/VendettaXposed/releases/latest/download/app-release.apk"

}