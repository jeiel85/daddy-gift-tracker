package com.jeiel.daddygifttracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = NavyDark,
    onPrimary = TextLight,
    primaryContainer = SlateBlue,
    onPrimaryContainer = TextLight,
    secondary = DeepGreen,
    onSecondary = TextLight,
    secondaryContainer = SoftGreenBg,
    onSecondaryContainer = DeepGreen,
    tertiary = GoldPoint,
    onTertiary = TextLight,
    tertiaryContainer = SoftGoldBg,
    onTertiaryContainer = GoldPoint,
    background = WarmBeigeBg,
    onBackground = TextDark,
    surface = CardBeige,
    onSurface = TextDark,
    surfaceVariant = SoftBeigeSurface,
    onSurfaceVariant = TextDark,
    error = CondolenceRed,
    onError = TextLight
)

private val DarkColorScheme = darkColorScheme(
    primary = SlateBlue,
    onPrimary = TextLight,
    secondary = DeepGreen,
    onSecondary = TextLight,
    tertiary = GoldPoint,
    background = NavyDark,
    onBackground = TextLight,
    surface = NavyLight,
    onSurface = TextLight,
    surfaceVariant = SlateBlue,
    onSurfaceVariant = TextLight
)

@Composable
fun GyeongjosaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false by default to preserve our bespoke Navy/Beige/Gold color design system
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

