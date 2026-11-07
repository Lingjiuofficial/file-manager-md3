package io.github.lingjiuo.filekeeper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.lingjiuo.filekeeper.model.FileEntry
import io.github.lingjiuo.filekeeper.util.Fmt
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun FileRow(
    entry: FileEntry,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colors = MiuixTheme.colorScheme
    Card(
        onClick = onClick,
        onLongPress = onLongClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp),
        colors = CardDefaults.defaultColors(
            color = if (selected) colors.primaryContainer else colors.surfaceContainer,
            contentColor = if (selected) colors.onPrimaryContainer else colors.onSurfaceContainer
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Fmt.tintFor(entry.name, entry.isDir).copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (entry.isDir) MiuixIcons.Folder else MiuixIcons.File,
                    contentDescription = entry.name,
                    tint = Fmt.tintFor(entry.name, entry.isDir),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MiuixTheme.textStyles.title4,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (entry.isDir) Fmt.date(entry.modified)
                    else "${Fmt.size(entry.size)} · ${Fmt.date(entry.modified)}",
                    style = MiuixTheme.textStyles.footnote2,
                    color = colors.onSurfaceVariantSummary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun MiuixInputDialog(
    title: String,
    label: String,
    initial: String,
    confirmText: String = "确定",
    selectNameOnly: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initial) }
    SuperDialog(
        show = true,
        title = title,
        onDismissRequest = onDismiss
    ) {
        Column {
            top.yukonga.miuix.kmp.basic.TextField(
                value = value,
                onValueChange = { value = it },
                label = label,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(
                    text = "取消",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { if (value.isNotBlank()) onConfirm(value.trim()) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary()
                ) { Text(confirmText) }
            }
        }
    }
}

@Composable
fun MiuixConfirmDialog(
    title: String,
    text: String,
    confirmText: String = "确定",
    danger: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    SuperDialog(
        show = true,
        title = title,
        onDismissRequest = onDismiss
    ) {
        Column {
            Text(text, style = MiuixTheme.textStyles.body2)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(text = "取消", onClick = onDismiss, modifier = Modifier.weight(1f))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = if (danger) ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.error,
                        contentColor = MiuixTheme.colorScheme.onError
                    ) else ButtonDefaults.buttonColorsPrimary()
                ) { Text(confirmText) }
            }
        }
    }
}

data class OpProgress(
    val title: String,
    val done: Int,
    val total: Int,
    val current: String
)

@Composable
fun MiuixProgressDialog(progress: OpProgress, onCancel: (() -> Unit)? = null) {
    val colors = MiuixTheme.colorScheme
    SuperDialog(
        show = true,
        title = progress.title,
        onDismissRequest = { onCancel?.invoke() }
    ) {
        Column {
            val fraction = if (progress.total <= 0) 0f else (progress.done.toFloat() / progress.total).coerceIn(0f, 1f)
            Box(
                Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(colors.secondaryContainer)
            ) {
                Box(
                    Modifier.fillMaxWidth(fraction.coerceAtLeast(0.02f))
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.primary)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("${progress.done} / ${progress.total}", style = MiuixTheme.textStyles.footnote2)
            Text(progress.current, style = MiuixTheme.textStyles.body2, maxLines = 2)
        }
    }
}
