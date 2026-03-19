package com.edmik.parentapp.di

import com.edmik.parentapp.data.api.ApiClient
import com.edmik.parentapp.data.api.AuthService
import org.koin.dsl.module

val NetworkModule = module {
    single {
        ApiClient(get(), isDebug = true)
    }
    single {
        AuthService(get())
    }
}
