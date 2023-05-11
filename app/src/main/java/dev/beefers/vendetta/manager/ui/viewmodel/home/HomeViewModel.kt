package dev.beefers.vendetta.manager.ui.viewmodel.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import dev.beefers.vendetta.manager.domain.manager.InstallManager
import dev.beefers.vendetta.manager.domain.manager.PreferenceManager
import dev.beefers.vendetta.manager.domain.repository.RestRepository
import dev.beefers.vendetta.manager.network.dto.Commit
import dev.beefers.vendetta.manager.network.utils.ApiResponse
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

    val commits = Pager(PagingConfig(pageSize = 30)) {
        object : PagingSource<Int, Commit>() {
            override fun getRefreshKey(state: PagingState<Int, Commit>): Int? =
                state.anchorPosition?.let {
                    state.closestPageToPosition(it)?.prevKey
                }

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Commit> {
                val page = params.key ?: 0

                return when (val response = repo.getCommits("Vendetta", page)) {
                    is ApiResponse.Success -> LoadResult.Page(
                        data = response.data,
                        prevKey = if (page > 0) page - 1 else null,
                        nextKey = if (response.data.isNotEmpty()) page + 1 else null
                    )

                    is ApiResponse.Failure -> LoadResult.Error(response.error)
                    is ApiResponse.Error -> LoadResult.Error(response.error)
                }
            }
        }
    }.flow.cachedIn(coroutineScope)

    init {
        getDiscordVersions()
    }

    fun getDiscordVersions() {
        coroutineScope.launch {
            discordVersions = repo.getLatestDiscordVersions().dataOrNull
            if (prefs.autoClearCache) autoClearCache()
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

    private fun autoClearCache() {
        val currentVersion =
            DiscordVersion.fromVersionCode(installManager.current?.versionCode.toString()) ?: return
        val latestVersion = when {
            prefs.discordVersion.isBlank() -> discordVersions?.get(prefs.channel)
            else -> DiscordVersion.fromVersionCode(prefs.discordVersion)
        } ?: return

        if (latestVersion > currentVersion) {
            for (file in (context.externalCacheDir ?: context.cacheDir).listFiles()
                ?: emptyArray()) {
                if (file.isDirectory) file.deleteRecursively()
            }
        }
    }

}