package dev.beefers.vendetta.manager.installer.util

import dev.beefers.vendetta.manager.network.utils.Signer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lsposed.lspatch.share.PatchConfig
import org.lsposed.patch.LSPatch
import org.lsposed.patch.util.Logger
import java.io.File

object Patcher {

    class Options(
        private val config: PatchConfig,
        private val outputDir: File,
        private val apkPaths: List<String>,
        private val embeddedModules: List<String>?
    ) {

        fun toStringArray(): Array<String> {
            return buildList {
                addAll(apkPaths)
                add("-o"); add(outputDir.absolutePath)
                if (config.debuggable) add("-d")
                add("-l"); add(config.sigBypassLevel.toString())
                if (config.useManager) add("--manager")
                if (config.overrideVersionCode) add("-r")
                add("-v")
                embeddedModules?.forEach {
                    add("-m"); add(it)
                }
                addAll(arrayOf("-k", Signer.keyStore.absolutePath, "password", "alias", "password"))
            }.toTypedArray()
        }

    }

    suspend fun patch(
        logger: Logger,
        outputDir: File,
        apkPaths: List<String>,
        embeddedModules: List<String>
    ) {
        withContext(Dispatchers.IO) {
            LSPatch(
                logger,
                *apkPaths.toTypedArray(),
                "-o",
                outputDir.absolutePath,
                "-l",
                "2",
                "-v",
                "-m",
                *embeddedModules.toTypedArray(),
                "-k",
                Signer.keyStore.absolutePath,
                "password",
                "alias",
                "password"
            ).doCommandLine()
        }
    }

}