package com.example.freshtrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import android.app.Activity
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0063FF),
    onPrimary = Color.White,

    secondary = Color(0xFF5C6BC0),
    onSecondary = Color.White,

    background = Color(0xFFF4F4F4),
    onBackground = Color(0xFF1A1A1A),

    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),

    surfaceVariant = Color(0xFFEDEDED),
    onSurfaceVariant = Color(0xFF666666),

    error = Color(0xFFD32F2F),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF003258),

    secondary = Color(0xFF9FA8DA),
    onSecondary = Color(0xFF1A237E),

    background = Color(0xFF121212),
    onBackground = Color(0xFFECECEC),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFECECEC),

    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0B0B0),

    error = Color(0xFFEF9A9A),
    onError = Color(0xFF690005)
)

private val LightColors = lightColorScheme()
private val DarkColors = darkColorScheme()

/**
 * @param darkTheme
 *   true  → always dark
 *   false → always light
 *   null  → follow the system (default behaviour)
 */
@Composable
fun FreshTrackTheme(
    darkTheme: Boolean? = null,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = darkTheme ?: systemDark   // null = respect system

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}