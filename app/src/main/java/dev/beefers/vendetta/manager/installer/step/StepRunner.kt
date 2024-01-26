package dev.beefers.vendetta.manager.installer.step

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.beefers.vendetta.manager.BuildConfig
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
    private val debugInfo = """
            Vendetta Manager v${BuildConfig.VERSION_NAME}
            Built from commit ${BuildConfig.GIT_COMMIT} on ${BuildConfig.GIT_BRANCH} ${if (BuildConfig.GIT_LOCAL_CHANGES || BuildConfig.GIT_LOCAL_COMMITS) "(Changes Present)" else ""}
            
            Running Android ${Build.VERSION.RELEASE}, API level ${Build.VERSION.SDK_INT}
            Supported ABIs: ${Build.SUPPORTED_ABIS.joinToString()}
            Device: ${Build.MANUFACTURER} - ${Build.MODEL} (${Build.DEVICE})
            ${if(Build.VERSION.SDK_INT > Build.VERSION_CODES.S) "SOC: ${Build.SOC_MANUFACTURER} ${Build.SOC_MODEL}\n" else "\n\n"} 
            Adding Vendetta to Discord v$discordVersion
            
            
        """.trimIndent()

    val logger = Logger("StepRunner").also {
        debugInfo.split("\n").forEach(it.logs::add)
    }

    private val cacheDir =
        context.externalCacheDir
        ?: File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS)
            .resolve("VendettaManager")
            .also { it.mkdirs() }

    private val discordCacheDir = cacheDir.resolve(discordVersion.toVersionCode())
    private val patchedDir = discordCacheDir.resolve("patched").also { it.deleteRecursively() }
    private val signedDir = discordCacheDir.resolve("signed").also { it.deleteRecursively() }
    private val lspatchedDir = patchedDir.resolve("lspatched").also { it.deleteRecursively() }

    var currentStep by mutableStateOf<Step?>(null)
        private set

    var completed by mutableStateOf<Boolean>(false)
        private set

    /**
     * List of steps to go through for this install
     *
     * ORDER MATTERS
     */
    val steps: ImmutableList<Step> = buildList {
        // Downloading
        add(DownloadBaseStep(discordCacheDir, patchedDir, discordVersion.toVersionCode()))
        add(DownloadLibsStep(discordCacheDir, patchedDir, discordVersion.toVersionCode()))
        add(DownloadLangStep(discordCacheDir, patchedDir, discordVersion.toVersionCode()))
        add(DownloadResourcesStep(discordCacheDir, patchedDir, discordVersion.toVersionCode()))
        add(DownloadVendettaStep(patchedDir))

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

    fun clearCache() {
        cacheDir.deleteRecursively()
    }

    suspend fun runAll(): Throwable? {
        for (step in steps) {
            if (completed) return null // Failsafe in case runner is incorrectly marked as not completed too early

            currentStep = step
            val error = step.runCatching(this)
            if (error != null) {
                logger.i("\n")
                logger.e("Failed on ${step::class.simpleName}")
                logger.e(error.stackTraceToString())

                completed = true
                return error
            }

            // Add delay for human psychology and
            // better group visibility in UI (the active group can change way too fast)
            if (!preferenceManager.isDeveloper && step.durationMs < 1000) {
                delay(1000L - step.durationMs)
            }
        }

        completed = true
        return null
    }

}