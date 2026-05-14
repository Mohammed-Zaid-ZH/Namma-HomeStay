package com.nammahomestay.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = White,
    secondary = EarthGreen,
    onSecondary = White,
    tertiary = EarthClay,
    background = EarthCream,
    onBackground = EarthBrown,
    surface = White,
    onSurface = EarthBrown
)

@Composable
fun NammaHomeStayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We stick to Light Theme for that "Warm/Rural" feel unless user strictly wants dark
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // We'll create this next
        content = content
    )
}
