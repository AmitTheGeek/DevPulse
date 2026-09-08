package com.devpulse.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C4F),
    onPrimary = Color.White,
    background = Color(0xFFFAFCF8),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFAFCF8),
    onSurface = Color(0xFF191C1A),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF74DBB2),
    onPrimary = Color(0xFF003828),
    background = Color(0xFF101412),
    onBackground = Color(0xFFE1E3DF),
    surface = Color(0xFF101412),
    onSurface = Color(0xFFE1E3DF),
)

@Composable
fun DevPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography(),
        content = content,
    )
}

