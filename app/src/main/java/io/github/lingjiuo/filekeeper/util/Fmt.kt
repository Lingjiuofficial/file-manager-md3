package io.github.lingjiuo.filekeeper.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.TextSnippet
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Fmt {

    fun size(bytes: Long): String {
        if (bytes < 0) return "—"
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return trim(kb) + " KB"
        val mb = kb / 1024.0
        if (mb < 1024) return trim(mb) + " MB"
        val gb = mb / 1024.0
        if (gb < 1024) return trim(gb) + " GB"
        return trim(gb / 1024.0) + " TB"
    }

    private fun trim(v: Double): String =
        if (v >= 100) String.format(Locale.US, "%.0f", v)
        else if (v >= 10) String.format(Locale.US, "%.1f", v)
        else String.format(Locale.US, "%.2f", v)

    fun date(ms: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(ms))

    fun day(ms: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(ms))

    fun count(n: Int): String = if (n >= 10000) "${n / 1000} 千" else "$n"

    val imageExt = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif", "avif", "svg", "ico", "tif", "tiff")
    val videoExt = setOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "3gp", "ts", "m4v", "rmvb")
    val audioExt = setOf("mp3", "flac", "wav", "aac", "ogg", "m4a", "wma", "ape", "opus", "mid")
    val textExt = setOf("txt", "md", "json", "xml", "yaml", "yml", "ini", "conf", "log", "kt", "java", "py", "js", "ts", "c", "cpp", "h", "go", "rs", "sh", "css", "html", "htm", "sql", "gradle", "properties", "toml", "csv")
    val archiveExt = setOf("zip", "jar", "rar", "7z", "tar", "gz", "tgz", "bz2", "tbz2", "xz", "txz", "zst", "apk", "ipa")

    fun ext(name: String): String = name.substringAfterLast('.', "").lowercase(Locale.US)

    fun isImage(name: String) = ext(name) in imageExt
    fun isVideo(name: String) = ext(name) in videoExt
    fun isAudio(name: String) = ext(name) in audioExt
    fun isText(name: String) = ext(name) in textExt
    fun isArchive(name: String) = ext(name) in archiveExt
    fun isApk(name: String) = ext(name) == "apk"

    fun isImageMime(mime: String?) = mime?.startsWith("image/") == true
    fun isVideoMime(mime: String?) = mime?.startsWith("video/") == true
    fun isAudioMime(mime: String?) = mime?.startsWith("audio/") == true

    fun iconFor(name: String, isDir: Boolean): ImageVector = when {
        isDir -> Icons.Rounded.Folder
        isApk(name) -> Icons.Rounded.Android
        ext(name) == "pdf" -> Icons.Rounded.PictureAsPdf
        isImage(name) -> Icons.Rounded.Image
        isVideo(name) -> Icons.Rounded.Movie
        isAudio(name) -> Icons.Rounded.AudioFile
        isArchive(name) -> if (ext(name) == "zip" || ext(name) == "7z" || ext(name) == "rar") Icons.Rounded.FolderZip else Icons.Rounded.Archive
        ext(name) in setOf("xls", "xlsx", "csv") -> Icons.Rounded.TableChart
        isText(name) -> if (ext(name) in setOf("kt", "java", "py", "js", "ts", "c", "cpp", "h", "go", "rs", "sh", "css", "html", "sql")) Icons.Rounded.Code else Icons.Rounded.TextSnippet
        ext(name) in setOf("doc", "docx", "rtf") -> Icons.Rounded.Description
        else -> Icons.Rounded.InsertDriveFile
    }

    fun tintFor(name: String, isDir: Boolean): Color = when {
        isDir -> Color(0xFFFFB300)
        isApk(name) -> Color(0xFF3DDC84)
        isImage(name) -> Color(0xFF8E7CFF)
        isVideo(name) -> Color(0xFFFF6B6B)
        isAudio(name) -> Color(0xFFFF9F43)
        isArchive(name) -> Color(0xFF00B8D4)
        isText(name) -> Color(0xFF4C97FF)
        else -> Color(0xFF9E9E9E)
    }

    fun mimeOf(name: String): String = when (ext(name)) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "mp4" -> "video/mp4"
        "mkv" -> "video/x-matroska"
        "mp3" -> "audio/mpeg"
        "flac" -> "audio/flac"
        "wav" -> "audio/wav"
        "pdf" -> "application/pdf"
        "apk" -> "application/vnd.android.package-archive"
        "zip" -> "application/zip"
        "txt", "md", "log" -> "text/plain"
        "json" -> "application/json"
        "html", "htm" -> "text/html"
        else -> "*/*"
    }

    fun humanCompare(a: String, b: String): Int {
        var i = 0; var j = 0
        while (i < a.length && j < b.length) {
            val ca = a[i]; val cb = b[j]
            if (ca.isDigit() && cb.isDigit()) {
                var ni = i; var nj = j
                while (ni < a.length && a[ni].isDigit()) ni++
                while (nj < b.length && b[nj].isDigit()) nj++
                val na = a.substring(i, ni).trimStart('0').ifEmpty { "0" }
                val nb = b.substring(j, nj).trimStart('0').ifEmpty { "0" }
                if (na.length != nb.length) return na.length - nb.length
                val c = na.compareTo(nb)
                if (c != 0) return c
                i = ni; j = nj
            } else {
                val c = ca.lowercaseChar().compareTo(cb.lowercaseChar())
                if (c != 0) return c
                i++; j++
            }
        }
        return (a.length - i) - (b.length - j)
    }

    fun sha256(f: File): String = digest(f, "SHA-256")
    fun md5(f: File): String = digest(f, "MD5")

    private fun digest(f: File, algo: String): String {
        val md = MessageDigest.getInstance(algo)
        f.inputStream().use { input ->
            val buf = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buf)
                if (n <= 0) break
                md.update(buf, 0, n)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    fun safeName(name: String): String =
        name.replace(Regex("[\\\\/:*?\"<>|\\n\\r]"), "_").trim().ifEmpty { "未命名" }

    fun uniqueFile(dir: File, name: String): File {
        var f = File(dir, name)
        if (!f.exists()) return f
        val base = name.substringBeforeLast('.', name)
        val suffix = if (name.contains('.')) "." + name.substringAfterLast('.') else ""
        var i = 1
        while (f.exists()) {
            f = File(dir, "$base ($i)$suffix")
            i++
        }
        return f
    }
}
