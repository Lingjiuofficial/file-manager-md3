package io.github.lingjiuo.filekeeper.ui.miui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.lingjiuo.filekeeper.ui.theme.LocalMiui
import androidx.compose.foundation.layout.PaddingValues

/** MIUI / HyperOS 开关：胶囊轨道 + 圆点。 */
@Composable
fun MiuiSwitch(checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    val palette = LocalMiui.current
    val track by animateColorAsState(
        when {
            !enabled -> palette.divider
            checked -> palette.accent
            else -> palette.cardAlt
        }, label = "track"
    )
    val knobX by animateDpAsState(if (checked) 21.dp else 0.dp, label = "knob")
    Box(
        modifier = Modifier
            .size(52.dp, 32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(track)
            .clickable(enabled = enabled) { onChange(!checked) }
            .padding(3.dp)
    ) {
        Box(
            Modifier
                .offset(x = knobX)
                .size(26.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/** 圆形图标按钮，MIUI 顶栏常见样式。 */
@Composable
fun MiuiIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color = LocalMiui.current.textPrimary,
    background: Color = LocalMiui.current.cardAlt
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Icon(icon, description, tint = tint, modifier = Modifier.size(20.dp))
    }
}

/** 白色（暗色为深灰）圆角卡片。 */
@Composable
fun MiuiCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalMiui.current
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(palette.card)
            .padding(padding),
        content = content
    )
}

@Composable
fun MiuiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    enabled: Boolean = true,
    danger: Boolean = false
) {
    val palette = LocalMiui.current
    val bg = when {
        !enabled -> palette.cardAlt
        danger -> palette.danger
        filled -> palette.accent
        else -> palette.cardAlt
    }
    val fg = when {
        !enabled -> palette.textSecondary
        filled || danger -> Color.White
        else -> palette.textPrimary
    }
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(23.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = fg, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun MiuiChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val palette = LocalMiui.current
    val bg = if (selected) palette.accent.copy(alpha = 0.14f) else palette.cardAlt
    val fg = if (selected) palette.accent else palette.textSecondary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = fg, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
    }
}

/** MIUI 风格对话框：大圆角、按钮横排、中间分割线。 */
@Composable
fun MiuiDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String = "取消",
    danger: Boolean = false,
    showDismiss: Boolean = true,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val palette = LocalMiui.current
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(palette.card)
        ) {
            Column(Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
                Text(
                    title,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary
                )
                Spacer(Modifier.height(12.dp))
                content()
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(palette.divider))
            Row(Modifier.fillMaxWidth().height(52.dp)) {
                if (showDismiss) {
                    Box(
                        Modifier.weight(1f).fillMaxWidth().clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(dismissText, color = palette.textSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    }
                    Box(Modifier.width(1.dp).fillMaxWidth(1f).background(palette.divider))
                }
                Box(
                    Modifier.weight(1f).fillMaxWidth().clickable(onClick = onConfirm),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        confirmText,
                        color = if (danger) palette.danger else palette.accent,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

/** 分组标题，HyperOS 设置页那种小字。 */
@Composable
fun MiuiSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        color = LocalMiui.current.textSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier.padding(start = 24.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
fun MiuiDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(LocalMiui.current.divider))
}

/** 悬浮底栏专用：半透明磨砂感的胶囊。 */
@Composable
fun DockSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val palette = LocalMiui.current
    Box(
        modifier
            .clip(RoundedCornerShape(34.dp))
            .background(palette.card.copy(alpha = 0.93f))
            .border(1.dp, palette.divider.copy(alpha = 0.6f), RoundedCornerShape(34.dp))
            .shadow(18.dp, RoundedCornerShape(34.dp), clip = false),
        contentAlignment = Alignment.Center
    ) { content() }
}
