package dev.beefers.vendetta.manager.di

import dev.beefers.vendetta.manager.domain.manager.DownloadManager
import dev.beefers.vendetta.manager.domain.manager.InstallManager
import dev.beefers.vendetta.manager.domain.manager.PreferenceManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val managerModule = module {
    singleOf(::DownloadManager)
    singleOf(::PreferenceManager)
    singleOf(::InstallManager)
}