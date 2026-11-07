package io.github.lingjiuo.filekeeper.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

val MiuiOrange = Color(0xFFFF6900)
val MiuiBlue = Color(0xFF3482FF)

/** 从 Miuix 主题里取出的语义色，供自定义组件（文件行、底栏）使用。 */
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

@Composable
fun FileKeeperTheme(dark: Boolean, dynamic: Boolean = true, content: @Composable () -> Unit) {
    val controller = remember {
        ThemeController(
            colorSchemeMode = if (dynamic) ColorSchemeMode.MonetSystem else ColorSchemeMode.System,
            isDark = dark
        )
    }
    MiuixTheme(controller = controller) {
        val colors = MiuixTheme.colorScheme
        val palette = MiuiPalette(
            accent = colors.primary,
            background = colors.background,
            card = colors.surface,
            cardAlt = colors.secondaryContainer,
            textPrimary = colors.onBackground,
            textSecondary = colors.onSurfaceVariantSummary,
            divider = colors.onBackgroundVariant.copy(alpha = 0.18f),
            danger = colors.error,
            isDark = dark
        )
        CompositionLocalProvider(LocalMiui provides palette) {
            content()
        }
    }
}
