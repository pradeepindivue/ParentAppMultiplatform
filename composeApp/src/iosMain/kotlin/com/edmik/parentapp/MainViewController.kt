package com.edmik.parentapp

import androidx.compose.ui.window.ComposeUIViewController
import com.edmik.parentapp.di.initKoin

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