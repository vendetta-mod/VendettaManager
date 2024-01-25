package dev.beefers.vendetta.manager.installer.step

import android.content.Context
import android.os.Environment
import androidx.compose.runtime.Stable
import dev.beefers.vendetta.manager.domain.manager.PreferenceManager
import dev.beefers.vendetta.manager.installer.step.download.DownloadBaseStep
import dev.beefers.vendetta.manager.installer.step.download.DownloadLangStep
import dev.beefers.vendetta.manager.installer.step.download.DownloadLibsStep
import dev.beefers.vendetta.manager.installer.step.download.DownloadResourcesStep
import dev.beefers.vendetta.manager.installer.step.download.DownloadVendettaStep
import dev.beefers.vendetta.manager.installer.step.installing.InstallStep
import dev.beefers.vendetta.manager.installer.step.patching.AddVendettaStep
import dev.beefers.vendetta.manager.installer.step.patching.PatchManifestsStep
import dev.beefers.vendetta.manager.installer.step.patching.PresignApksStep
import dev.beefers.vendetta.manager.installer.step.patching.ReplaceIconStep
import dev.beefers.vendetta.manager.installer.util.Logger
import dev.beefers.vendetta.manager.utils.DiscordVersion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/**
 * Runs all installation steps in order
 *
 * Credit to rushii (github.com/rushiiMachine)
 */
@Stable
class StepRunner(
    private val discordVersion: DiscordVersion
): KoinComponent {

    private val preferenceManager: PreferenceManager by inject()
    private val context: Context by inject()

    val logger = Logger("StepRunner")

    private val cacheDir =
        context.externalCacheDir
        ?: File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS)
            .resolve("VendettaManager")
            .also { it.mkdirs() }

    private val discordCacheDir = cacheDir.resolve(discordVersion.toVersionCode())
    private val patchedDir = discordCacheDir.resolve("patched").also { it.deleteRecursively() }
    private val signedDir = discordCacheDir.resolve("signed").also { it.deleteRecursively() }
    private val lspatchedDir = patchedDir.resolve("lspatched").also { it.deleteRecursively() }

    /**
     * List of steps to go through for this install
     *
     * ORDER MATTERS
     */
    val steps: ImmutableList<Step> = buildList {
        // Downloading
        add(DownloadBaseStep(discordCacheDir, discordVersion.toVersionCode()))
        add(DownloadLibsStep(discordCacheDir, discordVersion.toVersionCode()))
        add(DownloadLangStep(discordCacheDir, discordVersion.toVersionCode()))
        add(DownloadResourcesStep(discordCacheDir, discordVersion.toVersionCode()))
        add(DownloadVendettaStep())

        // Patching
        if (preferenceManager.patchIcon) add(ReplaceIconStep())
        add(PatchManifestsStep())
        add(PresignApksStep(signedDir))
        add(AddVendettaStep(signedDir, lspatchedDir))

        // Installing
        add(InstallStep(lspatchedDir))
    }.toImmutableList()

    /**
     * Get a step that has already been successfully executed.
     * This is used to retrieve previously executed dependency steps from a later step.
     */
    inline fun <reified T : Step> getCompletedStep(): T {
        val step = steps.asSequence()
            .filterIsInstance<T>()
            .filter { it.status == StepStatus.SUCCESSFUL }
            .firstOrNull()

        if (step == null) {
            throw IllegalArgumentException("No completed step ${T::class.simpleName} exists in container")
        }

        return step
    }

    suspend fun runAll(): Throwable? {
        for (step in steps) {
            val error = step.runCatching(this)
            if (error != null) return error

            // Add delay for human psychology and
            // better group visibility in UI (the active group can change way too fast)
            if (!preferenceManager.isDeveloper && step.durationMs < 1000) {
                delay(1000L - step.durationMs)
            }
        }

        return null
    }

}