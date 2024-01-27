package dev.beefers.vendetta.manager.installer.util

import android.util.Log
import androidx.compose.runtime.Stable
import org.lsposed.patch.util.Logger

/**
 * Used to log events done during the patching process
 *
 * @param tag The tag to use for logcat
 */
@Stable
class Logger(
    private val tag: String
): Logger() {

    /**
     * All logs made with this [Logger]
     */
    val logs = mutableListOf<String>()

    /**
     * Prints a debug message to logcat and stores it in [logs]
     */
    override fun d(msg: String?) {
        logs += msg.toString()
        Log.d(tag, msg.toString())
    }

    /**
     * Prints an info message to logcat and stores it in [logs]
     */
    override fun i(msg: String?) {
        logs += msg.toString()
        Log.i(tag, msg.toString())
    }

    /**
     * Prints an error message to logcat and stores it in [logs]
     */
    override fun e(msg: String?) {
        logs += msg.toString()
        Log.e(tag, msg.toString())
    }

    /**
     * Prints an error message and stacktrace with a preceding empty line.
     */
    fun e(msg: String?, th: Throwable?) {
        newline()
        e(msg)
        if(th != null) e(th.stackTraceToString())
    }

    /**
     * Prints an empty line
     */
    private fun newline() {
        i("\n")
    }

}