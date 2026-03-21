package com.edmik.parentapp.di

import com.edmik.parentapp.domain.usecase.auth.LoginUseCase
import com.edmik.parentapp.domain.usecase.auth.ForgotPasswordUseCase
import org.koin.dsl.module

val UseCaseModule = module {
    single { LoginUseCase(get()) }
    single { ForgotPasswordUseCase(get()) }
}
