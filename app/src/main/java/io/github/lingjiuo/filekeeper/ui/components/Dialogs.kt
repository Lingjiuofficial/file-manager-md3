package io.github.lingjiuo.filekeeper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lingjiuo.filekeeper.ui.miui.MiuiDialog
import io.github.lingjiuo.filekeeper.ui.theme.LocalMiui

data class OpProgress(
    val title: String,
    val done: Int,
    val total: Int,
    val current: String
)

@Composable
fun MiuiTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Done
) {
    val palette = LocalMiui.current
    Column(modifier) {
        Text(label, fontSize = 12.sp, color = palette.textSecondary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(palette.cardAlt)
                .padding(horizontal = 14.dp, vertical = 13.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(color = palette.textPrimary),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(palette.accent),
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun InputDialog(
    title: String,
    label: String,
    initial: String = "",
    confirmText: String = "确定",
    selectNameOnly: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val start = if (selectNameOnly) 0 else initial.length
    val end = if (selectNameOnly) initial.substringBeforeLast('.').ifEmpty { initial }.length else initial.length
    var value by remember {
        mutableStateOf(TextFieldValue(initial, TextRange(start, end.coerceAtLeast(start))))
    }
    MiuiDialog(
        title = title,
        onDismiss = onDismiss,
        confirmText = confirmText,
        onConfirm = { if (value.text.isNotBlank()) onConfirm(value.text.trim()) }
    ) {
        MiuiTextField(value = value, onValueChange = { value = it }, label = label)
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String = "确定",
    danger: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    MiuiDialog(
        title = title,
        onDismiss = onDismiss,
        confirmText = confirmText,
        onConfirm = onConfirm,
        danger = danger
    ) {
        Text(text, color = LocalMiui.current.textSecondary, fontSize = 14.sp)
    }
}

@Composable
fun ProgressDialog(progress: OpProgress, onCancel: (() -> Unit)? = null) {
    val palette = LocalMiui.current
    MiuiDialog(
        title = progress.title,
        onDismiss = {},
        confirmText = if (onCancel != null) "取消" else "好",
        onConfirm = { onCancel?.invoke() },
        showDismiss = false
    ) {
        val fraction = if (progress.total <= 0) 0f else (progress.done.toFloat() / progress.total).coerceIn(0f, 1f)
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(palette.divider)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction.coerceAtLeast(0.01f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(palette.accent)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "${progress.done} / ${progress.total}",
            color = palette.textSecondary,
            fontSize = 12.sp
        )
        Text(
            progress.current,
            color = palette.textPrimary,
            fontSize = 13.5.sp,
            maxLines = 2,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}
