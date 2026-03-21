package com.edmik.parentapp.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = CardBackground,
    secondary = PrimaryBlue,
    onSecondary = CardBackground,
    background = BackgroundGradEnd,
    surface = CardBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorColor,
    onError = CardBackground
)

@Composable
fun ParentAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
