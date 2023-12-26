package dev.beefers.vendetta.manager.installer.shizuku

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import dev.beefers.vendetta.manager.installer.Installer
import dev.beefers.vendetta.manager.installer.session.InstallService
import java.io.File

internal class ShizukuInstaller(private val context: Context) : Installer {
    override fun installApks(silent: Boolean, vararg apks: File) {

    }
}