package dev.beefers.vendetta.manager.di

import dev.beefers.vendetta.manager.domain.repository.RestRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::RestRepository)
}