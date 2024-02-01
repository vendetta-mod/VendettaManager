package dev.beefers.vendetta.manager.installer.step.patching

import android.content.Context
import com.github.diamondminer88.zip.ZipWriter
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.installer.step.Step
import dev.beefers.vendetta.manager.installer.step.StepGroup
import dev.beefers.vendetta.manager.installer.step.StepRunner
import dev.beefers.vendetta.manager.installer.step.download.DownloadBaseStep
import org.koin.core.component.inject

/**
 * Replaces the existing app icons with Vendetta tinted ones
 */
class ReplaceIconStep : Step() {

    val context: Context by inject()

    override val group = StepGroup.PATCHING
    override val nameRes = R.string.step_change_icon

    override suspend fun run(runner: StepRunner) {
        val baseApk = runner.getCompletedStep<DownloadBaseStep>().workingCopy

        ZipWriter(baseApk, true).use { apk ->
            runner.logger.i("Replacing icons in ${baseApk.name}")

            val mipmaps =
                arrayOf("mipmap-xhdpi-v4", "mipmap-xxhdpi-v4", "mipmap-xxxhdpi-v4")
            val icons = arrayOf(
                "ic_logo_foreground.png",
                "ic_logo_square.png",
                "ic_logo_foreground.png"
            )

            for (icon in icons) {
                val newIcon = context.assets.open("icons/$icon")
                    .use { it.readBytes() }

                for (mipmap in mipmaps) {
                    runner.logger.i("Replacing $mipmap with $icon")
                    val path = "res/$mipmap/$icon"
                    apk.deleteEntry(path)
                    apk.writeEntry(path, newIcon)
                }
            }
        }
    }

}