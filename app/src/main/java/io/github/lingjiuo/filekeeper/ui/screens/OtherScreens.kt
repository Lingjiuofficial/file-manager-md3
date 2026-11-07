package io.github.lingjiuo.filekeeper.ui.screens

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.lingjiuo.filekeeper.data.FileRepo
import io.github.lingjiuo.filekeeper.model.FileEntry
import io.github.lingjiuo.filekeeper.model.Section
import io.github.lingjiuo.filekeeper.ui.AppState
import io.github.lingjiuo.filekeeper.ui.components.LargeTitle
import io.github.lingjiuo.filekeeper.util.Fmt
import java.io.File

@Composable
fun SearchScreen(state: AppState) {
    var category by remember { mutableStateOf<String?>(null) }
    val categories = listOf("图片", "视频", "音频", "文档", "安装包", "压缩包")
    LaunchedEffect(state.searchQuery, category) {
        if (state.searchQuery.isBlank()) {
            state.searchResults = emptyList()
            return@LaunchedEffect
        }
        state.searching = true
        val res = mutableListOf<File>()
        FileRepo.search(File(state.path), state.searchQuery, category) { f -> res += f }
        state.searchResults = res
        state.searching = false
    }

    Column(Modifier.fillMaxSize()) {
        LargeTitle("搜索", "在 ${File(state.path).name.ifEmpty { "/" }} 下查找")
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { state.searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("文件名关键词") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions.Default
        )
        LazyRow(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(selected = category == null, onClick = { category = null }, label = { Text("全部") })
            }
            items(categories) { c ->
                FilterChip(selected = category == c, onClick = { category = if (category == c) null else c }, label = { Text(c) })
            }
        }
        if (state.searching) LinearProgressIndicator(Modifier.fillMaxWidth())
        LazyColumn(Modifier.weight(1f)) {
            items(state.searchResults, key = { it.absolutePath }) { f ->
                Row(
                    Modifier.fillMaxWidth().clickable {
                        val parent = f.parentFile
                        if (parent != null) {
                            state.navigate(parent)
                            state.section = Section.FILES
                        }
                    }.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Folder, null, tint = Fmt.tintFor(f.name, false), modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(f.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(f.parent, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    Text(Fmt.size(f.length()), style = MaterialTheme.typography.labelMedium)
                }
            }
            item { Spacer(Modifier.height(110.dp)) }
        }
    }
}

@Composable
fun FavoritesScreen(state: AppState) {
    val favorites = state.prefs.favorites
    val recent = state.prefs.recent
    Column(Modifier.fillMaxSize()) {
        LargeTitle("收藏", "常用目录与最近访问")
        LazyColumn(Modifier.fillMaxSize()) {
            item { SectionTitle("收藏") }
            if (favorites.isEmpty()) {
                item { Hint("在文件页长按文件夹可加入收藏") }
            }
            items(favorites) { path ->
                PathRow(File(path), onOpen = { state.navigate(File(path)) }, onRemove = {
                    state.prefs.favorites = favorites - path
                })
            }
            item { SectionTitle("最近") }
            items(recent) { path ->
                PathRow(File(path), onOpen = { state.navigate(File(path)) }, onRemove = null)
            }
            item { Spacer(Modifier.height(110.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 6.dp)
    )
}

@Composable
private fun Hint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
    )
}

@Composable
private fun PathRow(file: File, onOpen: () -> Unit, onRemove: (() -> Unit)?) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Folder, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(file.name.ifEmpty { "/" }, style = MaterialTheme.typography.bodyLarge)
            Text(file.absolutePath, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (onRemove != null) {
            IconButton(onClick = onRemove) { Icon(Icons.Rounded.Delete, "移除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
fun StorageScreen(state: AppState) {
    var dir by remember { mutableStateOf(File(PrefsRoot)) }
    var items by remember { mutableStateOf<List<FileEntry>>(emptyList()) }
    var scanning by remember { mutableStateOf(false) }

    LaunchedEffect(dir) {
        scanning = true
        items = FileRepo.scanChildren(dir)
        scanning = false
    }

    val total = FileRepo.totalSpace(dir)
    val free = FileRepo.freeSpace(dir)
    val used = (total - free).coerceAtLeast(0)
    val maxSize = items.maxOfOrNull { it.size } ?: 1L

    Column(Modifier.fillMaxSize()) {
        LargeTitle("存储", dir.absolutePath)
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("${Fmt.size(used)} / ${Fmt.size(total)}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (total > 0) used.toFloat() / total else 0f },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                Text("剩余 ${Fmt.size(free)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (dir.absolutePath != PrefsRoot) {
            TextButton(onClick = { dir = dir.parentFile ?: dir }, modifier = Modifier.padding(start = 10.dp)) {
                Icon(Icons.Rounded.ArrowBack, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("上一级")
            }
        }
        if (scanning) LinearProgressIndicator(Modifier.fillMaxWidth())
        LazyColumn(Modifier.fillMaxSize()) {
            items(items, key = { it.file.absolutePath }) { item ->
                Column(
                    Modifier.fillMaxWidth().clickable {
                        if (item.isDir) dir = item.file else state.navigate(item.file.parentFile ?: dir)
                    }.padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (item.isDir) Icons.Rounded.Folder else Fmt.iconFor(item.name, false),
                            null,
                            tint = Fmt.tintFor(item.name, item.isDir),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(item.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(Fmt.size(item.size), style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier.fillMaxWidth().height(6.dp).background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(3.dp)
                        )
                    ) {
                        Box(
                            Modifier.fillMaxWidth((item.size.toFloat() / maxSize).coerceIn(0.02f, 1f))
                                .height(6.dp)
                                .background(Fmt.tintFor(item.name, item.isDir), RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(110.dp)) }
        }
    }
}

private val PrefsRoot = Environment.getExternalStorageDirectory()?.absolutePath ?: "/"

@Composable
fun SettingsScreen(state: AppState, onChange: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        LargeTitle("设置", "文件管家 1.0")
        LazyColumn(Modifier.fillMaxSize()) {
            item { SettingHeader("外观") }
            item {
                SettingRow("主题") {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("跟随系统" to 0, "浅色" to 1, "深色" to 2).forEach { (label, v) ->
                            FilterChip(
                                selected = state.prefs.darkMode == v,
                                onClick = { state.prefs.darkMode = v; onChange() },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
            item { SettingHeader("浏览") }
            item {
                SettingRow("显示隐藏文件") {
                    androidx.compose.material3.Switch(
                        checked = state.showHidden,
                        onCheckedChange = { state.showHidden = it; state.applyPrefs(); onChange() }
                    )
                }
            }
            item {
                SettingRow("默认视图") {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = state.viewMode == io.github.lingjiuo.filekeeper.model.ViewMode.LIST,
                            onClick = { state.viewMode = io.github.lingjiuo.filekeeper.model.ViewMode.LIST; state.applyPrefs(); onChange() },
                            label = { Text("列表") }
                        )
                        FilterChip(
                            selected = state.viewMode == io.github.lingjiuo.filekeeper.model.ViewMode.GRID,
                            onClick = { state.viewMode = io.github.lingjiuo.filekeeper.model.ViewMode.GRID; state.applyPrefs(); onChange() },
                            label = { Text("网格") }
                        )
                    }
                }
            }
            item { SettingHeader("关于") }
            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text("文件管家 · Kotlin + Jetpack Compose", style = MaterialTheme.typography.bodyMedium)
                    Text("支持 ZIP / 7Z / RAR / TAR 浏览与解压，APK / 文本 / 图片 / 视频查看。", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item { Spacer(Modifier.height(110.dp)) }
        }
    }
}

@Composable
private fun SettingHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 22.dp, top = 18.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingRow(title: String, content: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        content()
    }
}
