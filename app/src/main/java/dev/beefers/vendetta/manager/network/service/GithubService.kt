package dev.beefers.vendetta.manager.network.service

import dev.beefers.vendetta.manager.network.dto.Release
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GithubService(
    private val httpService: HttpService
) {

    suspend fun getLatestRelease() = withContext(Dispatchers.IO) {
        httpService.request<Release> {
            url("https://api.github.com/repos/vendetta-mod/VendettaManager/releases/latest")
        }
    }

}