package com.edmik.parentapp.di

import com.edmik.parentapp.data.local.TokenManager
import com.russhwolf.settings.Settings
import com.edmik.parentapp.ui.login.LoginViewModel
import com.edmik.parentapp.ui.login.ForgotPasswordViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

expect fun createSettings(): Settings

val AppModule = module {
    single { createSettings() }
    single { TokenManager(get()) }
    single { AppStateManager() }
    
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { ForgotPasswordViewModel() }
}
