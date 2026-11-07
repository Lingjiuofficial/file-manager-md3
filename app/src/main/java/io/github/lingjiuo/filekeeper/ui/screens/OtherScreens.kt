package io.github.lingjiuo.filekeeper.ui.screens

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lingjiuo.filekeeper.data.FileRepo
import io.github.lingjiuo.filekeeper.model.FileEntry
import io.github.lingjiuo.filekeeper.model.Section
import io.github.lingjiuo.filekeeper.model.ViewMode
import io.github.lingjiuo.filekeeper.ui.AppState
import io.github.lingjiuo.filekeeper.ui.components.LargeTitle
import io.github.lingjiuo.filekeeper.ui.miui.MiuiButton
import io.github.lingjiuo.filekeeper.ui.miui.MiuiCard
import io.github.lingjiuo.filekeeper.ui.miui.MiuiChip
import io.github.lingjiuo.filekeeper.ui.miui.MiuiDivider
import io.github.lingjiuo.filekeeper.ui.miui.MiuiSectionTitle
import io.github.lingjiuo.filekeeper.ui.miui.MiuiSwitch
import io.github.lingjiuo.filekeeper.ui.theme.LocalMiui
import io.github.lingjiuo.filekeeper.util.Fmt
import java.io.File

@Composable
fun SearchScreen(state: AppState) {
    val palette = LocalMiui.current
    var category by remember { mutableStateOf<String?>(null) }
    val categories = listOf("图片", "视频", "音频", "文档", "安装包", "压缩包")

    LaunchedEffect(state.searchQuery, category) {
        if (state.searchQuery.isBlank()) {
            state.searchResults = emptyList()
            return@LaunchedEffect
        }
        state.searching = true
        val res = mutableListOf<File>()
        FileRepo.search(File(state.path), state.searchQuery, category) { res += it }
        state.searchResults = res
        state.searching = false
    }

    Column(Modifier.fillMaxSize()) {
        LargeTitle("搜索", "在 ${File(state.path).name.ifEmpty { "/" }} 下查找")
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(palette.card)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Search,
                null,
                tint = palette.textSecondary,
                modifier = Modifier.size(19.dp)
            )
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (state.searchQuery.isEmpty()) {
                    Text("文件名关键词", color = palette.textSecondary, fontSize = 15.sp)
                }
                BasicTextField(
                    value = state.searchQuery,
                    onValueChange = { state.searchQuery = it },
                    singleLine = true,
                    textStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(color = palette.textPrimary),
                    cursorBrush = SolidColor(palette.accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        LazyRow(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { MiuiChip("全部", category == null) { category = null } }
            items(categories) { c ->
                MiuiChip(c, category == c) { category = if (category == c) null else c }
            }
        }

        LazyColumn(Modifier.weight(1f)) {
            items(state.searchResults, key = { it.absolutePath }) { f ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            val parent = f.parentFile
                            if (parent != null) {
                                state.navigate(parent)
                                state.section = Section.FILES
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Fmt.iconFor(f.name, false),
                        null,
                        tint = Fmt.tintFor(f.name, false),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(f.name, fontSize = 15.sp, color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            f.parent,
                            fontSize = 11.5.sp,
                            color = palette.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(Fmt.size(f.length()), fontSize = 12.sp, color = palette.textSecondary)
                }
            }
            item { Spacer(Modifier.height(110.dp)) }
        }
    }
}

@Composable
fun FavoritesScreen(state: AppState) {
    val palette = LocalMiui.current
    val favorites = state.prefs.favorites
    val recent = state.prefs.recent
    Column(Modifier.fillMaxSize()) {
        LargeTitle("收藏", "常用目录与最近访问")
        LazyColumn(Modifier.fillMaxSize()) {
            item { MiuiSectionTitle("收藏") }
            item {
                MiuiCard(Modifier.padding(horizontal = 16.dp), padding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)) {
                    if (favorites.isEmpty()) {
                        Text(
                            "在文件页长按文件夹即可收藏",
                            color = palette.textSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    favorites.forEachIndexed { index, path ->
                        PathRow(File(path), onOpen = { state.navigate(File(path)) }, onRemove = {
                            state.prefs.favorites = favorites - path
                        })
                        if (index != favorites.lastIndex) MiuiDivider(Modifier.padding(start = 52.dp))
                    }
                }
            }
            item { MiuiSectionTitle("最近") }
            item {
                MiuiCard(Modifier.padding(horizontal = 16.dp), padding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)) {
                    recent.take(10).forEachIndexed { index, path ->
                        PathRow(File(path), onOpen = { state.navigate(File(path)) }, onRemove = null)
                        if (index != recent.take(10).lastIndex) MiuiDivider(Modifier.padding(start = 52.dp))
                    }
                }
            }
            item { Spacer(Modifier.height(110.dp)) }
        }
    }
}

@Composable
private fun PathRow(file: File, onOpen: () -> Unit, onRemove: (() -> Unit)?) {
    val palette = LocalMiui.current
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(palette.accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Folder,
                null,
                tint = palette.accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(file.name.ifEmpty { "/" }, fontSize = 15.sp, color = palette.textPrimary)
            Text(
                file.absolutePath,
                fontSize = 11.5.sp,
                color = palette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onRemove != null) {
            Text("移除", fontSize = 13.sp, color = palette.danger, modifier = Modifier.clickable(onClick = onRemove))
        }
    }
}

@Composable
fun StorageScreen(state: AppState) {
    val palette = LocalMiui.current
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
        MiuiCard(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(Fmt.size(used), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                Spacer(Modifier.width(6.dp))
                Text(
                    "/ ${Fmt.size(total)}",
                    fontSize = 13.sp,
                    color = palette.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(palette.divider)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(if (total > 0) (used.toFloat() / total).coerceIn(0f, 1f) else 0f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(palette.accent)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("剩余 ${Fmt.size(free)}", fontSize = 12.sp, color = palette.textSecondary)
        }

        if (dir.absolutePath != PrefsRoot) {
            MiuiButton("上一级", onClick = { dir = dir.parentFile ?: dir }, filled = false,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth())
        }

        LazyColumn(Modifier.fillMaxSize()) {
            items(items, key = { it.file.absolutePath }) { item ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable { if (item.isDir) dir = item.file }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (item.isDir) Icons.Rounded.Folder else Fmt.iconFor(item.name, false),
                            null,
                            tint = Fmt.tintFor(item.name, item.isDir),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            item.name,
                            Modifier.weight(1f),
                            fontSize = 15.sp,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(Fmt.size(item.size), fontSize = 12.5.sp, color = palette.textSecondary)
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(palette.divider)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth((item.size.toFloat() / maxSize).coerceIn(0.02f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Fmt.tintFor(item.name, item.isDir))
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
    val palette = LocalMiui.current
    
    Column(Modifier.fillMaxSize()) {
        LargeTitle("设置", "文件管家 1.0")
        LazyColumn(Modifier.fillMaxSize()) {
            item { MiuiSectionTitle("外观") }
            item {
                MiuiCard(Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                        Text("主题", Modifier.weight(1f), fontSize = 15.sp, color = palette.textPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MiuiChip("系统", state.darkMode == 0) { state.changeDarkMode(0) }
                            MiuiChip("浅色", state.darkMode == 1) { state.changeDarkMode(1) }
                            MiuiChip("深色", state.darkMode == 2) { state.changeDarkMode(2) }
                        }
                    }
                    MiuiDivider()
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("跟随壁纸取色", fontSize = 15.sp, color = palette.textPrimary)
                            Text("Android 12+ 从壁纸提取强调色", fontSize = 11.5.sp, color = palette.textSecondary)
                        }
                        MiuiSwitch(
                            checked = state.dynamicColor,
                            onChange = { state.changeDynamicColor(it) }
                        )
                    }
                }
            }
            item { MiuiSectionTitle("浏览") }
            item {
                MiuiCard(Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("显示隐藏文件", fontSize = 15.sp, color = palette.textPrimary)
                            Text("以 . 开头的文件和目录", fontSize = 11.5.sp, color = palette.textSecondary)
                        }
                        MiuiSwitch(
                            checked = state.showHidden,
                            onChange = {
                                state.showHidden = it; state.applyPrefs(); onChange()
                            }
                        )
                    }
                    MiuiDivider()
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                        Text("默认视图", Modifier.weight(1f), fontSize = 15.sp, color = palette.textPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MiuiChip("列表", state.viewMode == ViewMode.LIST) {
                                state.viewMode = ViewMode.LIST; state.applyPrefs(); onChange()
                            }
                            MiuiChip("网格", state.viewMode == ViewMode.GRID) {
                                state.viewMode = ViewMode.GRID; state.applyPrefs(); onChange()
                            }
                        }
                    }
                }
            }
            item { MiuiSectionTitle("关于") }
            item {
                MiuiCard(Modifier.padding(horizontal = 16.dp)) {
                    Text("文件管家", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                    Text(
                        "Kotlin + Jetpack Compose · GPL-3.0\n支持 ZIP / 7Z / RAR / TAR、PDF、APK、文本、图片与视频。",
                        fontSize = 12.sp,
                        color = palette.textSecondary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
            item { Spacer(Modifier.height(110.dp)) }
        }
    }
}
