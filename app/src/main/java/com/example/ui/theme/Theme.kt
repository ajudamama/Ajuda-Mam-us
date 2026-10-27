package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = EmeraldDeep,
    secondary = TealAccent,
    onSecondary = Color.White,
    secondaryContainer = EmeraldSoftBg,
    onSecondaryContainer = TealDark,
    tertiary = HealthBlue,
    onTertiary = Color.White,
    tertiaryContainer = HealthBlueLight,
    onTertiaryContainer = HealthBlueDark,
    background = SlateBg,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SlateDark,
    outline = SlateBorder,
    error = HealthRed,
    onError = Color.White,
    errorContainer = HealthRedLight,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.Black,
    primaryContainer = EmeraldDeep,
    onPrimaryContainer = EmeraldLight,
    secondary = TealAccent,
    onSecondary = Color.White,
    secondaryContainer = TealDark,
    onSecondaryContainer = EmeraldLight,
    tertiary = HealthBlueLight,
    onTertiary = Color.Black,
    tertiaryContainer = HealthBlueDark,
    onTertiaryContainer = HealthBlueLight,
    background = SlateDark,
    onBackground = Color.White,
    surface = SlateNavy,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = SlateLight,
    outline = Color(0xFF475569),
    error = HealthRed,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = HealthRedLight
)

@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    disabledTextColor = Color.Black.copy(alpha = 0.5f),
    errorTextColor = Color.Black,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = EmeraldDark,
    unfocusedBorderColor = SlateBorder,
    focusedLabelColor = EmeraldDark,
    unfocusedLabelColor = SlateMedium,
    cursorColor = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // For this medical app, maintain crisp high-contrast light scheme with black text for clarity
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalContentColor provides Color.Black,
            content = content
        )
    }
}

