package dev.beefers.vendetta.manager.installer.shizuku

import android.content.Context
import dev.beefers.vendetta.manager.installer.Installer
import rikka.shizuku.Shizuku
import java.io.File
import java.util.UUID

class ShizukuInstaller(private val context: Context) : Installer {

    companion object {
        private val SESSION_ID_REGEX = Regex("(?<=\\[).+?(?=])")
    }

    override suspend fun installApks(silent: Boolean, vararg apks: File) {
        val tempDir = File("/data/local/tmp")
        val movedApks = mutableListOf<File>()

        // Copy each split to tmp
        apks.forEach {
            val moveCommand = "cp ${it.absolutePath} ${tempDir.absolutePath}"
            val moveResult = executeShellCommand(moveCommand)

            if(moveResult.isBlank())
                movedApks.add(File(tempDir.absolutePath, it.name))
            else
                throw RuntimeException("Failed to move ${it.absolutePath} to temp dir")
        }

        val installCommand = "pm install ${movedApks.joinToString(" ") { it.absolutePath }}"
        executeShellCommand(installCommand)

        movedApks.forEach {
            it.delete()
        }
    }

    private fun executeShellCommand(command: String): String {
        val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)

        val errorStr = process.errorStream.bufferedReader().use { it.readText().trim() }
        if(errorStr.isNotBlank()) throw RuntimeException("Failed to execute $command:\n\n$errorStr")

        return process.inputStream.bufferedReader().use { it.readText().trim() }
    }

}
