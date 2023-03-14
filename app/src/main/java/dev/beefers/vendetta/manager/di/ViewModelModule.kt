package dev.beefers.vendetta.manager.di

import dev.beefers.vendetta.manager.ui.viewmodel.installer.InstallerViewModel
import dev.beefers.vendetta.manager.ui.viewmodel.main.MainViewModel
import dev.beefers.vendetta.manager.ui.viewmodel.settings.SettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val viewModelModule = module {
    factoryOf(::InstallerViewModel)
    factoryOf(::SettingsViewModel)
    factoryOf(::MainViewModel)
}