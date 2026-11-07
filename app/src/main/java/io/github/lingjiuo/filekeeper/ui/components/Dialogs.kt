package io.github.lingjiuo.filekeeper.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        },
        confirmButton = {
            TextButton(onClick = { if (value.text.isNotBlank()) onConfirm(value.text.trim()) }) {
                Text(confirmText)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

data class OpProgress(
    val title: String,
    val done: Int,
    val total: Int,
    val current: String
)

@Composable
fun ProgressDialog(progress: OpProgress, onCancel: (() -> Unit)? = null) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(progress.title, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = {
                        if (progress.total <= 0) 0f else (progress.done.toFloat() / progress.total).coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "${progress.done} / ${progress.total}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    progress.current,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            if (onCancel != null) TextButton(onClick = onCancel) { Text("取消") }
        }
    )
}
