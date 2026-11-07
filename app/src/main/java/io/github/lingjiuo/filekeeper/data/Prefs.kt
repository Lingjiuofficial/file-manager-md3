package io.github.lingjiuo.filekeeper.data

import android.content.Context
import android.os.Environment
import io.github.lingjiuo.filekeeper.model.SortMode
import io.github.lingjiuo.filekeeper.model.ViewMode
import java.io.File

/** 轻量设置存储：SharedPreferences，读写都在主线程，量很小。 */
class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var darkMode: Int
        get() = sp.getInt("dark", 0)
        set(v) = sp.edit().putInt("dark", v).apply()

    var viewMode: ViewMode
        get() = if (sp.getString("view", "list") == "grid") ViewMode.GRID else ViewMode.LIST
        set(v) = sp.edit().putString("view", if (v == ViewMode.GRID) "grid" else "list").apply()

    var sort: SortMode
        get() = runCatching { SortMode.valueOf(sp.getString("sort", "NAME")!!) }.getOrDefault(SortMode.NAME)
        set(v) = sp.edit().putString("sort", v.name).apply()

    var ascending: Boolean
        get() = sp.getBoolean("asc", true)
        set(v) = sp.edit().putBoolean("asc", v).apply()

    var dynamicColor: Boolean
        get() = sp.getBoolean("dynamic", true)
        set(v) = sp.edit().putBoolean("dynamic", v).apply()

    var showHidden: Boolean
        get() = sp.getBoolean("hidden", false)
        set(v) = sp.edit().putBoolean("hidden", v).apply()

    var lastPath: String
        get() = sp.getString("path", defaultRoot())!!
        set(v) = sp.edit().putString("path", v).apply()

    var recent: List<String>
        get() = sp.getString("recent", "")!!.split('\n').filter { it.isNotBlank() }
        set(v) = sp.edit().putString("recent", v.take(20).joinToString("\n")).apply()

    var favorites: List<String>
        get() = sp.getString("fav", "")!!.split('\n').filter { it.isNotBlank() }
        set(v) = sp.edit().putString("fav", v.joinToString("\n")).apply()

    fun addRecent(path: String) {
        recent = (listOf(path) + recent.filter { it != path }).take(20)
    }

    companion object {
        fun defaultRoot(): String {
            val f = Environment.getExternalStorageDirectory()
            return if (f != null && f.exists()) f.absolutePath else "/"
        }
    }
}

fun File.isRootDir(): Boolean = absolutePath == "/" || absolutePath == Prefs.defaultRoot()
