package com.dalapenko.laba.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC8ABF3),
    onPrimary = Color(0xFF211C27),
    primaryContainer = Color(0xFF352A48),
    onPrimaryContainer = Color(0xFFF5F2F8),
    secondary = Color(0xFFBEB7C7),
    onSecondary = Color(0xFF211C27),
    secondaryContainer = Color(0xFF45404D),
    onSecondaryContainer = Color(0xFFF5F2F8),
    background = Color(0xFF111016),
    onBackground = Color(0xFFF5F2F8),
    surface = Color(0xFF211E27),
    onSurface = Color(0xFFF5F2F8),
    surfaceVariant = Color(0xFF45404D),
    onSurfaceVariant = Color(0xFFBEB7C7),
    surfaceTint = Color(0xFFC8ABF3),
    outline = Color(0xFF45404D),
    outlineVariant = Color(0xFF45404D),
    surfaceContainerLowest = Color(0xFF111016),
    surfaceContainerLow = Color(0xFF211E27),
    surfaceContainer = Color(0xFF211E27),
    surfaceContainerHigh = Color(0xFF211E27),
    surfaceContainerHighest = Color(0xFF211E27),
    error = Color(0xFFE1A4A0),
    onError = Color(0xFF211C27),
    errorContainer = Color(0xFF633F4A),
    onErrorContainer = Color(0xFFF5F2F8),
)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6748C7),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE4DDE9),
    onPrimaryContainer = Color(0xFF211C27),
    secondary = Color(0xFF706978),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE4DDE9),
    onSecondaryContainer = Color(0xFF211C27),
    background = Color(0xFFF7F4EF),
    onBackground = Color(0xFF211C27),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF211C27),
    surfaceVariant = Color(0xFFE4DDE9),
    onSurfaceVariant = Color(0xFF706978),
    surfaceTint = Color(0xFF6748C7),
    outline = Color(0xFFE4DDE9),
    outlineVariant = Color(0xFFE4DDE9),
    surfaceContainerLowest = Color(0xFFF7F4EF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFFFFFFF),
    error = Color(0xFF8B6067),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFE1A4A0),
    onErrorContainer = Color(0xFF211C27),
)

@Composable
fun LabaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
