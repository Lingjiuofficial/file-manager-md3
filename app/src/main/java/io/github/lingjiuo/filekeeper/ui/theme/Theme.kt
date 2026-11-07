package io.github.lingjiuo.filekeeper.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val MiuiOrange = Color(0xFFFF6900)
val MiuiOrangeSoft = Color(0xFFFF8A3D)
val MiuiBlue = Color(0xFF3482FF)

private val LightColors = lightColorScheme(
    primary = MiuiOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE7D6),
    onPrimaryContainer = Color(0xFF6B2A00),
    secondary = MiuiBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5EEFF),
    onSecondaryContainer = Color(0xFF00296B),
    background = Color(0xFFF6F6F8),
    onBackground = Color(0xFF141416),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF141416),
    surfaceVariant = Color(0xFFF0F0F3),
    onSurfaceVariant = Color(0xFF6B6B70),
    outline = Color(0xFFE2E2E7),
    outlineVariant = Color(0xFFEDEDF0),
    error = Color(0xFFE5484D)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF7A1A),
    onPrimary = Color(0xFF2B1400),
    primaryContainer = Color(0xFF4A2100),
    onPrimaryContainer = Color(0xFFFFD8BE),
    secondary = Color(0xFF6EA8FF),
    onSecondary = Color(0xFF00224F),
    secondaryContainer = Color(0xFF12325E),
    onSecondaryContainer = Color(0xFFD6E3FF),
    background = Color(0xFF0C0C0E),
    onBackground = Color(0xFFF2F2F5),
    surface = Color(0xFF17171A),
    onSurface = Color(0xFFF2F2F5),
    surfaceVariant = Color(0xFF232327),
    onSurfaceVariant = Color(0xFF9B9BA2),
    outline = Color(0xFF35353A),
    outlineVariant = Color(0xFF26262A),
    error = Color(0xFFFF6369)
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 13.5.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun FileKeeperTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
