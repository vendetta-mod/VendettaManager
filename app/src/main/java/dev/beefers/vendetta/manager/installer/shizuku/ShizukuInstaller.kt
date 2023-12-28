package dev.beefers.vendetta.manager.installer.shizuku

import android.util.Log
import dev.beefers.vendetta.manager.installer.Installer
import dev.beefers.vendetta.manager.ui.activity.MainActivity
import java.io.File

internal class ShizukuInstaller : Installer {
    override suspend fun installApks(silent: Boolean, vararg apks: File) {
        val apkPaths = apks.map { it.absolutePath }
        Log.i("ShizukuInstaller", "Preparing to install APKs: $apkPaths")

        if (!MainActivity.serviceBinder?.asBinder()?.pingBinder()!!) {
            Log.e("ShizukuInstaller", "Service binder is not alive or null.")
            return
        }

        try {
            val installResult = MainActivity.serviceBinder!!.installApks(apkPaths)
            Log.i("ShizukuInstaller", "Install result: $installResult")
        } catch (e: Exception) {
            Log.e("ShizukuInstaller", "Exception during APK installation: ${e.message}", e)
        }
    }
}
