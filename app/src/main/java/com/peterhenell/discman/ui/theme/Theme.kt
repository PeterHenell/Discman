package com.peterhenell.discman.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = DarkGray,
    onPrimary = White,
    primaryContainer = MediumGray,
    onPrimaryContainer = VeryDarkGray,
    secondary = DarkGray,
    onSecondary = White,
    secondaryContainer = MediumGray,
    onSecondaryContainer = VeryDarkGray,
    tertiary = DarkGray,
    onTertiary = White,
    tertiaryContainer = MediumGray,
    onTertiaryContainer = VeryDarkGray,
    error = Color(0xFFB3261E),
    onError = White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = DarkGray,
    background = LightGray,
    onBackground = VeryDarkGray,
    surface = White,
    onSurface = VeryDarkGray,
    surfaceVariant = MediumGray,
    onSurfaceVariant = DarkGray,
)

@Composable
fun DiscmanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> LightColorScheme // Always use light gray theme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}