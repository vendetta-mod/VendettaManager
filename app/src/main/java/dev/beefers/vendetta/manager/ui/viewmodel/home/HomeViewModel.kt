package dev.beefers.vendetta.manager.ui.viewmodel.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import dev.beefers.vendetta.manager.domain.manager.InstallManager
import dev.beefers.vendetta.manager.domain.manager.PreferenceManager
import dev.beefers.vendetta.manager.domain.repository.RestRepository
import dev.beefers.vendetta.manager.network.utils.dataOrNull
import dev.beefers.vendetta.manager.utils.DiscordVersion
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: RestRepository,
    val context: Context,
    val prefs: PreferenceManager,
    val installManager: InstallManager
) : ScreenModel {

    var discordVersions by mutableStateOf<Map<DiscordVersion.Type, DiscordVersion?>?>(null)
        private set

    init {
        getDiscordVersions()
    }

    private fun getDiscordVersions() {
        coroutineScope.launch {
            discordVersions = repo.getLatestDiscordVersions().dataOrNull
        }
    }

    fun launchVendetta() {
        installManager.current?.let {
            val intent = context.packageManager.getLaunchIntentForPackage(it.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun uninstallVendetta() {
        installManager.uninstall()
    }

    fun launchVendettaInfo() {
        installManager.current?.let {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.parse("package:${it.packageName}")
                context.startActivity(this)
            }
        }

    }

}