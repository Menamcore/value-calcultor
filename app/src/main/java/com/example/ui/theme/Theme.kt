package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ProfessionalPrimaryContainer,
    onPrimary = ProfessionalOnPrimaryContainer,
    primaryContainer = ProfessionalPrimary,
    onPrimaryContainer = ProfessionalOnPrimary,
    secondary = ProfessionalSecondary,
    onSecondary = ProfessionalOnSecondary,
    background = ProfessionalOnBackground, // Dark background
    onBackground = ProfessionalBackground,
    surface = ProfessionalOnBackground,
    onSurface = ProfessionalBackground,
    surfaceVariant = Color(0xFF2F3033),
    onSurfaceVariant = Color(0xFFC4C7CF),
    outline = Color(0xFF44474E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = ProfessionalOnErrorContainer,
    onErrorContainer = ProfessionalErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = ProfessionalPrimary,
    onPrimary = ProfessionalOnPrimary,
    primaryContainer = ProfessionalPrimaryContainer,
    onPrimaryContainer = ProfessionalOnPrimaryContainer,
    secondary = ProfessionalSecondary,
    onSecondary = ProfessionalOnSecondary,
    background = ProfessionalBackground,
    onBackground = ProfessionalOnBackground,
    surface = ProfessionalSurface,
    onSurface = ProfessionalOnSurface,
    surfaceVariant = ProfessionalSurfaceVariant,
    onSurfaceVariant = ProfessionalOnSurfaceVariant,
    outline = ProfessionalOutline,
    error = ProfessionalError,
    onError = ProfessionalOnError,
    errorContainer = ProfessionalErrorContainer,
    onErrorContainer = ProfessionalOnErrorContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce the Professional Polish theme strictly
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
