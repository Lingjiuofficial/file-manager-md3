package io.github.lingjiuo.filekeeper.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val MiuiOrange = Color(0xFFFF6900)
val MiuiBlue = Color(0xFF3482FF)

/** HyperOS / MIUI 的语义色，和 Material 的 colorScheme 并行提供。 */
data class MiuiPalette(
    val accent: Color,
    val background: Color,
    val card: Color,
    val cardAlt: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val danger: Color,
    val isDark: Boolean
)

val LocalMiui = staticCompositionLocalOf {
    MiuiPalette(
        accent = MiuiOrange,
        background = Color(0xFFF2F3F5),
        card = Color.White,
        cardAlt = Color(0xFFF7F7F9),
        textPrimary = Color(0xFF0F0F10),
        textSecondary = Color(0xFF7A7A80),
        divider = Color(0xFFEBEBEE),
        danger = Color(0xFFE5484D),
        isDark = false
    )
}

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.5.sp),
    bodyMedium = TextStyle(fontSize = 13.5.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun FileKeeperTheme(dark: Boolean, dynamic: Boolean = true, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val accent = when {
        dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context).primary else dynamicLightColorScheme(context).primary
        else -> if (dark) Color(0xFFFF7A1A) else MiuiOrange
    }

    val palette = if (dark) {
        MiuiPalette(
            accent = accent,
            background = Color(0xFF000000),
            card = Color(0xFF1C1C1E),
            cardAlt = Color(0xFF2A2A2C),
            textPrimary = Color(0xFFF5F5F7),
            textSecondary = Color(0xFF9B9BA1),
            divider = Color(0xFF2C2C2E),
            danger = Color(0xFFFF6369),
            isDark = true
        )
    } else {
        MiuiPalette(
            accent = accent,
            background = Color(0xFFF2F3F5),
            card = Color(0xFFFFFFFF),
            cardAlt = Color(0xFFF7F7F9),
            textPrimary = Color(0xFF0F0F10),
            textSecondary = Color(0xFF7A7A80),
            divider = Color(0xFFEBEBEE),
            danger = Color(0xFFE5484D),
            isDark = false
        )
    }

    val scheme = if (dark) {
        darkColorScheme(
            primary = accent,
            onPrimary = Color.White,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.card,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.cardAlt,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.divider,
            outlineVariant = palette.divider,
            error = palette.danger
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = Color.White,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.card,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.cardAlt,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.divider,
            outlineVariant = palette.divider,
            error = palette.danger
        )
    }

    CompositionLocalProvider(LocalMiui provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
