package dev.beefers.vendetta.manager.installer

import java.io.File

interface Installer {
    fun installApks(silent: Boolean = false, vararg apks: File)
}