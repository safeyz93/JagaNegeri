package com.jaganegeri.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Red700,
    onPrimary = White,
    primaryContainer = Red50,
    secondary = Orange700,
    onSecondary = White,
    tertiary = Green700,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = Gray50,
    error = Red700
)

@Composable
fun JagaNegeriTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
