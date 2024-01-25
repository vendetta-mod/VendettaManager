package dev.beefers.vendetta.manager.installer.util

import android.util.Log
import androidx.compose.runtime.Stable
import org.lsposed.patch.util.Logger

@Stable
class Logger(
    private val tag: String
): Logger() {

    val logs = mutableListOf<String>()

    override fun d(msg: String?) {
        logs += msg.toString()
        Log.d(tag, msg.toString())
    }

    override fun e(msg: String?) {
        logs += msg.toString()
        Log.e(tag, msg.toString())
    }

    override fun i(msg: String?) {
        logs += msg.toString()
        Log.i(tag, msg.toString())
    }

}