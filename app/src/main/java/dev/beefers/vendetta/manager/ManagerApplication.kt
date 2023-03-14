package dev.beefers.vendetta.manager

import android.app.Application
import dev.beefers.vendetta.manager.di.httpModule
import dev.beefers.vendetta.manager.di.managerModule
import dev.beefers.vendetta.manager.di.repositoryModule
import dev.beefers.vendetta.manager.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ManagerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ManagerApplication)
            modules(
                httpModule,
                managerModule,
                viewModelModule,
                repositoryModule
            )
        }
    }

}