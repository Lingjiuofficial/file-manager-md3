package io.github.lingjiuo.filekeeper.ui.screens

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NoteAdd
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.lingjiuo.filekeeper.data.ArchiveRepo
import io.github.lingjiuo.filekeeper.data.FileRepo
import io.github.lingjiuo.filekeeper.model.Clip
import io.github.lingjiuo.filekeeper.model.FileEntry
import io.github.lingjiuo.filekeeper.model.SortMode
import io.github.lingjiuo.filekeeper.model.ViewMode
import io.github.lingjiuo.filekeeper.ui.AppState
import io.github.lingjiuo.filekeeper.ui.components.ConfirmDialog
import io.github.lingjiuo.filekeeper.ui.components.FileGridCell
import io.github.lingjiuo.filekeeper.ui.components.FileRow
import io.github.lingjiuo.filekeeper.ui.components.InputDialog
import io.github.lingjiuo.filekeeper.ui.components.LargeTitle
import io.github.lingjiuo.filekeeper.ui.components.OpProgress
import io.github.lingjiuo.filekeeper.ui.components.ProgressDialog
import io.github.lingjiuo.filekeeper.ui.components.RoundIconButton
import io.github.lingjiuo.filekeeper.util.Fmt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FilesScreen(state: AppState, onOpen: (File) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var reload by remember { mutableIntStateOf(0) }
    var progress by remember { mutableStateOf<OpProgress?>(null) }
    var menuTarget by remember { mutableStateOf<File?>(null) }
    var renameTarget by remember { mutableStateOf<File?>(null) }
    var propsTarget by remember { mutableStateOf<File?>(null) }
    var deleteTargets by remember { mutableStateOf<List<File>>(emptyList()) }
    var showNewDir by remember { mutableStateOf(false) }
    var showNewFile by remember { mutableStateOf(false) }
    var showPath by remember { mutableStateOf(false) }
    var sortOpen by remember { mutableStateOf(false) }
    var moreOpen by remember { mutableStateOf(false) }
    var batchRename by remember { mutableStateOf(false) }

    LaunchedEffect(state.path, state.sort, state.ascending, state.showHidden, reload) {
        state.loading = true
        state.entries = FileRepo.list(File(state.path), state.showHidden, state.sort, state.ascending)
        state.loading = false
        state.applyPrefs()
    }

    fun refresh() { reload++ }

    fun copySelection(cut: Boolean) {
        val files = state.selectedFiles()
        if (files.isEmpty()) return
        state.clip = Clip(files.map { it.absolutePath }, cut)
        state.selection = emptySet()
        state.message = if (cut) "已剪切 ${files.size} 项" else "已复制 ${files.size} 项"
    }

    fun paste() {
        val clip = state.clip ?: return
        val sources = clip.paths.map { File(it) }.filter { it.exists() }
        if (sources.isEmpty()) {
            state.clip = null; return
        }
        val dest = state.activeDir
        scope.launch {
            val total = sources.size
            progress = OpProgress(if (clip.cut) "正在移动" else "正在复制", 0, total, "")
            try {
                if (clip.cut) {
                    FileRepo.move(sources, dest) { i, t, name, _ ->
                        progress = OpProgress("正在移动", i - 1, t, name)
                    }
                } else {
                    FileRepo.copy(sources, dest) { i, t, name, _ ->
                        progress = OpProgress("正在复制", i - 1, t, name)
                    }
                }
                state.clip = null
                state.message = "完成，共 $total 项"
            } catch (e: Exception) {
                state.message = "操作失败：${e.message}"
            } finally {
                progress = null
                refresh()
            }
        }
    }

    fun delete(files: List<File>) {
        if (files.isEmpty()) return
        scope.launch {
            progress = OpProgress("正在删除", 0, files.size, "")
            try {
                val ok = FileRepo.delete(files) { i, t, name ->
                    progress = OpProgress("正在删除", i - 1, t, name)
                }
                state.message = "已删除 $ok 项"
            } catch (e: Exception) {
                state.message = "删除失败：${e.message}"
            } finally {
                progress = null
                state.selection = emptySet()
                refresh()
            }
        }
    }

    fun rename(file: File, name: String) {
        try {
            FileRepo.rename(file, name)
            state.message = "已重命名"
            refresh()
        } catch (e: Exception) {
            state.message = e.message
        }
    }

    fun compress(files: List<File>) {
        if (files.isEmpty()) return
        val name = if (files.size == 1) files[0].name.substringBeforeLast('.') + ".zip" else "压缩包.zip"
        val dest = File(state.activeDir, name)
        scope.launch {
            progress = OpProgress("正在压缩", 0, files.size, "")
            try {
                ArchiveRepo.createZip(files, dest) { i, t, n ->
                    progress = OpProgress("正在压缩", i - 1, t, n)
                }
                state.message = "已创建 ${dest.name}"
            } catch (e: Exception) {
                state.message = "压缩失败：${e.message}"
            } finally {
                progress = null
                refresh()
            }
        }
    }

    fun extract(file: File) {
        val dirName = file.name.removeSuffix(".${Fmt.ext(file.name)}")
        val dest = File(state.activeDir, dirName)
        scope.launch {
            progress = OpProgress("正在解压", 0, 0, file.name)
            try {
                dest.mkdirs()
                ArchiveRepo.extract(file, dest) { i, t, n ->
                    progress = OpProgress("正在解压", i, t, n)
                }
                state.message = "已解压到 ${dest.name}"
            } catch (e: Exception) {
                state.message = "解压失败：${e.message}"
            } finally {
                progress = null
                refresh()
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        LargeTitle(
            title = "文件",
            subtitle = state.path,
            trailing = {
                Row {
                    RoundIconButton(Icons.Rounded.Sort, "排序", onClick = { sortOpen = true })
                    Spacer(Modifier.width(6.dp))
                    RoundIconButton(Icons.Rounded.MoreVert, "更多", onClick = { moreOpen = true })
                    DropdownMenu(expanded = moreOpen, onDismissRequest = { moreOpen = false }) {
                        DropdownMenuItem(text = { Text("新建文件夹") }, leadingIcon = { Icon(Icons.Rounded.CreateNewFolder, null) },
                            onClick = { moreOpen = false; showNewDir = true })
                        DropdownMenuItem(text = { Text("新建文件") }, leadingIcon = { Icon(Icons.Rounded.NoteAdd, null) },
                            onClick = { moreOpen = false; showNewFile = true })
                        DropdownMenuItem(text = { Text("跳转路径") }, leadingIcon = { Icon(Icons.Rounded.DriveFileMove, null) },
                            onClick = { moreOpen = false; showPath = true })
                        DropdownMenuItem(text = { Text(if (state.showHidden) "隐藏隐藏文件" else "显示隐藏文件") },
                            leadingIcon = { Icon(Icons.Rounded.Description, null) },
                            onClick = { moreOpen = false; state.showHidden = !state.showHidden; state.applyPrefs() })
                        DropdownMenuItem(text = { Text(if (state.viewMode == ViewMode.LIST) "网格视图" else "列表视图") },
                            leadingIcon = { Icon(if (state.viewMode == ViewMode.LIST) Icons.Rounded.GridView else Icons.Rounded.ViewList, null) },
                            onClick = { moreOpen = false; state.viewMode = if (state.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST; state.applyPrefs() })
                        DropdownMenuItem(text = { Text("新建标签页") }, leadingIcon = { Icon(Icons.Rounded.Add, null) },
                            onClick = { moreOpen = false; state.addTab() })
                        DropdownMenuItem(text = { Text("刷新") }, leadingIcon = { Icon(Icons.Rounded.Refresh, null) },
                            onClick = { moreOpen = false; refresh() })
                    }
                    DropdownMenu(expanded = sortOpen, onDismissRequest = { sortOpen = false }) {
                        SortMode.entries.forEach { mode ->
                            val label = when (mode) {
                                SortMode.NAME -> "按名称"
                                SortMode.SIZE -> "按大小"
                                SortMode.DATE -> "按时间"
                                SortMode.TYPE -> "按类型"
                            }
                            DropdownMenuItem(
                                text = { Text(label + if (state.sort == mode) " ✓" else "") },
                                onClick = {
                                    state.sort = mode
                                    state.ascending = true
                                    state.applyPrefs()
                                    sortOpen = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(if (state.ascending) "改为降序" else "改为升序") },
                            onClick = { state.ascending = !state.ascending; state.applyPrefs(); sortOpen = false }
                        )
                    }
                }
            }
        )

        if (state.tabs.size > 1) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.tabs.size) { i ->
                    val selected = i == state.tabIndex
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { state.switchTab(i) }
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                File(state.tabs[i]).name.ifEmpty { "/" },
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Rounded.Close, "关闭",
                                modifier = Modifier.size(14.dp).clickable { state.closeTab(i) },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        PathCrumbs(
            path = state.path,
            onUp = { state.up() },
            onNavigate = { dir -> state.navigate(dir) }
        )

        if (state.clip != null) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "已${if (state.clip!!.cut) "剪切" else "复制"} ${state.clip!!.paths.size} 项",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { state.clip = null }) { Text("取消") }
                    TextButton(onClick = { paste() }) { Text("粘贴到此处") }
                }
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.loading -> LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
                state.entries.isEmpty() -> EmptyDir(Modifier.align(Alignment.Center))
                state.viewMode == ViewMode.GRID -> LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(9.dp)
                ) {
                    itemsIndexed(state.entries) { _, entry ->
                        FileGridCell(
                            entry = entry,
                            selected = state.selection.contains(entry.file.absolutePath),
                            onClick = {
                                if (state.selection.isNotEmpty()) {
                                    state.toggleSelect(entry.file)
                                } else if (entry.isDir) {
                                    state.navigate(entry.file)
                                } else {
                                    onOpen(entry.file)
                                }
                            },
                            onLongClick = {
                                state.toggleSelect(entry.file)
                                menuTarget = entry.file
                            }
                        )
                    }
                }
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(state.entries, key = { it.file.absolutePath }) { entry ->
                        FileRow(
                            entry = entry,
                            selected = state.selection.contains(entry.file.absolutePath),
                            onClick = {
                                if (state.selection.isNotEmpty()) {
                                    state.toggleSelect(entry.file)
                                } else if (entry.isDir) {
                                    state.navigate(entry.file)
                                } else {
                                    onOpen(entry.file)
                                }
                            },
                            onLongClick = {
                                state.toggleSelect(entry.file)
                                menuTarget = entry.file
                            }
                        )
                    }
                    item { Spacer(Modifier.height(110.dp)) }
                }
            }
        }

        if (state.selection.isNotEmpty()) {
            SelectionBar(
                count = state.selection.size,
                allSelected = state.selection.size == state.entries.size,
                onSelectAll = {
                    state.selection = if (state.selection.size == state.entries.size) emptySet()
                    else state.entries.map { it.file.absolutePath }.toSet()
                },
                onCopy = { copySelection(false) },
                onCut = { copySelection(true) },
                onDelete = { deleteTargets = state.selectedFiles() },
                onMore = { menuTarget = state.selectedFiles().firstOrNull() },
                onClose = { state.selection = emptySet() }
            )
        }
    }

    if (progress != null) {
        ProgressDialog(progress!!)
    }

    if (showNewDir) {
        InputDialog("新建文件夹", "文件夹名称", "新建文件夹", onDismiss = { showNewDir = false }) { name ->
            showNewDir = false
            try {
                FileRepo.createDir(state.activeDir, name); refresh()
            } catch (e: Exception) { state.message = e.message }
        }
    }

    if (showNewFile) {
        InputDialog("新建文件", "文件名", "新建文件.txt", onDismiss = { showNewFile = false }) { name ->
            showNewFile = false
            try {
                FileRepo.createFile(state.activeDir, name); refresh()
            } catch (e: Exception) { state.message = e.message }
        }
    }

    if (showPath) {
        InputDialog("跳转路径", "绝对路径", state.path, onDismiss = { showPath = false }) { p ->
            showPath = false
            val f = File(p)
            if (f.isDirectory) state.navigate(f) else state.message = "路径不存在"
        }
    }

    renameTarget?.let { target ->
        InputDialog("重命名", "新名称", target.name, selectNameOnly = true, onDismiss = { renameTarget = null }) { name ->
            renameTarget = null
            rename(target, name)
        }
    }

    propsTarget?.let { target ->
        PropertiesDialog(target, onDismiss = { propsTarget = null })
    }

    if (deleteTargets.isNotEmpty()) {
        ConfirmDialog(
            title = "删除",
            text = "确定永久删除 ${deleteTargets.size} 项？此操作不可撤销。",
            confirmText = "删除",
            danger = true,
            onDismiss = { deleteTargets = emptyList() },
            onConfirm = {
                val list = deleteTargets
                deleteTargets = emptyList()
                delete(list)
            }
        )
    }

    if (batchRename) {
        BatchRenameDialog(
            count = state.selection.size,
            onDismiss = { batchRename = false },
            onConfirm = { prefix, start ->
                batchRename = false
                val files = state.selectedFiles().sortedBy { it.name }
                scope.launch {
                    var i = start
                    files.forEach { f ->
                        val ext = if (f.isDirectory) "" else ".${Fmt.ext(f.name)}"
                        runCatching { FileRepo.rename(f, "$prefix$i$ext") }
                        i++
                    }
                    state.selection = emptySet()
                    refresh()
                }
            }
        )
    }

    menuTarget?.let { target ->
        val isMulti = state.selection.size > 1
        val targets = if (isMulti) state.selectedFiles() else listOf(target)
        ModalBottomSheet(onDismissRequest = { menuTarget = null }) {
            Column(Modifier.padding(bottom = 24.dp)) {
                Text(
                    if (isMulti) "已选择 ${targets.size} 项" else target.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SheetAction("复制", Icons.Rounded.ContentCopy) { menuTarget = null; copySelection(false) }
                SheetAction("剪切", Icons.Rounded.ContentCut) { menuTarget = null; copySelection(true) }
                SheetAction("重命名", Icons.Rounded.Description) { menuTarget = null; if (!isMulti) renameTarget = target }
                SheetAction("批量重命名", Icons.Rounded.SelectAll) { menuTarget = null; batchRename = true }
                SheetAction("压缩为 ZIP", Icons.Rounded.FolderZip) { menuTarget = null; compress(targets) }
                if (!isMulti && Fmt.isArchive(target.name) && !Fmt.isApk(target.name)) {
                    SheetAction("解压到当前目录", Icons.Rounded.FolderZip) { menuTarget = null; extract(target) }
                }
                if (target.isDirectory && !isMulti) {
                    val favs = state.prefs.favorites
                    val isFav = favs.contains(target.absolutePath)
                    SheetAction(if (isFav) "取消收藏" else "收藏此文件夹", Icons.Rounded.Star) {
                        menuTarget = null
                        state.prefs.favorites = if (isFav) favs - target.absolutePath else favs + target.absolutePath
                        state.message = if (isFav) "已取消收藏" else "已收藏"
                    }
                }
                SheetAction("属性", Icons.Rounded.Info) { menuTarget = null; propsTarget = target }
                SheetAction("分享", Icons.Rounded.Share) {
                    menuTarget = null
                    shareFile(context, target)
                }
                SheetAction("删除", Icons.Rounded.Delete, danger = true) { menuTarget = null; deleteTargets = targets }
            }
        }
    }
}

@Composable
private fun SelectionBar(
    count: Int,
    allSelected: Boolean,
    onSelectAll: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onDelete: () -> Unit,
    onMore: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "取消") }
            Text("$count 项", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = onSelectAll) { Icon(Icons.Rounded.Check, if (allSelected) "取消全选" else "全选") }
            IconButton(onClick = onCopy) { Icon(Icons.Rounded.ContentCopy, "复制") }
            IconButton(onClick = onCut) { Icon(Icons.Rounded.ContentCut, "剪切") }
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, "删除", tint = MaterialTheme.colorScheme.error) }
            IconButton(onClick = onMore) { Icon(Icons.Rounded.MoreVert, "更多") }
        }
    }
}

@Composable
private fun SheetAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, label, tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(16.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PathCrumbs(path: String, onUp: () -> Unit, onNavigate: (File) -> Unit) {
    val parts = path.trim('/').split('/').filter { it.isNotEmpty() }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundIconButton(Icons.Rounded.ArrowBack, "上级目录", onUp)
        Spacer(Modifier.width(8.dp))
        LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            item {
                Crumb("/") { onNavigate(File("/")) }
            }
            items(parts.size) { i ->
                val sub = parts.subList(0, i + 1).joinToString("/")
                val target = File("/", sub)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("/", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                    Crumb(parts[i]) { onNavigate(target) }
                }
            }
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun Crumb(text: String, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    )
}

@Composable
private fun EmptyDir(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("空文件夹", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text("没有权限或这里确实没有文件", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PropertiesDialog(file: File, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var size by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(file) {
        size = if (file.isDirectory) FileRepo.sizeOf(file) else file.length()
    }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("属性", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                PropRow("名称", file.name)
                PropRow("路径", file.absolutePath)
                PropRow("类型", if (file.isDirectory) "文件夹" else (Fmt.ext(file.name).uppercase().ifEmpty { "文件" }))
                PropRow("大小", size?.let { Fmt.size(it) } ?: "计算中…")
                PropRow("修改时间", Fmt.date(file.lastModified()))
                if (!file.isDirectory) {
                    PropRow("读取权限", if (file.canRead()) "可读" else "不可读")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
private fun PropRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(76.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun BatchRenameDialog(count: Int, onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var prefix by remember { mutableStateOf("文件") }
    var start by remember { mutableStateOf("1") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("批量重命名 $count 项") },
        text = {
            Column {
                androidx.compose.material3.OutlinedTextField(
                    value = prefix,
                    onValueChange = { prefix = it },
                    label = { Text("前缀") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = start,
                    onValueChange = { start = it.filter { c -> c.isDigit() } },
                    label = { Text("起始编号") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text("示例：${prefix}${start.ifEmpty { "1" }}.jpg", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(prefix.ifBlank { "文件" }, start.toIntOrNull() ?: 1) }) { Text("重命名") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

private fun shareFile(context: android.content.Context, file: File) {
    runCatching {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = Fmt.mimeOf(file.name)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享"))
    }
}
