package com.edmik.parentapp.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) =
    startKoin {
        appDeclaration()
        modules(
            AppModule,
            NetworkModule,
            RepositoryModule,
            UseCaseModule,
            DatabaseModule
        )
    }

