package com.edmik.parentapp

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.edmik.parentapp.ui.navigation.AppNavHost
import com.edmik.parentapp.ui.theme.ParentAppTheme

@Composable
fun App() {
    ParentAppTheme {
        val navController = rememberNavController()
        AppNavHost(navController = navController)
    }
}