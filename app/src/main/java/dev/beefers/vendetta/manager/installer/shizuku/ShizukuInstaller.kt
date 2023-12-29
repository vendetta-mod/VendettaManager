package dev.beefers.vendetta.manager.installer.shizuku

import android.content.Context
import dev.beefers.vendetta.manager.installer.Installer
import rikka.shizuku.Shizuku
import java.io.File

internal class ShizukuInstaller(private val context: Context) : Installer {
        companion object {
                private val SESSION_ID_REGEX = Regex("(?<=\\[).+?(?=])")
        }

        override suspend fun installApks(silent: Boolean, vararg apks: File) {
                var sessionId: String? = null
                try {
                        val createCommand = "pm install-create -r -S ${apks.sumOf { it.length() }}"
                        val createResult = executeShellCommand(createCommand)
                        sessionId = SESSION_ID_REGEX.find(createResult)?.value
                                ?: throw RuntimeException("Failed to create install session")

                        apks.forEach { apk ->
                                val writeCommand = "pm install-write -S ${apk.length()} $sessionId ${apk.name} ${apk.absolutePath}"
                                val writeResult = executeShellCommand(writeCommand)
                                if (writeResult.contains("Failure")) {
                                        throw RuntimeException("Failed to write APK: $writeResult")
                                }
                        }

                        val commitCommand = "pm install-commit $sessionId"
                        val commitResult = executeShellCommand(commitCommand)
                        if (commitResult.contains("Failure")) {
                                throw RuntimeException("Failed to commit install session: $commitResult")
                        }
                } catch (e: Exception) {
                        if (sessionId != null) {
                                executeShellCommand("pm install-abandon $sessionId")
                        }
                        throw e
                }
        }

        private fun executeShellCommand(command: String): String {
                val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
                return process.inputStream.bufferedReader().use { it.readText().trim() }
        }
}
