package dev.beefers.vendetta.manager.installer.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import dev.beefers.vendetta.manager.BuildConfig
import dev.beefers.vendetta.manager.installer.Installer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku
import java.io.File

internal class ShizukuInstaller(private val context: Context) : Installer {
    private var serviceBinder: IShizukuInstallerService? = null
    private val serviceConnected = CompletableDeferred<Unit>()

    private val userServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            if (binder.pingBinder()) {
                serviceBinder = IShizukuInstallerService.Stub.asInterface(binder)
                serviceConnected.complete(Unit)
                Log.i("ShizukuInstaller", "Service connected")
            } else {
                Log.e("ShizukuInstaller", "Invalid binder received")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBinder = null
            Log.i("ShizukuInstaller", "Service disconnected")
        }
    }

    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(
            BuildConfig.APPLICATION_ID,
            ShizukuInstallerService::class.java.getName()
        )
    )
        .daemon(false)
        .processNameSuffix("service")
        .debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)

    private fun bindService() {
        if (Shizuku.getVersion() >= 10) {
            Shizuku.bindUserService(userServiceArgs, userServiceConnection)
        }
    }

    private fun unbindService() {
        if (Shizuku.getVersion() >= 10) {
            Shizuku.unbindUserService(userServiceArgs, userServiceConnection, true)
        }
    }

    override suspend fun installApks(silent: Boolean, vararg apks: File) {
        bindService()

        withTimeoutOrNull(5000) {
            serviceConnected.await()
        }

        if (serviceBinder == null) {
            Log.e("ShizukuInstaller", "Service binder is not connected.")
            unbindService()
            return
        }

        val apkPaths = apks.map { it.absolutePath }
        Log.i("ShizukuInstaller", "Preparing to install APKs: $apkPaths")

        try {
            val installResult = serviceBinder!!.installApks(apkPaths)
            Log.i("ShizukuInstaller", "Install result: $installResult")
        } catch (e: Exception) {
            Log.e("ShizukuInstaller", "Exception during APK installation: ${e.message}", e)
        } finally {
            unbindService()
        }
    }
}
