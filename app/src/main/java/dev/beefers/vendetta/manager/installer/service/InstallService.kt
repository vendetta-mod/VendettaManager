package dev.beefers.vendetta.manager.installer.service

import android.app.Service
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.IBinder
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.domain.manager.InstallManager
import dev.beefers.vendetta.manager.utils.showToast
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class InstallService : Service(), KoinComponent {

    private val installManager: InstallManager by inject()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val isInstall = intent.action == "vendetta.actions.ACTION_INSTALL"
        when (val statusCode = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                @Suppress("DEPRECATION") // No.
                val confirmationIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(confirmationIntent)
            }

            PackageInstaller.STATUS_SUCCESS -> {
                if (isInstall) showToast(R.string.installer_success)
                installManager.getInstalled()
            }

            PackageInstaller.STATUS_FAILURE_ABORTED -> if (isInstall) showToast(R.string.installer_aborted)

            else -> {
                if (isInstall) showToast(R.string.installer_failed, statusCode)
            }
        }

        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

}