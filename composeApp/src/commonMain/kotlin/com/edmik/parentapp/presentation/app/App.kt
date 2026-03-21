package com.edmik.parentapp.presentation.app

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.edmik.parentapp.presentation.navigation.AppNavHost
import com.edmik.parentapp.presentation.theme.ParentAppTheme

@Composable
fun App() {
    ParentAppTheme {
        val navController = rememberNavController()
        AppNavHost(navController = navController)
    }
}
