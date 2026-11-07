package io.github.lingjiuo.filekeeper.ui.screens

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.lingjiuo.filekeeper.data.FileRepo
import io.github.lingjiuo.filekeeper.model.FileEntry
import io.github.lingjiuo.filekeeper.model.Section
import io.github.lingjiuo.filekeeper.model.ViewMode
import io.github.lingjiuo.filekeeper.ui.AppState
import io.github.lingjiuo.filekeeper.util.Fmt
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File

@Composable
fun SearchPage(state: AppState) {
    var category by remember { mutableStateOf<String?>(null) }
    val categories = listOf("全部", "图片", "视频", "音频", "文档", "安装包", "压缩包")

    LaunchedEffect(state.searchQuery, category) {
        if (state.searchQuery.isBlank()) {
            state.searchResults = emptyList()
            return@LaunchedEffect
        }
        state.searching = true
        val res = mutableListOf<File>()
        FileRepo.search(File(state.path), state.searchQuery, category?.takeIf { it != "全部" }) { res += it }
        state.searchResults = res
        state.searching = false
    }

    Scaffold(topBar = { TopAppBar(title = "搜索") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TextField(
                value = state.searchQuery,
                onValueChange = { state.searchQuery = it },
                label = "文件名关键词",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
            )
            var selectedIndex by remember { mutableStateOf(0) }
            TabRow(
                tabs = categories,
                selectedTabIndex = selectedIndex,
                onTabSelected = {
                    selectedIndex = it
                    category = categories[it].takeIf { c -> c != "全部" }
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
            LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)) {
                items(state.searchResults, key = { it.absolutePath }) { f ->
                    Card(
                        onClick = {
                            val parent = f.parentFile
                            if (parent != null) {
                                state.navigate(parent)
                                state.section = Section.FILES
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(MiuixIcons.File, null, Modifier.width(22.dp), Fmt.tintFor(f.name, false))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(f.name, style = MiuixTheme.textStyles.title4, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(f.parent ?: "", style = MiuixTheme.textStyles.footnote2,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary, maxLines = 1)
                            }
                            Text(Fmt.size(f.length()), style = MiuixTheme.textStyles.footnote1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesPage(state: AppState) {
    val favorites = state.prefs.favorites
    val recent = state.prefs.recent
    Scaffold(topBar = { TopAppBar(title = "收藏") }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)) {
            item { SmallTitle("收藏") }
            items(favorites) { path ->
                Card(
                    onClick = { state.navigate(File(path)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(MiuixIcons.Favorites, null, Modifier.width(22.dp), MiuixTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(File(path).name.ifEmpty { "/" }, style = MiuixTheme.textStyles.title4)
                            Text(path, style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary, maxLines = 1)
                        }
                    }
                }
            }
            item { SmallTitle("最近") }
            items(recent.take(10)) { path ->
                Card(
                    onClick = { state.navigate(File(path)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(MiuixIcons.Folder, null, Modifier.width(22.dp), MiuixTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(File(path).name.ifEmpty { "/" }, style = MiuixTheme.textStyles.title4)
                            Text(path, style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoragePage(state: AppState) {
    var dir by remember { mutableStateOf(File(Environment.getExternalStorageDirectory()?.absolutePath ?: "/")) }
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

    Scaffold(topBar = { TopAppBar(title = "存储") }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)) {
            item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("${Fmt.size(used)} / ${Fmt.size(total)}", style = MiuixTheme.textStyles.title2)
                    Spacer(Modifier.height(8.dp))
                    BoxBar(if (total > 0) used.toFloat() / total else 0f)
                    Spacer(Modifier.height(6.dp))
                    Text("剩余 ${Fmt.size(free)}", style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
            }
            if (dir.parentFile != null && dir.absolutePath != "/") {
                item {
                    Card(
                        onClick = { dir = dir.parentFile ?: dir },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)
                    ) { Text("上一级", style = MiuixTheme.textStyles.title4) }
                }
            }
            items(items, key = { it.file.absolutePath }) { item ->
                Card(
                    onClick = { if (item.isDir) dir = item.file },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.name, Modifier.weight(1f), style = MiuixTheme.textStyles.title4,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(Fmt.size(item.size), style = MiuixTheme.textStyles.footnote1)
                    }
                    Spacer(Modifier.height(6.dp))
                    BoxBar((item.size.toFloat() / maxSize).coerceIn(0.02f, 1f))
                }
            }
        }
    }
}

@Composable
private fun BoxBar(fraction: Float) {
    androidx.compose.foundation.layout.Box(
        Modifier.fillMaxWidth().height(7.dp)
            .background(MiuixTheme.colorScheme.secondaryContainer, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
    ) {
        androidx.compose.foundation.layout.Box(
            Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(7.dp)
                .background(MiuixTheme.colorScheme.primary, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun SettingsPage(state: AppState) {
    var themeIndex by remember { mutableStateOf(state.darkMode) }
    var dynamic by remember { mutableStateOf(state.dynamicColor) }
    Scaffold(topBar = { TopAppBar(title = "设置") }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)) {
            item { SmallTitle("外观") }
            item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)) {
                    Text("主题", style = MiuixTheme.textStyles.title4)
                    Spacer(Modifier.height(8.dp))
                    TabRow(
                        tabs = listOf("跟随系统", "浅色", "深色"),
                        selectedTabIndex = themeIndex,
                        onTabSelected = { themeIndex = it; state.changeDarkMode(it) }
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("跟随壁纸取色", style = MiuixTheme.textStyles.title4)
                            Text("Android 12+ 从壁纸提取强调色", style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        }
                        Switch(checked = dynamic, onCheckedChange = {
                            dynamic = it
                            state.changeDynamicColor(it)
                        })
                    }
                }
            }
            item { SmallTitle("浏览") }
            item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("显示隐藏文件", style = MiuixTheme.textStyles.title4)
                            Text("以 . 开头的文件", style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        }
                        Switch(checked = state.showHidden, onCheckedChange = {
                            state.showHidden = it
                            state.applyPrefs()
                        })
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("默认视图", style = MiuixTheme.textStyles.title4)
                    Spacer(Modifier.height(8.dp))
                    TabRow(
                        tabs = listOf("列表", "网格"),
                        selectedTabIndex = if (state.viewMode == ViewMode.LIST) 0 else 1,
                        onTabSelected = {
                            state.viewMode = if (it == 0) ViewMode.LIST else ViewMode.GRID
                            state.applyPrefs()
                        }
                    )
                }
            }
            item { SmallTitle("关于") }
            item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)) {
                    Text("文件管家", style = MiuixTheme.textStyles.title3)
                    Text(
                        "Kotlin + Jetpack Compose + MIUIX\n支持 ZIP / 7Z / RAR / TAR、PDF、APK、文本、图片与视频。",
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }
        }
    }
}
