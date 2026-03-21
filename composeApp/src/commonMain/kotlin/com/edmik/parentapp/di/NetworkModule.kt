package com.edmik.parentapp.di

import com.edmik.parentapp.data.remote.api.HttpClientFactory
import com.edmik.parentapp.data.remote.api.AuthApiService
import org.koin.dsl.module

val NetworkModule = module {
    single { HttpClientFactory(get(), isDebug = true) }
    single { AuthApiService(get()) }
}

