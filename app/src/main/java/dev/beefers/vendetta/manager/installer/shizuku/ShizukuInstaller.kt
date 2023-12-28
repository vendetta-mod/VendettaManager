package dev.beefers.vendetta.manager.installer.shizuku

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dev.beefers.vendetta.manager.installer.Installer
import dev.beefers.vendetta.manager.installer.session.InstallService
import rikka.shizuku.Shizuku
import java.io.File

internal class ShizukuInstaller(private val context: Context) : Installer {
        companion object {
                private val SESSION_ID_REGEX = Regex("(?<=\\[).+?(?=])")
        }

        override suspend fun installApks(silent: Boolean, vararg apks: File) {
                apks.forEach { apk ->
                        val apkPath = apk.absolutePath
                        var sessionId: String? = null
                        var resultMessage = ""
                        try {
                                val size = apk.length()
                                val createCommand = "pm install-create -r -S $size"
                                val createResult = executeShellCommand(createCommand)
                                sessionId = SESSION_ID_REGEX.find(createResult)?.value
                                        ?: throw RuntimeException("Failed to create install session for APK: $apkPath")
                                resultMessage += "Create session result: $createResult\n"

                                val writeCommand = "pm install-write -S $size $sessionId base $apkPath"
                                val writeResult = executeShellCommand(writeCommand)
                                if (writeResult.contains("Failure")) {
                                        throw RuntimeException("Failed to write APK to session $sessionId: $writeResult")
                                }
                                resultMessage += "Write APK result: $writeResult\n"

                                val commitCommand = "pm install-commit $sessionId"
                                val commitResult = executeShellCommand(commitCommand)
                                if (commitResult.contains("Failure")) {
                                        throw RuntimeException("Failed to commit install session $sessionId: $commitResult")
                                }
                                resultMessage += "Commit session result: $commitResult\n"

                                Log.i("ShizukuInstaller", "Successfully installed $apkPath")
                                triggerStatusIntent("Success", resultMessage, "")
                        } catch (e: Exception) {
                                Log.e("ShizukuInstaller", "Error installing APK $apkPath: ${e.message}")
                                triggerStatusIntent("Failure", resultMessage, e.message ?: "")
                                if (sessionId != null) {
                                        executeShellCommand("pm install-abandon $sessionId")
                                }
                                throw e
                        }
                }
        }

        private fun executeShellCommand(command: String): String {
                val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
                return process.inputStream.bufferedReader().use { it.readText().trim() }
        }

        private fun triggerStatusIntent(status: String, result: String, error: String) {
                val callbackIntent = Intent(context, InstallService::class.java).apply {
                        action = "vendetta.actions.ACTION_INSTALL"
                        putExtra("status", status)
                        putExtra("result", result)
                        putExtra("error", error)
                }

                val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_IMMUTABLE
                } else {
                        0
                }

                val contentIntent = PendingIntent.getService(context, 0, callbackIntent, pendingIntentFlag)
                contentIntent.send()
        }
}
