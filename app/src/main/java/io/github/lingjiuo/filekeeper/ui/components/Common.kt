@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package io.github.lingjiuo.filekeeper.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lingjiuo.filekeeper.model.FileEntry
import io.github.lingjiuo.filekeeper.model.Section
import io.github.lingjiuo.filekeeper.ui.miui.DockSurface
import io.github.lingjiuo.filekeeper.ui.miui.MiuiIconButton
import io.github.lingjiuo.filekeeper.ui.theme.LocalMiui
import io.github.lingjiuo.filekeeper.util.Fmt

@Composable
fun FloatingDock(current: Section, onSelect: (Section) -> Unit) {
    val palette = LocalMiui.current
    val items = listOf(
        Triple(Section.FILES, Icons.Rounded.Folder, "文件"),
        Triple(Section.FAVORITES, Icons.Rounded.Star, "收藏"),
        Triple(Section.SEARCH, Icons.Rounded.Search, "搜索"),
        Triple(Section.STORAGE, Icons.Rounded.PieChart, "存储"),
        Triple(Section.SETTINGS, Icons.Rounded.Settings, "设置")
    )
    DockSurface(
        Modifier
            .padding(horizontal = 18.dp)
            .fillMaxWidth()
            .height(66.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { (section, icon, label) ->
                val selected = section == current
                val tint by animateColorAsState(
                    if (selected) palette.accent else palette.textSecondary,
                    label = "tint"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .combinedClickable(onClick = { onSelect(section) }, onLongClick = {})
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(if (selected) 50.dp else 38.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                if (selected) palette.accent.copy(alpha = 0.15f) else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, label, tint = tint, modifier = Modifier.size(22.dp))
                    }
                    Text(
                        label,
                        fontSize = 10.sp,
                        color = tint,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun LargeTitle(title: String, subtitle: String? = null, trailing: @Composable () -> Unit = {}) {
    val palette = LocalMiui.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 14.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
            if (subtitle != null) {
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailing()
    }
}

@Composable
fun RoundIconButton(
    icon: ImageVector,
    desc: String,
    onClick: () -> Unit,
    tint: Color = LocalMiui.current.textPrimary
) {
    MiuiIconButton(icon = icon, description = desc, onClick = onClick, tint = tint)
}

@Composable
fun FileRow(
    entry: FileEntry,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val palette = LocalMiui.current
    val bg by animateColorAsState(
        if (selected) palette.accent.copy(alpha = 0.10f) else Color.Transparent,
        label = "rowbg"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Fmt.tintFor(entry.name, entry.isDir).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Fmt.iconFor(entry.name, entry.isDir),
                entry.name,
                tint = Fmt.tintFor(entry.name, entry.isDir),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                entry.name,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = palette.textPrimary
            )
            val sub = if (entry.isDir) {
                if (entry.childCount >= 0) "${entry.childCount} 项 · ${Fmt.date(entry.modified)}"
                else Fmt.date(entry.modified)
            } else {
                "${Fmt.size(entry.size)} · ${Fmt.date(entry.modified)}"
            }
            Text(sub, fontSize = 11.5.sp, color = palette.textSecondary, maxLines = 1)
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(palette.accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Check, "已选择", tint = Color.White, modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
fun FileGridCell(entry: FileEntry, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val palette = LocalMiui.current
    val bg by animateColorAsState(
        if (selected) palette.accent.copy(alpha = 0.12f) else palette.card,
        label = "cellbg"
    )
    Column(
        modifier = Modifier
            .padding(5.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(
                1.dp,
                if (selected) palette.accent.copy(alpha = 0.5f) else palette.divider,
                RoundedCornerShape(20.dp)
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Fmt.tintFor(entry.name, entry.isDir).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Fmt.iconFor(entry.name, entry.isDir),
                entry.name,
                tint = Fmt.tintFor(entry.name, entry.isDir),
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            entry.name,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = palette.textPrimary
        )
        if (!entry.isDir) {
            Text(Fmt.size(entry.size), fontSize = 11.sp, color = palette.textSecondary)
        }
    }
}
