package io.github.lingjiuo.filekeeper.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.lingjiuo.filekeeper.data.Prefs
import io.github.lingjiuo.filekeeper.model.Clip
import io.github.lingjiuo.filekeeper.model.FileEntry
import io.github.lingjiuo.filekeeper.model.Section
import io.github.lingjiuo.filekeeper.model.SortMode
import io.github.lingjiuo.filekeeper.model.ViewMode
import java.io.File

class AppState(context: Context) {

    val prefs = Prefs(context)

    var section by mutableStateOf(Section.FILES)

    var path by mutableStateOf(prefs.lastPath.coerceDir())
    var entries by mutableStateOf<List<FileEntry>>(emptyList())
    var loading by mutableStateOf(false)

    var secondPath by mutableStateOf(path)
    var secondEntries by mutableStateOf<List<FileEntry>>(emptyList())
    var dualPane by mutableStateOf(false)
    var activePane by mutableStateOf(0)

    var selection by mutableStateOf<Set<String>>(emptySet())
    var clip by mutableStateOf<Clip?>(null)

    var sort by mutableStateOf(prefs.sort)
    var ascending by mutableStateOf(prefs.ascending)
    var showHidden by mutableStateOf(prefs.showHidden)
    var viewMode by mutableStateOf(prefs.viewMode)

    var tabs by mutableStateOf(listOf(path))
    var tabIndex by mutableStateOf(0)

    var searchQuery by mutableStateOf("")
    var searchResults by mutableStateOf<List<File>>(emptyList())
    var searching by mutableStateOf(false)

    var message by mutableStateOf<String?>(null)

    var darkMode by androidx.compose.runtime.mutableIntStateOf(prefs.darkMode)
        private set
    var dynamicColor by mutableStateOf(prefs.dynamicColor)
        private set

    fun changeDarkMode(value: Int) {
        darkMode = value
        prefs.darkMode = value
    }

    fun changeDynamicColor(value: Boolean) {
        dynamicColor = value
        prefs.dynamicColor = value
    }

    val currentDir: File get() = File(path)
    val secondDir: File get() = File(secondPath)
    val activeDir: File get() = if (dualPane && activePane == 1) secondDir else currentDir
    val activeEntries: List<FileEntry> get() = if (dualPane && activePane == 1) secondEntries else entries

    fun applyPrefs() {
        prefs.sort = sort
        prefs.ascending = ascending
        prefs.showHidden = showHidden
        prefs.viewMode = viewMode
        prefs.lastPath = path
    }

    fun navigate(dir: File) {
        if (!dir.exists()) {
            message = "目录不存在"
            return
        }
        if (!dir.isDirectory) {
            message = "不是文件夹"
            return
        }
        path = dir.absolutePath
        if (dualPane && activePane == 1) secondPath = path
        selection = emptySet()
        tabs = tabs.toMutableList().also { it[tabIndex] = path }
        prefs.addRecent(path)
        prefs.lastPath = path
    }

    fun navigateSecond(dir: File) {
        if (!dir.isDirectory) return
        secondPath = dir.absolutePath
    }

    fun up() {
        val parent = activeDir.parentFile ?: return
        if (dualPane && activePane == 1) navigateSecond(parent) else navigate(parent)
    }

    fun addTab() {
        tabs = tabs + path
        tabIndex = tabs.lastIndex
    }

    fun closeTab(index: Int) {
        if (tabs.size <= 1) return
        val list = tabs.toMutableList()
        list.removeAt(index)
        tabs = list
        tabIndex = tabIndex.coerceIn(0, list.lastIndex)
        path = tabs[tabIndex]
        selection = emptySet()
    }

    fun switchTab(index: Int) {
        tabIndex = index.coerceIn(0, tabs.lastIndex)
        path = tabs[tabIndex]
        selection = emptySet()
    }

    fun toggleSelect(file: File) {
        selection = if (selection.contains(file.absolutePath)) selection - file.absolutePath
        else selection + file.absolutePath
    }

    fun selectedFiles(): List<File> = selection.map { File(it) }

    fun entryFor(file: File): FileEntry? = activeEntries.firstOrNull { it.file.absolutePath == file.absolutePath }

    fun uniqueViewEntries(): List<FileEntry> {
        val list = activeEntries
        return if (list.any { it.name.startsWith(".") } && !showHidden) list.filter { !it.name.startsWith(".") } else list
    }
}

fun String.coerceDir(): String {
    val f = File(this)
    return if (f.isDirectory) absolutePathSafe() else Prefs.defaultRoot()
}

private fun String.absolutePathSafe(): String = try {
    File(this).absolutePath
} catch (e: Exception) {
    Prefs.defaultRoot()
}
