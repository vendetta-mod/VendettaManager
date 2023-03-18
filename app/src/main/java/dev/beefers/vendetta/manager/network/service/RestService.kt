package dev.beefers.vendetta.manager.network.service

import dev.beefers.vendetta.manager.network.dto.Index
import dev.beefers.vendetta.manager.network.dto.Release
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RestService(
    private val httpService: HttpService
) {

    suspend fun getLatestRelease(repo: String) = withContext(Dispatchers.IO) {
        httpService.request<Release> {
            url("https://api.github.com/repos/vendetta-mod/$repo/releases/latest")
        }
    }

    suspend fun getLatestDiscordVersions() = withContext(Dispatchers.IO) {
        httpService.request<Index> {
            url("https://discord.k6.tf/index.json")
        }
    }

}