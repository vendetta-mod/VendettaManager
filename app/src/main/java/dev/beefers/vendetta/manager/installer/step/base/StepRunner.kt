package dev.beefers.vendetta.manager.installer.step.base

import android.content.Context
import android.os.Environment
import androidx.compose.runtime.Stable
import dev.beefers.vendetta.manager.domain.manager.PreferenceManager
import dev.beefers.vendetta.manager.installer.step.download.DownloadBaseStep
import dev.beefers.vendetta.manager.installer.step.download.DownloadLangStep
import dev.beefers.vendetta.manager.installer.step.download.DownloadLibsStep
import dev.beefers.vendetta.manager.installer.step.download.DownloadResourcesStep
import dev.beefers.vendetta.manager.utils.DiscordVersion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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

    private val cacheDir =
        context.externalCacheDir
        ?: File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS)
            .resolve("VendettaManager")
            .also { it.mkdirs() }

    private val discordCacheDir = cacheDir.resolve(discordVersion.toVersionCode())
    private val patchedDir = discordCacheDir.resolve("patched").also { it.deleteRecursively() }
    private val signedDir = discordCacheDir.resolve("signed").also { it.deleteRecursively() }
    private val lspatchedDir = patchedDir.resolve("lspatched").also { it.deleteRecursively() }

    val steps: ImmutableList<Step> = persistentListOf(
        DownloadBaseStep(discordCacheDir, discordVersion.toVersionCode()),
        DownloadLibsStep(discordCacheDir, discordVersion.toVersionCode()),
        DownloadLangStep(discordCacheDir, discordVersion.toVersionCode()),
        DownloadResourcesStep(discordCacheDir, discordVersion.toVersionCode()),
    )

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