package dev.beefers.vendetta.manager.di

import dev.beefers.vendetta.manager.network.service.GithubService
import dev.beefers.vendetta.manager.network.service.HttpService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val httpModule = module {

    fun provideJson() = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun provideHttpClient(json: Json) = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    singleOf(::provideJson)
    singleOf(::provideHttpClient)
    singleOf(::HttpService)
    singleOf(::GithubService)

}