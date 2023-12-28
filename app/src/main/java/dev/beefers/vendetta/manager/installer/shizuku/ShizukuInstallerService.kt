package dev.beefers.vendetta.manager.installer.shizuku

import android.content.Context
import android.os.Parcel
import android.util.Log
import rikka.shizuku.Shizuku
import kotlin.system.exitProcess

class ShizukuInstallerService(private val context: Context) : IShizukuInstallerService.Stub() {
    /**
     * Reserved destroy method
     */
    override fun destroy() {
        Log.i("ShizukuInstallerService", "destroy")
        exitProcess(0)
    }

    override fun exit() {
        destroy()
    }

    override fun installApks(apkPaths: List<String>): String {
        val command = buildInstallCommand(apkPaths)

        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken("shizuku")
            data.writeStringList(command)

            Shizuku.transactRemote(data, reply, 0);

            reply.readException()
            val result = reply.readString() ?: ""
            Log.i("ShizukuInstallerService", "Install result: $result")
            return result
        } catch (e: Exception) {
            Log.e("ShizukuInstallerService", "Exception in installApks: ${e.message}")
            return "Exception occurred: ${e.message}"
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    private fun buildInstallCommand(apkPaths: List<String>): List<String> {
        return listOf("pm", "install", "-r", *apkPaths.toTypedArray())
    }
}
