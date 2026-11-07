package io.github.lingjiuo.filekeeper.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.lingjiuo.filekeeper.data.FileRepo
import io.github.lingjiuo.filekeeper.util.Fmt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest

@Composable
fun FileViewer(file: File, onClose: () -> Unit) {
    val context = LocalContext.current
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 6.dp, end = 6.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) { Icon(Icons.Rounded.ArrowBack, "返回") }
                Column(Modifier.weight(1f)) {
                    Text(file.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(Fmt.size(file.length()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { openWith(context, file) }) { Text("其他应用") }
            }
            Box(Modifier.weight(1f)) {
                when {
                    Fmt.isImage(file.name) -> ImageViewer(file)
                    Fmt.isVideo(file.name) || Fmt.isAudio(file.name) -> MediaPlayer(file)
                    Fmt.isApk(file.name) -> ApkInfo(file)
                    Fmt.ext(file.name) == "pdf" -> PdfViewer(file)
                    Fmt.isText(file.name) || file.name.lowercase().endsWith(".json") -> TextViewer(file)
                    Fmt.isArchive(file.name) -> ArchiveViewer(file)
                    else -> HexViewer(file)
                }
            }
        }
    }
}

@Composable
private fun ImageViewer(file: File) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 6f)
                    if (scale > 1f) {
                        offsetX += pan.x
                        offsetY += pan.y
                    } else {
                        offsetX = 0f; offsetY = 0f
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(file).crossfade(true).build(),
            contentDescription = file.name,
            modifier = Modifier.fillMaxSize().graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun MediaPlayer(file: File) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun TextViewer(file: File) {
    val context = LocalContext.current
    var loaded by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    var charset by remember { mutableStateOf("UTF-8") }
    var dirty by remember { mutableStateOf(false) }

    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            val (t, cs) = FileRepo.readTextSmart(file)
            text = t
            charset = cs
            loaded = true
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("编码 $charset", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = {
                    runCatching { FileRepo.writeText(file, text, charset) }
                        .onSuccess { dirty = false }
                },
                enabled = dirty
            ) { Text("保存") }
        }
        if (!loaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("读取中…") }
        } else {
            androidx.compose.foundation.text.BasicTextField(
                value = text,
                onValueChange = { text = it; dirty = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .background(
                        io.github.lingjiuo.filekeeper.ui.theme.LocalMiui.current.cardAlt,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = io.github.lingjiuo.filekeeper.ui.theme.LocalMiui.current.textPrimary
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(
                    io.github.lingjiuo.filekeeper.ui.theme.LocalMiui.current.accent
                )
            )
        }
    }
}

@Composable
private fun ApkInfo(file: File) {
    val context = LocalContext.current
    var lines by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val flags = PackageManager.GET_PERMISSIONS or
                if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
            val info = pm.getPackageArchiveInfo(file.absolutePath, flags) ?: return@withContext
            val app = info.applicationInfo
            val label = app?.let { pm.getApplicationLabel(it).toString() } ?: info.packageName
            val sigs = if (Build.VERSION.SDK_INT >= 28) {
                info.signingInfo?.apkContentsSigners ?: emptyArray()
            } else {
                @Suppress("DEPRECATION")
                info.signatures ?: emptyArray()
            }
            val sha = sigs.firstOrNull()?.toByteArray()?.let { bytes ->
                MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
            } ?: "—"
            lines = listOf(
                "应用名" to label,
                "包名" to info.packageName,
                "版本" to "${info.versionName} (${info.versionCode})",
                "SDK" to "min ${app?.minSdkVersion ?: "?"} / target ${app?.targetSdkVersion ?: "?"}",
                "架构" to (app?.nativeLibraryDir?.substringAfterLast('/') ?: "—"),
                "签名 SHA-256" to sha,
                "权限" to (info.requestedPermissions?.size?.toString() ?: "0") + " 项"
            )
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        lines.forEach { (k, v) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(k, Modifier.width(100.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(v, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(18.dp))
        io.github.lingjiuo.filekeeper.ui.miui.MiuiButton(
            text = "安装",
            onClick = { installApk(context, file) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PdfViewer(file: File) {
    var pageCount by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            runCatching {
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer -> pageCount = renderer.pageCount }
                }
            }.onFailure { error = it.message }
        }
    }
    if (error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("无法打开 PDF：${error}")
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(pageCount) { index -> PdfPage(file, index) }
    }
}

@Composable
private fun PdfPage(file: File, index: Int) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(null, file, index) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        renderer.openPage(index).use { page ->
                            val width = context.resources.displayMetrics.widthPixels
                            val height = (width.toFloat() * page.height / page.width).toInt().coerceAtLeast(1)
                            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            bmp.eraseColor(android.graphics.Color.WHITE)
                            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bmp
                        }
                    }
                }
            }.getOrNull()
        }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "第 ${index + 1} 页",
            modifier = Modifier.fillMaxWidth().padding(6.dp)
        )
    }
}

@Composable
private fun ArchiveViewer(file: File) {
    var entries by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            entries = runCatching {
                io.github.lingjiuo.filekeeper.data.ArchiveRepo.list(file).entries.map {
                    (if (it.isDir) "📁 " else "📄 ") + it.name + "   " + Fmt.size(it.size)
                }
            }.getOrDefault(listOf("无法读取该压缩包"))
        }
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        items(entries) { e ->
            Text(e, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 5.dp))
        }
    }
}

@Composable
private fun HexViewer(file: File) {
    var page by remember { mutableIntStateOf(0) }
    var rows by remember { mutableStateOf<List<String>>(emptyList()) }
    val pageSize = 16 * 1024
    LaunchedEffect(file, page) {
        withContext(Dispatchers.IO) {
            RandomAccessFile(file, "r").use { raf ->
                raf.seek(page.toLong() * pageSize)
                val buf = ByteArray(pageSize)
                val n = raf.read(buf)
                val count = if (n < 0) 0 else n
                val list = mutableListOf<String>()
                var i = 0
                while (i < count) {
                    val end = minOf(i + 16, count)
                    val bytes = buf.copyOfRange(i, end)
                    val hex = bytes.joinToString(" ") { "%02x".format(it) }
                    val ascii = bytes.joinToString("") { b ->
                        val c = b.toInt().toChar()
                        if (c.code in 32..126) c.toString() else "."
                    }
                    list += "%08x  %-47s  %s".format(page.toLong() * pageSize + i, hex, ascii)
                    i += 16
                }
                rows = list
            }
        }
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { if (page > 0) page-- }) { Text("上一页") }
            Text("页 ${page + 1}", Modifier.weight(1f))
            TextButton(onClick = { page++ }) { Text("下一页") }
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(rows) { r ->
                Text(r, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp))
            }
        }
    }
}

fun openWith(context: android.content.Context, file: File) {
    runCatching {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, Fmt.mimeOf(file.name))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "打开方式"))
    }
}

fun installApk(context: android.content.Context, file: File) {
    runCatching {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}
