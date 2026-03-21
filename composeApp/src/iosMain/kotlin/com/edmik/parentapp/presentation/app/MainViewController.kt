package com.edmik.parentapp.presentation.app

import androidx.compose.ui.window.ComposeUIViewController
import com.edmik.parentapp.di.initKoin
import com.edmik.parentapp.presentation.app.App

private var koinInitialized = false

fun MainViewController() = ComposeUIViewController(
    configure = {
        if (!koinInitialized) {
            initKoin()
            koinInitialized = true
        }
    }
) { 
    App() 
}