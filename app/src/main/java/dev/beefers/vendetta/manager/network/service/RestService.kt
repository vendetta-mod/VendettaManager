package dev.beefers.vendetta.manager.network.service

import dev.beefers.vendetta.manager.network.dto.Commit
import dev.beefers.vendetta.manager.network.dto.Index
import dev.beefers.vendetta.manager.network.dto.Release
import io.ktor.client.request.parameter
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
            url("https://vd.k6.tf/tracker/index")
        }
    }

    suspend fun getCommits(repo: String, page: Int = 1) = withContext(Dispatchers.IO) {
        httpService.request<List<Commit>> {
            url("https://api.github.com/repos/vendetta-mod/$repo/commits")
            parameter("page", page)
        }
    }

}