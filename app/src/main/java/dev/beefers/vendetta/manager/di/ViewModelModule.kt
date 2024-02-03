package dev.beefers.vendetta.manager.di

import dev.beefers.vendetta.manager.ui.viewmodel.home.HomeViewModel
import dev.beefers.vendetta.manager.ui.viewmodel.installer.InstallerViewModel
import dev.beefers.vendetta.manager.ui.viewmodel.installer.LogViewerViewModel
import dev.beefers.vendetta.manager.ui.viewmodel.settings.AdvancedSettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val viewModelModule = module {
    factoryOf(::InstallerViewModel)
    factoryOf(::AdvancedSettingsViewModel)
    factoryOf(::HomeViewModel)
    factoryOf(::LogViewerViewModel)
}