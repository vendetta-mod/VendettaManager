package dev.beefers.vendetta.manager.domain.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.beefers.vendetta.manager.domain.manager.InstallManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class InstallReceiver : BroadcastReceiver(), KoinComponent {

    private val installManager: InstallManager by inject()

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        installManager.getInstalled()
    }

}