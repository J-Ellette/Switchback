package app.switchback.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Pine = Color(0xFF2F4A3A)
private val Ember = Color(0xFFE0663A)
private val Sandstone = Color(0xFFF3F0E6)
private val Bark = Color(0xFF3D352B)

private val Light = lightColorScheme(
    primary = Pine,
    onPrimary = Color.White,
    secondary = Ember,
    onSecondary = Color.White,
    background = Sandstone,
    onBackground = Bark,
    surface = Color(0xFFFBF9F3),
    onSurface = Bark,
)

private val Dark = darkColorScheme(
    primary = Color(0xFF9CC3A8),
    onPrimary = Color(0xFF10261A),
    secondary = Color(0xFFFFB59A),
    onSecondary = Color(0xFF4A1500),
)

@Composable
fun SwitchbackTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}
