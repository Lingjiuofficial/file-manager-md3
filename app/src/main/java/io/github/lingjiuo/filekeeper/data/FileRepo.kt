package io.github.lingjiuo.filekeeper.data

import io.github.lingjiuo.filekeeper.model.FileEntry
import io.github.lingjiuo.filekeeper.model.SortMode
import io.github.lingjiuo.filekeeper.util.Fmt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.coroutines.coroutineContext

object FileRepo {

    fun listBlocking(dir: File, showHidden: Boolean, sort: SortMode, ascending: Boolean): List<FileEntry> {
        val raw = dir.listFiles() ?: return emptyList()
        val entries = raw.asSequence()
            .filter { showHidden || !it.name.startsWith(".") }
            .map { f ->
                FileEntry(
                    file = f,
                    name = f.name,
                    isDir = f.isDirectory,
                    size = if (f.isDirectory) 0L else f.length(),
                    modified = f.lastModified()
                )
            }
            .toList()

        val cmp = Comparator<FileEntry> { a, b ->
            if (a.isDir != b.isDir) return@Comparator if (a.isDir) -1 else 1
            when (sort) {
                SortMode.NAME -> Fmt.humanCompare(a.name, b.name)
                SortMode.SIZE -> a.size.compareTo(b.size)
                SortMode.DATE -> a.modified.compareTo(b.modified)
                SortMode.TYPE -> {
                    val ea = Fmt.ext(a.name); val eb = Fmt.ext(b.name)
                    if (ea == eb) Fmt.humanCompare(a.name, b.name) else ea.compareTo(eb)
                }
            }
        }
        val sorted = if (ascending) entries.sortedWith(cmp) else entries.sortedWith(cmp.reversed())
        // 目录始终排在最前
        return sorted.sortedByDescending { it.isDir }
    }

    suspend fun list(dir: File, showHidden: Boolean, sort: SortMode, ascending: Boolean): List<FileEntry> =
        withContext(Dispatchers.IO) { listBlocking(dir, showHidden, sort, ascending) }

    suspend fun sizeOf(file: File, shouldCount: () -> Boolean = { true }): Long = withContext(Dispatchers.IO) {
        var total = 0L
        if (file.isFile) return@withContext file.length()
        val stack = ArrayDeque<File>()
        stack.add(file)
        while (stack.isNotEmpty()) {
            coroutineContext.ensureActive()
            if (!shouldCount()) break
            val d = stack.removeLast()
            val kids = d.listFiles() ?: continue
            for (k in kids) {
                if (k.isDirectory) stack.add(k) else total += k.length()
            }
        }
        total
    }

    /** 返回创建成功的文件；失败抛异常。 */
    suspend fun copy(
        sources: List<File>,
        destDir: File,
        onProgress: (index: Int, total: Int, name: String, fraction: Float) -> Unit
    ): List<File> = withContext(Dispatchers.IO) {
        val created = mutableListOf<File>()
        val destPath = destDir.canonicalPath
        sources.forEachIndexed { index, src ->
            coroutineContext.ensureActive()
            val srcPath = src.canonicalPath
            if (src.isDirectory && (destPath == srcPath || destPath.startsWith(srcPath + File.separator))) {
                throw IllegalStateException("不能把文件夹复制到它自己里面")
            }
            val target = if (src.isDirectory) resolveDir(destDir, src.name) else Fmt.uniqueFile(destDir, src.name)
            copyRecursive(src, target) { fraction -> onProgress(index + 1, sources.size, src.name, fraction) }
            created += target
        }
        created
    }

    private fun resolveDir(destDir: File, name: String): File {
        val f = File(destDir, name)
        if (!f.exists()) return f
        var i = 1
        while (true) {
            val c = File(destDir, "$name ($i)")
            if (!c.exists()) return c
            i++
        }
    }

    private suspend fun copyRecursive(src: File, dst: File, onFraction: (Float) -> Unit) {
        coroutineContext.ensureActive()
        if (src.isDirectory) {
            dst.mkdirs()
            val kids = src.listFiles() ?: return
            var index = 0
            for (k in kids) {
                copyRecursive(k, File(dst, k.name)) { f -> onFraction((index + f) / kids.size) }
                index++
            }
        } else {
            dst.parentFile?.mkdirs()
            FileInputStream(src).use { input ->
                FileOutputStream(dst).use { output ->
                    val buf = ByteArray(128 * 1024)
                    val total = src.length().coerceAtLeast(1)
                    var done = 0L
                    while (true) {
                        coroutineContext.ensureActive()
                        val n = input.read(buf)
                        if (n <= 0) break
                        output.write(buf, 0, n)
                        done += n
                        onFraction((done.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
            dst.setLastModified(src.lastModified())
        }
    }

    suspend fun move(
        sources: List<File>,
        destDir: File,
        onProgress: (index: Int, total: Int, name: String, fraction: Float) -> Unit
    ): List<File> = withContext(Dispatchers.IO) {
        val moved = mutableListOf<File>()
        sources.forEachIndexed { index, src ->
            coroutineContext.ensureActive()
            if (src.parentFile?.canonicalPath == destDir.canonicalPath) {
                moved += src
                return@forEachIndexed
            }
            val srcPath = src.canonicalPath
            if (src.isDirectory && destDir.canonicalPath.startsWith(srcPath + File.separator)) {
                throw IllegalStateException("不能把文件夹移动到它自己里面")
            }
            val direct = File(destDir, src.name)
            if (!direct.exists() && src.renameTo(direct)) {
                moved += direct
                onProgress(index + 1, sources.size, src.name, 1f)
            } else {
                val target = if (src.isDirectory) resolveDir(destDir, src.name) else Fmt.uniqueFile(destDir, src.name)
                copyRecursive(src, target) { f -> onProgress(index + 1, sources.size, src.name, f) }
                deleteRecursive(src)
                moved += target
            }
        }
        moved
    }

    suspend fun delete(files: List<File>, onProgress: (index: Int, total: Int, name: String) -> Unit): Int =
        withContext(Dispatchers.IO) {
            var ok = 0
            files.forEachIndexed { i, f ->
                coroutineContext.ensureActive()
                onProgress(i + 1, files.size, f.name)
                if (deleteRecursive(f)) ok++
            }
            ok
        }

    private fun deleteRecursive(f: File): Boolean {
        if (f.isDirectory) {
            f.listFiles()?.forEach { deleteRecursive(it) }
        }
        return f.delete()
    }

    fun rename(file: File, newName: String): File {
        val clean = Fmt.safeName(newName)
        val target = File(file.parentFile, clean)
        if (target.exists() && target.absolutePath != file.absolutePath) {
            throw IllegalStateException("同名文件已存在")
        }
        if (!file.renameTo(target)) throw IllegalStateException("重命名失败")
        return target
    }

    fun createFile(dir: File, name: String): File {
        val f = File(dir, Fmt.safeName(name))
        if (f.exists()) throw IllegalStateException("同名文件已存在")
        if (!f.createNewFile()) throw IllegalStateException("创建失败")
        return f
    }

    fun createDir(dir: File, name: String): File {
        val f = File(dir, Fmt.safeName(name))
        if (f.exists()) throw IllegalStateException("同名文件夹已存在")
        if (!f.mkdirs()) throw IllegalStateException("创建失败")
        return f
    }

    suspend fun search(
        root: File,
        query: String,
        category: String?,
        limit: Int = 300,
        onFound: (File) -> Unit
    ) = withContext(Dispatchers.IO) {
        val q = query.lowercase()
        val stack = ArrayDeque<File>()
        stack.add(root)
        var found = 0
        while (stack.isNotEmpty() && found < limit) {
            coroutineContext.ensureActive()
            val dir = stack.removeLast()
            val kids = dir.listFiles() ?: continue
            for (k in kids) {
                if (k.name.startsWith(".") && dir == root) continue
                if (k.isDirectory) {
                    stack.add(k)
                } else if (matches(k, q, category)) {
                    onFound(k); found++
                    if (found >= limit) break
                }
            }
        }
    }

    private fun matches(f: File, q: String, category: String?): Boolean {
        val byName = q.isBlank() || f.name.lowercase().contains(q)
        if (!byName) return false
        return when (category) {
            null -> true
            "图片" -> Fmt.isImage(f.name)
            "视频" -> Fmt.isVideo(f.name)
            "音频" -> Fmt.isAudio(f.name)
            "文档" -> Fmt.isText(f.name) || Fmt.ext(f.name) in setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx")
            "安装包" -> Fmt.isApk(f.name)
            "压缩包" -> Fmt.isArchive(f.name) && !Fmt.isApk(f.name)
            else -> true
        }
    }

    /** 扫描目录下每个子项的总大小，用于存储分析。 */
    suspend fun scanChildren(dir: File): List<FileEntry> = withContext(Dispatchers.IO) {
        val kids = dir.listFiles() ?: return@withContext emptyList()
        kids.map { f ->
            coroutineContext.ensureActive()
            val size = if (f.isDirectory) sizeOf(f) else f.length()
            FileEntry(f, f.name, f.isDirectory, size, f.lastModified())
        }.sortedByDescending { it.size }
    }

    fun freeSpace(dir: File): Long = try {
        dir.usableSpace
    } catch (e: Exception) {
        -1L
    }

    fun totalSpace(dir: File): Long = try {
        dir.totalSpace
    } catch (e: Exception) {
        -1L
    }

    /** 把未知编码的文本转成可编辑字符串，返回文本与使用的编码。 */
    fun readTextSmart(f: File, maxBytes: Int = 2 * 1024 * 1024): Pair<String, String> {
        val bytes = f.inputStream().use { input ->
            val buf = ByteArray(minOf(maxBytes, f.length().coerceAtMost(maxBytes.toLong()).toInt().coerceAtLeast(1)))
            var off = 0
            while (off < buf.size) {
                val n = input.read(buf, off, buf.size - off)
                if (n <= 0) break
                off += n
            }
            buf.copyOf(off)
        }
        return try {
            val s = String(bytes, Charsets.UTF_8)
            // 出现替换字符说明不是合法 UTF-8
            if (s.contains('\uFFFD')) throw IllegalArgumentException()
            s to "UTF-8"
        } catch (e: Exception) {
            String(bytes, charset("GBK")) to "GBK"
        }
    }

    fun writeText(f: File, text: String, charsetName: String) {
        f.writeBytes(text.toByteArray(charset(charsetName)))
    }
}
