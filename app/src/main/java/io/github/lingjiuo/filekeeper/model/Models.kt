package io.github.lingjiuo.filekeeper.model

import java.io.File

enum class SortMode { NAME, SIZE, DATE, TYPE }

enum class ViewMode { LIST, GRID }

enum class Section { FILES, FAVORITES, SEARCH, STORAGE, SETTINGS }

data class FileEntry(
    val file: File,
    val name: String,
    val isDir: Boolean,
    val size: Long,
    val modified: Long,
    val childCount: Int = -1
)

data class Clip(val paths: List<String>, val cut: Boolean)

data class ArchiveEntry(
    val name: String,
    val size: Long,
    val isDir: Boolean,
    val time: Long = 0L
)

data class StorageItem(
    val file: File,
    val size: Long,
    val isDir: Boolean
)
