package com.edmik.parentapp.di

import com.edmik.parentapp.data.repository.AuthRepositoryImpl
import com.edmik.parentapp.domain.repository.AuthRepository
import org.koin.dsl.module

val RepositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}
