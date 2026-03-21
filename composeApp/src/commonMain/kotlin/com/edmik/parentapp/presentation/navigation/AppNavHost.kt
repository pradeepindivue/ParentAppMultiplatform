package com.edmik.parentapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.edmik.parentapp.data.local.database.TokenManager
import com.edmik.parentapp.presentation.components.ComingSoonScreen
import com.edmik.parentapp.presentation.screens.login.LoginScreen
import com.edmik.parentapp.presentation.screens.forgot_password.ForgotPasswordScreen
import org.koin.compose.koinInject

@Composable
fun AppNavHost(navController: NavHostController) {
    val tokenManager: TokenManager = koinInject()
    val startDestination = if (tokenManager.getAccessToken() == null) {
        Routes.LOGIN
    } else {
        Routes.DASHBOARD
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                }
            )
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.DASHBOARD) { ComingSoonScreen("Dashboard") }
        composable(Routes.ATTENDANCE) { ComingSoonScreen("Attendance") }
        composable(Routes.ATTENDANCE_SUBJECT) { ComingSoonScreen("Attendance Subject") }
        composable(Routes.FEES) { ComingSoonScreen("Fees") }
        composable(Routes.FEE_DETAIL) { ComingSoonScreen("Fee Detail") }
        composable(Routes.PAYMENT_RESULT) { ComingSoonScreen("Payment Result") }
        composable(Routes.NOTIFICATIONS) { ComingSoonScreen("Notifications") }
        composable(Routes.NOTIFICATION_PREFS) { ComingSoonScreen("Notification Prefs") }
        composable(Routes.LEAVE_APPLY) { ComingSoonScreen("Apply Leave") }
        composable(Routes.LEAVE_HISTORY) { ComingSoonScreen("Leave History") }
        composable(Routes.LEAVE_DETAIL) { ComingSoonScreen("Leave Detail") }
        composable(Routes.MESSAGES) { ComingSoonScreen("Messages") }
        composable(Routes.MESSAGE_THREAD) { ComingSoonScreen("Message Thread") }
        composable(Routes.ANNOUNCEMENTS) { ComingSoonScreen("Announcements") }
        composable(Routes.ANALYTICS) { ComingSoonScreen("Analytics Dashboard") }
        composable(Routes.ANALYTICS_SUBJECT) { ComingSoonScreen("Analytics Subject") }
        composable(Routes.CALENDAR) { ComingSoonScreen("Calendar") }
    }
}
