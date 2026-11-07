@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package io.github.lingjiuo.filekeeper.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import io.github.lingjiuo.filekeeper.util.Fmt

@Composable
fun FloatingDock(current: Section, onSelect: (Section) -> Unit) {
    val items = listOf(
        Triple(Section.FILES, Icons.Rounded.Folder, "文件"),
        Triple(Section.FAVORITES, Icons.Rounded.Star, "收藏"),
        Triple(Section.SEARCH, Icons.Rounded.Search, "搜索"),
        Triple(Section.STORAGE, Icons.Rounded.PieChart, "存储"),
        Triple(Section.SETTINGS, Icons.Rounded.Settings, "设置")
    )
    Surface(
        modifier = Modifier
            .padding(horizontal = 18.dp)
            .height(66.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(33.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 6.dp,
        shadowElevation = 18.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { (section, icon, label) ->
                val selected = section == current
                val tint by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "tint"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .combinedClickable(
                            onClick = { onSelect(section) },
                            onLongClick = {}
                        )
                        .padding(vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(if (selected) 52.dp else 40.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, label, tint = tint, modifier = Modifier.size(23.dp))
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
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 14.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailing()
    }
}

@Composable
fun RoundIconButton(icon: ImageVector, desc: String, onClick: () -> Unit, tint: Color = MaterialTheme.colorScheme.onSurface) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .combinedClickable(onClick = onClick, onLongClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, desc, tint = tint, modifier = Modifier.size(21.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileRow(
    entry: FileEntry,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
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
                .clip(RoundedCornerShape(14.dp))
                .background(Fmt.tintFor(entry.name, entry.isDir).copy(alpha = 0.14f)),
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
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            val sub = if (entry.isDir) {
                if (entry.childCount >= 0) "${entry.childCount} 项 · ${Fmt.date(entry.modified)}"
                else Fmt.date(entry.modified)
            } else {
                "${Fmt.size(entry.size)} · ${Fmt.date(entry.modified)}"
            }
            Text(
                sub,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Check,
                    "已选择",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
fun FileGridCell(entry: FileEntry, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        label = "cellbg"
    )
    Column(
        modifier = Modifier
            .padding(5.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
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
                .background(Fmt.tintFor(entry.name, entry.isDir).copy(alpha = 0.14f)),
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
            style = MaterialTheme.typography.labelLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!entry.isDir) {
            Text(
                Fmt.size(entry.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
