package dev.beefers.vendetta.manager.ui.viewmodel.installer

import android.content.Context
import cafe.adriel.voyager.core.model.ScreenModel
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.installer.util.LogEntry
import dev.beefers.vendetta.manager.utils.copyText
import dev.beefers.vendetta.manager.utils.showToast

class LogViewerViewModel(
    private val context: Context,
    val logs: List<LogEntry>
): ScreenModel {

    private val logsString by lazy {
        logs.joinToString("\n") { it.toString() }
    }

    val maxLogLength = logs.maxOf { it.message.length }

    fun copyLog(log: LogEntry) {
        context.copyText(log.toString())
        context.showToast(R.string.msg_copied)
    }

    fun copyLogs() {
        context.copyText(logsString)
        context.showToast(R.string.msg_copied)
    }

}