package io.github.lingjiuo.filekeeper

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.lingjiuo.filekeeper.model.Section
import io.github.lingjiuo.filekeeper.ui.AppState
import io.github.lingjiuo.filekeeper.ui.screens.FavoritesPage
import io.github.lingjiuo.filekeeper.ui.screens.FilePage
import io.github.lingjiuo.filekeeper.ui.screens.FileViewer
import io.github.lingjiuo.filekeeper.ui.screens.SearchPage
import io.github.lingjiuo.filekeeper.ui.screens.SettingsPage
import io.github.lingjiuo.filekeeper.ui.screens.StoragePage
import io.github.lingjiuo.filekeeper.ui.theme.FileKeeperTheme
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Store
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }
}

@Composable
fun App() {
    val context = LocalContext.current
    val state = remember { AppState(context) }
    var viewer by remember { mutableStateOf<File?>(null) }
    var showPerm by remember { mutableStateOf(false) }

    val dark = when (state.darkMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
            showPerm = true
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            state.message = null
        }
    }

    FileKeeperTheme(dark = dark, dynamic = state.dynamicColor) {
        Box(Modifier.fillMaxSize().background(MiuixTheme.colorScheme.background)) {
            when (state.section) {
                Section.FILES -> FilePage(state) { file -> viewer = file }
                Section.FAVORITES -> FavoritesPage(state)
                Section.SEARCH -> SearchPage(state)
                Section.STORAGE -> StoragePage(state)
                Section.SETTINGS -> SettingsPage(state)
            }

            FloatingNavigationBar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)
            ) {
                FloatingNavigationBarItem(
                    selected = state.section == Section.FILES,
                    onClick = { state.section = Section.FILES },
                    icon = MiuixIcons.Folder,
                    label = "文件"
                )
                FloatingNavigationBarItem(
                    selected = state.section == Section.FAVORITES,
                    onClick = { state.section = Section.FAVORITES },
                    icon = MiuixIcons.Favorites,
                    label = "收藏"
                )
                FloatingNavigationBarItem(
                    selected = state.section == Section.SEARCH,
                    onClick = { state.section = Section.SEARCH },
                    icon = MiuixIcons.Search,
                    label = "搜索"
                )
                FloatingNavigationBarItem(
                    selected = state.section == Section.STORAGE,
                    onClick = { state.section = Section.STORAGE },
                    icon = MiuixIcons.Store,
                    label = "存储"
                )
                FloatingNavigationBarItem(
                    selected = state.section == Section.SETTINGS,
                    onClick = { state.section = Section.SETTINGS },
                    icon = MiuixIcons.Settings,
                    label = "设置"
                )
            }

            viewer?.let { FileViewer(it) { viewer = null } }

            if (showPerm) {
                SuperDialog(
                    show = true,
                    title = "需要文件访问权限",
                    onDismissRequest = { showPerm = false }
                ) {
                    androidx.compose.foundation.layout.Column {
                        Text("为了浏览和管理手机全部文件，请授予「所有文件访问权限」。")
                        androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
                        TextButton(
                            text = "去授权",
                            onClick = {
                                showPerm = false
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    BackHandler(enabled = viewer != null || state.selection.isNotEmpty() || state.section != Section.FILES) {
        when {
            viewer != null -> viewer = null
            state.selection.isNotEmpty() -> state.selection = emptySet()
            else -> state.section = Section.FILES
        }
    }
}
