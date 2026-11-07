package io.github.lingjiuo.filekeeper

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import io.github.lingjiuo.filekeeper.ui.components.FloatingDock
import io.github.lingjiuo.filekeeper.ui.screens.FavoritesScreen
import io.github.lingjiuo.filekeeper.ui.screens.FileViewer
import io.github.lingjiuo.filekeeper.ui.screens.FilesScreen
import io.github.lingjiuo.filekeeper.ui.screens.SearchScreen
import io.github.lingjiuo.filekeeper.ui.screens.SettingsScreen
import io.github.lingjiuo.filekeeper.ui.screens.StorageScreen
import io.github.lingjiuo.filekeeper.ui.theme.FileKeeperTheme
import java.io.File

class MainActivity : androidx.activity.ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
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
    var themeKey by remember { mutableStateOf(0) }
    var permissionAsked by remember { mutableStateOf(false) }
    var showPermDialog by remember { mutableStateOf(false) }

    val dark = when (state.darkMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
            showPermDialog = true
        } else if (Build.VERSION.SDK_INT < 30) {
            // 低版本申请常规存储权限
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            state.message = null
        }
    }

    FileKeeperTheme(dark = dark, dynamic = state.dynamicColor) {
        Box(Modifier.fillMaxSize().background(io.github.lingjiuo.filekeeper.ui.theme.LocalMiui.current.background)) {
            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                when (state.section) {
                    Section.FILES -> FilesScreen(state) { file -> viewer = file }
                    Section.FAVORITES -> FavoritesScreen(state)
                    Section.SEARCH -> SearchScreen(state)
                    Section.STORAGE -> StorageScreen(state)
                    Section.SETTINGS -> SettingsScreen(state) { themeKey++ }
                }
            }
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 10.dp)
            ) {
                FloatingDock(state.section) {
                    state.section = it
                    state.selection = emptySet()
                }
            }
            viewer?.let { file ->
                FileViewer(file) { viewer = null }
            }
            if (showPermDialog) {
                io.github.lingjiuo.filekeeper.ui.miui.MiuiDialog(
                    title = "需要文件访问权限",
                    onDismiss = { showPermDialog = false },
                    confirmText = "去授权",
                    onConfirm = {
                        showPermDialog = false
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                        }
                    }
                ) {
                    Text("为了浏览和管理手机全部文件，请授予「所有文件访问权限」。")
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
    val parentDir = state.currentDir.parentFile
    BackHandler(
        enabled = viewer == null && state.selection.isEmpty() && state.section == Section.FILES &&
            parentDir != null && state.currentDir.absolutePath != "/"
    ) {
        state.navigate(parentDir ?: state.currentDir)
    }
}
