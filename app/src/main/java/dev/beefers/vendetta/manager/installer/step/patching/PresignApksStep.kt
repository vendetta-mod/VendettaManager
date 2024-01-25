package dev.beefers.vendetta.manager.installer.step.patching

import android.os.Build
import com.github.diamondminer88.zip.ZipCompression
import com.github.diamondminer88.zip.ZipReader
import com.github.diamondminer88.zip.ZipWriter
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.domain.manager.PreferenceManager
import dev.beefers.vendetta.manager.installer.step.Step
import dev.beefers.vendetta.manager.installer.step.StepGroup
import dev.beefers.vendetta.manager.installer.step.StepRunner
import dev.beefers.vendetta.manager.installer.step.download.DownloadBaseStep
import dev.beefers.vendetta.manager.installer.step.download.DownloadLangStep
import dev.beefers.vendetta.manager.installer.step.download.DownloadLibsStep
import dev.beefers.vendetta.manager.installer.step.download.DownloadResourcesStep
import dev.beefers.vendetta.manager.installer.util.ManifestPatcher
import dev.beefers.vendetta.manager.installer.util.Signer
import org.koin.core.component.inject
import java.io.File

class PresignApksStep(
    private val signedDir: File
) : Step() {

    override val group = StepGroup.PATCHING
    override val nameRes = R.string.step_signing

    override suspend fun run(runner: StepRunner) {
        val baseApk = runner.getCompletedStep<DownloadBaseStep>().destination
        val libsApk = runner.getCompletedStep<DownloadLibsStep>().destination
        val langApk = runner.getCompletedStep<DownloadLangStep>().destination
        val resApk = runner.getCompletedStep<DownloadResourcesStep>().destination

        signedDir.mkdirs()
        val apks = listOf(baseApk, libsApk, langApk, resApk)

        // Align resources.arsc due to targeting api 30 for silent install
        if(Build.VERSION.SDK_INT >= 30) {
            for (file in apks) {
                val bytes = ZipReader(file).use {
                    if (it.entryNames.contains("resources.arsc")) {
                        it.openEntry("resources.arsc")?.read()
                    } else {
                        null
                    }
                } ?: continue

                ZipWriter(file, true).use {
                    it.deleteEntry("resources.arsc", true)
                    it.writeEntry("resources.arsc", bytes, ZipCompression.NONE, 4096)
                }
            }
        }

        apks.forEach {
            Signer.signApk(it, File(signedDir, it.name))
        }
    }

}