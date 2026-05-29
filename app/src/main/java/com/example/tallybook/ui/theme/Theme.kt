package com.example.tallybook.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 二次元风格配色方案
val AnimePink = Color(0xFFFF6B9D)
val AnimePurple = Color(0xFF9C27B0)
val AnimeBlue = Color(0xFF64B5F6)
val AnimeCyan = Color(0xFF4DD0E1)
val AnimeGreen = Color(0xFF81C784)
val AnimeYellow = Color(0xFFFFD54F)
val AnimeOrange = Color(0xFFFFB74D)
val AnimeRed = Color(0xFFE57373)

val AnimeBackground = Color(0xFFF8F0F8)
val AnimeSurface = Color(0xFFFFFFFF)
val AnimeOnBackground = Color(0xFF2D2D2D)
val AnimeOnSurface = Color(0xFF2D2D2D)

val AnimeBackgroundDark = Color(0xFF1A1A2E)
val AnimeSurfaceDark = Color(0xFF16213E)
val AnimeOnBackgroundDark = Color(0xFFEAEAEA)
val AnimeOnSurfaceDark = Color(0xFFEAEAEA)

private val LightColorScheme = lightColorScheme(
    primary = AnimePink,
    onPrimary = Color.White,
    primaryContainer = AnimePink.copy(alpha = 0.1f),
    onPrimaryContainer = AnimePink,
    secondary = AnimePurple,
    onSecondary = Color.White,
    secondaryContainer = AnimePurple.copy(alpha = 0.1f),
    onSecondaryContainer = AnimePurple,
    tertiary = AnimeCyan,
    onTertiary = Color.White,
    tertiaryContainer = AnimeCyan.copy(alpha = 0.1f),
    onTertiaryContainer = AnimeCyan,
    background = AnimeBackground,
    onBackground = AnimeOnBackground,
    surface = AnimeSurface,
    onSurface = AnimeOnSurface,
    surfaceVariant = AnimePink.copy(alpha = 0.05f),
    onSurfaceVariant = AnimeOnSurface,
    outline = AnimePink.copy(alpha = 0.3f)
)

private val DarkColorScheme = darkColorScheme(
    primary = AnimePink,
    onPrimary = Color.White,
    primaryContainer = AnimePink.copy(alpha = 0.2f),
    onPrimaryContainer = AnimePink,
    secondary = AnimePurple,
    onSecondary = Color.White,
    secondaryContainer = AnimePurple.copy(alpha = 0.2f),
    onSecondaryContainer = AnimePurple,
    tertiary = AnimeCyan,
    onTertiary = Color.White,
    tertiaryContainer = AnimeCyan.copy(alpha = 0.2f),
    onTertiaryContainer = AnimeCyan,
    background = AnimeBackgroundDark,
    onBackground = AnimeOnBackgroundDark,
    surface = AnimeSurfaceDark,
    onSurface = AnimeOnSurfaceDark,
    surfaceVariant = AnimePink.copy(alpha = 0.1f),
    onSurfaceVariant = AnimeOnSurfaceDark,
    outline = AnimePink.copy(alpha = 0.3f)
)

@Composable
fun TallyBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}