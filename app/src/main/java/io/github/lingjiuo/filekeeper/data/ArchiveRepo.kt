package io.github.lingjiuo.filekeeper.data

import io.github.lingjiuo.filekeeper.model.ArchiveEntry
import io.github.lingjiuo.filekeeper.util.Fmt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile as JZipFile
import kotlin.coroutines.coroutineContext

data class ArchiveInfo(val format: String, val entries: List<ArchiveEntry>)

object ArchiveRepo {

    fun formatOf(f: File): String {
        val n = f.name.lowercase()
        return when {
            n.endsWith(".tar.gz") || n.endsWith(".tgz") -> "tar.gz"
            n.endsWith(".tar.bz2") || n.endsWith(".tbz2") -> "tar.bz2"
            n.endsWith(".tar.xz") || n.endsWith(".txz") -> "tar.xz"
            else -> Fmt.ext(n)
        }
    }

    fun list(f: File): ArchiveInfo {
        val fmt = formatOf(f)
        val entries = when (fmt) {
            "zip", "jar", "apk" -> listZip(f)
            "7z" -> list7z(f)
            "rar" -> listRar(f)
            "tar", "tar.gz", "tar.bz2", "tar.xz" -> listTar(f, fmt)
            "gz", "bz2", "xz" -> listSingle(f, fmt)
            else -> emptyList()
        }
        return ArchiveInfo(fmt, entries)
    }

    private fun listZip(f: File): List<ArchiveEntry> = JZipFile(f).use { zip ->
        zip.entries().asSequence().map { e ->
            ArchiveEntry(e.name, e.size.coerceAtLeast(0), e.isDirectory, e.time)
        }.toList()
    }

    private fun list7z(f: File): List<ArchiveEntry> {
        val res = mutableListOf<ArchiveEntry>()
        SevenZFile.builder().setFile(f).get().use { z ->
            var e = z.nextEntry
            while (e != null) {
                res += ArchiveEntry(e.name, e.size, e.isDirectory, e.lastModifiedDate?.time ?: 0L)
                e = z.nextEntry
            }
        }
        return res
    }

    private fun listRar(f: File): List<ArchiveEntry> {
        val archive = com.github.junrar.Archive(f)
        return try {
            archive.fileHeaders.map { h ->
                ArchiveEntry(h.fileName, h.fullUnpackSize, h.isDirectory, h.mTime?.time ?: 0L)
            }
        } finally {
            archive.close()
        }
    }

    private fun listTar(f: File, fmt: String): List<ArchiveEntry> {
        val res = mutableListOf<ArchiveEntry>()
        openCompressed(f, fmt).use { input ->
            TarArchiveInputStream(BufferedInputStream(input)).use { tar ->
                var e = tar.nextEntry
                while (e != null) {
                    res += ArchiveEntry(e.name, e.size, e.isDirectory, e.lastModifiedDate?.time ?: 0L)
                    e = tar.nextEntry
                }
            }
        }
        return res
    }

    private fun listSingle(f: File, fmt: String): List<ArchiveEntry> {
        val name = f.name.removeSuffix(".$fmt")
        return listOf(ArchiveEntry(name, f.length(), false))
    }

    private fun openCompressed(f: File, fmt: String): InputStream {
        val raw = BufferedInputStream(FileInputStream(f))
        return when (fmt) {
            "tar.gz" -> GzipCompressorInputStream(raw)
            "tar.bz2" -> BZip2CompressorInputStream(raw)
            "tar.xz" -> XZCompressorInputStream(raw)
            "gz" -> GzipCompressorInputStream(raw)
            "bz2" -> BZip2CompressorInputStream(raw)
            "xz" -> XZCompressorInputStream(raw)
            else -> raw
        }
    }

    suspend fun extract(
        f: File,
        destDir: File,
        onProgress: (done: Int, total: Int, name: String) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        val fmt = formatOf(f)
        var count = 0
        when (fmt) {
            "zip", "jar", "apk" -> {
                val entries = listZip(f)
                val total = entries.size
                JZipFile(f).use { zip ->
                    zip.entries().asSequence().forEach { e ->
                        coroutineContext.ensureActive()
                        val out = safeChild(destDir, e.name) ?: return@forEach
                        if (e.isDirectory) {
                            out.mkdirs()
                        } else {
                            out.parentFile?.mkdirs()
                            zip.getInputStream(e).use { input ->
                                FileOutputStream(out).use { o -> input.copyTo(o) }
                            }
                        }
                        count++
                        onProgress(count, total, e.name)
                    }
                }
            }
            "7z" -> {
                val entries = list7z(f)
                SevenZFile.builder().setFile(f).get().use { z ->
                    var e = z.nextEntry
                    var done = 0
                    while (e != null) {
                        coroutineContext.ensureActive()
                        val out = safeChild(destDir, e.name)
                        if (out != null) {
                            if (e.isDirectory) out.mkdirs() else {
                                out.parentFile?.mkdirs()
                                FileOutputStream(out).use { o -> z.getInputStream(e).copyTo(o) }
                            }
                        }
                        done++
                        onProgress(done, entries.size, e.name)
                        e = z.nextEntry
                    }
                    count = done
                }
            }
            "rar" -> {
                val archive = com.github.junrar.Archive(f)
                try {
                    val headers = archive.fileHeaders
                    headers.forEachIndexed { i, h ->
                        coroutineContext.ensureActive()
                        val out = safeChild(destDir, h.fileName)
                        if (out != null) {
                            if (h.isDirectory) out.mkdirs() else {
                                out.parentFile?.mkdirs()
                                FileOutputStream(out).use { o ->
                                    archive.extractFile(h, o)
                                }
                            }
                        }
                        onProgress(i + 1, headers.size, h.fileName)
                    }
                    count = headers.size
                } finally {
                    archive.close()
                }
            }
            "tar", "tar.gz", "tar.bz2", "tar.xz" -> {
                val entries = listTar(f, fmt)
                openCompressed(f, fmt).use { input ->
                    TarArchiveInputStream(BufferedInputStream(input)).use { tar ->
                        var e = tar.nextEntry
                        while (e != null) {
                            coroutineContext.ensureActive()
                            val out = safeChild(destDir, e.name)
                            if (out != null) {
                                if (e.isDirectory) out.mkdirs() else {
                                    out.parentFile?.mkdirs()
                                    FileOutputStream(out).use { o -> tar.copyTo(o) }
                                }
                                count++
                                onProgress(count, entries.size, e.name)
                            }
                            e = tar.nextEntry
                        }
                    }
                }
            }
            else -> {
                val name = f.name.removeSuffix(".${fmt}")
                val out = File(destDir, name)
                openCompressed(f, fmt).use { input ->
                    FileOutputStream(out).use { o -> input.copyTo(o) }
                }
                count = 1
                onProgress(1, 1, name)
            }
        }
        count
    }

    private fun safeChild(base: File, name: String): File? {
        val clean = name.replace('\\', '/').trimStart('/')
        val out = File(base, clean)
        val basePath = base.canonicalPath
        val outPath = out.canonicalPath
        return if (outPath == basePath || outPath.startsWith(basePath + File.separator)) out else null
    }

    suspend fun createZip(
        sources: List<File>,
        dest: File,
        onProgress: (Int, Int, String) -> Unit
    ) = withContext(Dispatchers.IO) {
        dest.parentFile?.mkdirs()
        if (dest.exists()) dest.delete()
        val zip = net.lingala.zip4j.ZipFile(dest)
        val params = ZipParameters().apply {
            compressionMethod = CompressionMethod.DEFLATE
            compressionLevel = CompressionLevel.NORMAL
            isIncludeRootFolder = false
        }
        sources.forEachIndexed { i, s ->
            coroutineContext.ensureActive()
            if (s.isDirectory) zip.addFolder(s, params) else zip.addFile(s, params)
            onProgress(i + 1, sources.size, s.name)
        }
    }

    /** 用 7-Zip 容器打包（commons-compress 的 SevenZOutputFile）。 */
    suspend fun create7z(
        sources: List<File>,
        dest: File,
        onProgress: (Int, Int, String) -> Unit
    ) = withContext(Dispatchers.IO) {
        dest.parentFile?.mkdirs()
        if (dest.exists()) dest.delete()
        var done = 0
        SevenZOutputFile(dest).use { out ->
            suspend fun add(f: File, base: String) {
                coroutineContext.ensureActive()
                if (f.isDirectory) {
                    val entry = SevenZArchiveEntry().apply {
                        name = base
                        isDirectory = true
                    }
                    out.putArchiveEntry(entry)
                    out.closeArchiveEntry()
                    f.listFiles()?.forEach { add(it, "$base/${it.name}") }
                } else {
                    val entry = SevenZArchiveEntry().apply {
                        name = base
                        size = f.length()
                    }
                    out.putArchiveEntry(entry)
                    f.inputStream().use { input ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            coroutineContext.ensureActive()
                            val read = input.read(buffer)
                            if (read <= 0) break
                            out.write(buffer, 0, read)
                        }
                    }
                    out.closeArchiveEntry()
                }
                done++
                onProgress(done, sources.size, f.name)
            }
            sources.forEach { add(it, it.name) }
        }
    }

    /** 向已有 zip 追加文件 */
    suspend fun addToZip(zipFile: File, sources: List<File>) = withContext(Dispatchers.IO) {
        val zip = net.lingala.zip4j.ZipFile(zipFile)
        val params = ZipParameters().apply { compressionMethod = CompressionMethod.DEFLATE }
        sources.forEach { if (it.isDirectory) zip.addFolder(it, params) else zip.addFile(it, params) }
    }

    fun isSupported(name: String): Boolean = formatOf(File(name)) in setOf("zip", "jar", "apk", "7z", "rar", "tar", "tar.gz", "tar.bz2", "tar.xz", "gz", "bz2", "xz")
}
