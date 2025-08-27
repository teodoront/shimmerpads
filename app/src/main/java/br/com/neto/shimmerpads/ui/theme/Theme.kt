package br.com.neto.shimmerpads.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color




@Composable
fun ShimmerpadsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF8E44AD),
            secondary = Color(0xFFBB86FC),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White,
        ),
        typography = Typography,
        content = content
    )
}