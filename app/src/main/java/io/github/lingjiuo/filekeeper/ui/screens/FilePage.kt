package io.github.lingjiuo.filekeeper.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.lingjiuo.filekeeper.data.ArchiveRepo
import io.github.lingjiuo.filekeeper.data.FileRepo
import io.github.lingjiuo.filekeeper.model.Clip
import io.github.lingjiuo.filekeeper.model.FileEntry
import io.github.lingjiuo.filekeeper.model.SortMode
import io.github.lingjiuo.filekeeper.model.ViewMode
import io.github.lingjiuo.filekeeper.ui.AppState
import io.github.lingjiuo.filekeeper.ui.components.FileRow
import io.github.lingjiuo.filekeeper.ui.components.MiuixConfirmDialog
import io.github.lingjiuo.filekeeper.ui.components.MiuixInputDialog
import io.github.lingjiuo.filekeeper.ui.components.MiuixProgressDialog
import io.github.lingjiuo.filekeeper.ui.components.OpProgress
import io.github.lingjiuo.filekeeper.util.Fmt
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.extra.SuperBottomSheet
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File

@Composable
fun FilePage(state: AppState, onOpen: (File) -> Unit) {
    val scope = rememberCoroutineScope()
    var reload by remember { mutableIntStateOf(0) }
    var progress by remember { mutableStateOf<OpProgress?>(null) }
    var sortSheet by remember { mutableStateOf(false) }
    var moreSheet by remember { mutableStateOf(false) }
    var actionTarget by remember { mutableStateOf<File?>(null) }
    var renameTarget by remember { mutableStateOf<File?>(null) }
    var propsTarget by remember { mutableStateOf<File?>(null) }
    var deleteTargets by remember { mutableStateOf<List<File>>(emptyList()) }
    var newFolder by remember { mutableStateOf(false) }
    var newFile by remember { mutableStateOf(false) }
    var pathDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.path, state.sort, state.ascending, state.showHidden, reload) {
        state.loading = true
        state.entries = FileRepo.list(File(state.path), state.showHidden, state.sort, state.ascending)
        state.loading = false
    }
    LaunchedEffect(state.secondPath, state.dualPane, reload) {
        if (state.dualPane) {
            state.secondEntries = FileRepo.list(File(state.secondPath), state.showHidden, state.sort, state.ascending)
        }
    }

    fun refresh() { reload++ }

    fun paste() {
        val clip = state.clip ?: return
        val sources = clip.paths.map { File(it) }.filter { it.exists() }
        if (sources.isEmpty()) { state.clip = null; return }
        val dest = state.activeDir
        scope.launch {
            progress = OpProgress(if (clip.cut) "正在移动" else "正在复制", 0, sources.size, "")
            try {
                if (clip.cut) FileRepo.move(sources, dest) { i, t, n, _ ->
                    progress = OpProgress("正在移动", i - 1, t, n)
                } else FileRepo.copy(sources, dest) { i, t, n, _ ->
                    progress = OpProgress("正在复制", i - 1, t, n)
                }
                state.clip = null
                state.message = "完成，共 ${sources.size} 项"
            } catch (e: Exception) {
                state.message = "失败：${e.message}"
            } finally {
                progress = null
                refresh()
            }
        }
    }

    fun delete(files: List<File>) {
        scope.launch {
            progress = OpProgress("正在删除", 0, files.size, "")
            try {
                val ok = FileRepo.delete(files) { i, t, n -> progress = OpProgress("正在删除", i - 1, t, n) }
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

    fun compress(files: List<File>, sevenZip: Boolean) {
        val ext = if (sevenZip) ".7z" else ".zip"
        val name = if (files.size == 1) files[0].name.substringBeforeLast('.') + ext else "压缩包$ext"
        val dest = File(state.activeDir, name)
        scope.launch {
            progress = OpProgress("正在压缩", 0, files.size, "")
            try {
                if (sevenZip) ArchiveRepo.create7z(files, dest) { i, t, n ->
                    progress = OpProgress("正在压缩", i - 1, t, n)
                } else ArchiveRepo.createZip(files, dest) { i, t, n ->
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
        val dest = File(state.activeDir, file.name.removeSuffix(".${Fmt.ext(file.name)}"))
        scope.launch {
            progress = OpProgress("正在解压", 0, 0, file.name)
            try {
                dest.mkdirs()
                ArchiveRepo.extract(file, dest) { i, t, n -> progress = OpProgress("正在解压", i, t, n) }
                state.message = "已解压到 ${dest.name}"
            } catch (e: Exception) {
                state.message = "解压失败：${e.message}"
            } finally {
                progress = null
                refresh()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = state.path,
                navigationIcon = {
                    IconButton(onClick = { state.up() }) {
                        Icon(MiuixIcons.Back, "上级目录")
                    }
                },
                actions = {
                    IconButton(onClick = { sortSheet = true }) { Icon(MiuixIcons.Sort, "排序") }
                    IconButton(onClick = { moreSheet = true }) { Icon(MiuixIcons.More, "更多") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)
        ) {
            if (state.clip != null) {
                item {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "已复制 ${state.clip!!.paths.size} 项",
                                Modifier.weight(1f),
                                style = MiuixTheme.textStyles.body2
                            )
                            TextButton("粘贴", onClick = { paste() })
                        }
                    }
                }
            }
            items(state.entries, key = { it.file.absolutePath }) { entry ->
                FileRow(
                    entry = entry,
                    selected = state.selection.contains(entry.file.absolutePath),
                    onClick = {
                        if (state.selection.isNotEmpty()) state.toggleSelect(entry.file)
                        else if (entry.isDir) state.navigate(entry.file)
                        else onOpen(entry.file)
                    },
                    onLongClick = {
                        state.toggleSelect(entry.file)
                        actionTarget = entry.file
                    }
                )
            }
        }
    }

    if (progress != null) MiuixProgressDialog(progress!!)

    if (sortSheet) {
        SuperBottomSheet(show = true, title = "排序", onDismissRequest = { sortSheet = false }) {
            Column {
                SortMode.entries.forEach { mode ->
                    val label = when (mode) {
                        SortMode.NAME -> "按名称"
                        SortMode.SIZE -> "按大小"
                        SortMode.DATE -> "按时间"
                        SortMode.TYPE -> "按类型"
                    }
                    SheetItem(label + if (state.sort == mode) "  ✓" else "") {
                        state.sort = mode
                        state.applyPrefs()
                        sortSheet = false
                    }
                }
                SheetItem(if (state.ascending) "改为降序" else "改为升序") {
                    state.ascending = !state.ascending
                    state.applyPrefs()
                    sortSheet = false
                }
            }
        }
    }

    if (moreSheet) {
        SuperBottomSheet(show = true, title = "更多", onDismissRequest = { moreSheet = false }) {
            Column {
                SheetItem("新建文件夹") { moreSheet = false; newFolder = true }
                SheetItem("新建文件") { moreSheet = false; newFile = true }
                SheetItem("跳转路径") { moreSheet = false; pathDialog = true }
                SheetItem(if (state.showHidden) "隐藏隐藏文件" else "显示隐藏文件") {
                    state.showHidden = !state.showHidden
                    state.applyPrefs()
                    moreSheet = false
                }
                SheetItem(if (state.viewMode == ViewMode.LIST) "切换网格视图" else "切换列表视图") {
                    state.viewMode = if (state.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
                    state.applyPrefs()
                    moreSheet = false
                }
                SheetItem(if (state.dualPane) "关闭双窗格" else "开启双窗格") {
                    state.dualPane = !state.dualPane
                    if (state.dualPane) state.secondPath = state.path
                    moreSheet = false
                }
                SheetItem("刷新") { moreSheet = false; refresh() }
            }
        }
    }

    actionTarget?.let { target ->
        val targets = if (state.selection.size > 1) state.selectedFiles() else listOf(target)
        SuperBottomSheet(
            show = true,
            title = if (targets.size > 1) "已选择 ${targets.size} 项" else target.name,
            onDismissRequest = { actionTarget = null }
        ) {
            Column {
                SheetItem("复制") {
                    state.clip = Clip(targets.map { it.absolutePath }, false)
                    state.selection = emptySet()
                    actionTarget = null
                }
                SheetItem("剪切") {
                    state.clip = Clip(targets.map { it.absolutePath }, true)
                    state.selection = emptySet()
                    actionTarget = null
                }
                SheetItem("重命名") { actionTarget = null; renameTarget = target }
                SheetItem("压缩为 ZIP") { actionTarget = null; compress(targets, false) }
                SheetItem("压缩为 7Z") { actionTarget = null; compress(targets, true) }
                if (!Fmt.isApk(target.name) && Fmt.isArchive(target.name)) {
                    SheetItem("解压到当前目录") { actionTarget = null; extract(target) }
                }
                SheetItem("属性") { actionTarget = null; propsTarget = target }
                SheetItem("删除") { actionTarget = null; deleteTargets = targets }
            }
        }
    }

    if (newFolder) MiuixInputDialog(
        title = "新建文件夹", label = "名称", initial = "",
        onDismiss = { newFolder = false },
        onConfirm = { name ->
            newFolder = false
            try { FileRepo.createDir(state.activeDir, name); refresh() }
            catch (e: Exception) { state.message = e.message }
        }
    )

    if (newFile) MiuixInputDialog(
        title = "新建文件", label = "文件名", initial = "新建文件.txt",
        onDismiss = { newFile = false },
        onConfirm = { name ->
            newFile = false
            try { FileRepo.createFile(state.activeDir, name); refresh() }
            catch (e: Exception) { state.message = e.message }
        }
    )

    if (pathDialog) MiuixInputDialog(
        title = "跳转路径", label = "绝对路径", initial = state.path,
        onDismiss = { pathDialog = false },
        onConfirm = { p ->
            pathDialog = false
            val f = File(p)
            if (f.isDirectory) state.navigate(f) else state.message = "路径不存在"
        }
    )

    renameTarget?.let { target ->
        MiuixInputDialog(
            title = "重命名", label = "新名称", initial = target.name,
            onDismiss = { renameTarget = null },
            onConfirm = { name ->
                renameTarget = null
                try { FileRepo.rename(target, name); refresh() }
                catch (e: Exception) { state.message = e.message }
            }
        )
    }

    propsTarget?.let { target ->
        OverlayDialogProps(target) { propsTarget = null }
    }

    if (deleteTargets.isNotEmpty()) {
        MiuixConfirmDialog(
            title = "删除",
            text = "确定永久删除 ${deleteTargets.size} 项？",
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
}

@Composable
private fun SheetItem(text: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)
    ) {
        Text(text, style = MiuixTheme.textStyles.title4)
    }
}

@Composable
private fun OverlayDialogProps(file: File, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var size by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(file) {
        size = if (file.isDirectory) FileRepo.sizeOf(file) else file.length()
    }
    SuperDialog(
        show = true,
        title = "属性",
        onDismissRequest = onDismiss
    ) {
        Column {
            PropLine("名称", file.name)
            PropLine("路径", file.absolutePath)
            PropLine("大小", size?.let { Fmt.size(it) } ?: "计算中…")
            PropLine("修改时间", Fmt.date(file.lastModified()))
        }
    }
}

@Composable
private fun PropLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, Modifier.width(72.dp), style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        Text(value, style = MiuixTheme.textStyles.body2, maxLines = 3, overflow = TextOverflow.Ellipsis)
    }
}
