package com.example.notasapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme


private val LightColors = lightColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color.White,
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFE0E0E0),
    onSurface = Color.Black
)


private val DarkColors = darkColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color.White,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0)
)


@Composable
fun NotasAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
@Composable
fun NotasAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
