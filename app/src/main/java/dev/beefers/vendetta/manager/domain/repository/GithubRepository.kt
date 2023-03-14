package dev.beefers.vendetta.manager.domain.repository

import dev.beefers.vendetta.manager.network.service.GithubService

class GithubRepository(
    private val service: GithubService
) {

    suspend fun getLatestRelease() = service.getLatestRelease()

}