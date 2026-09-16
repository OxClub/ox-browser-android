package com.oxbrowser.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Bookmark(val title: String, val url: String)

class BookmarkStore(context: Context) {
    private val prefs = context.getSharedPreferences("ox_bookmarks", Context.MODE_PRIVATE)

    fun getAll(): List<Bookmark> {
        val raw = prefs.getString("list", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val result = mutableListOf<Bookmark>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            result.add(Bookmark(obj.getString("title"), obj.getString("url")))
        }
        return result
    }

    fun isBookmarked(url: String): Boolean = getAll().any { it.url == url }

    fun toggle(title: String, url: String) {
        val current = getAll().toMutableList()
        val existing = current.indexOfFirst { it.url == url }
        if (existing >= 0) {
            current.removeAt(existing)
        } else {
            current.add(Bookmark(title, url))
        }
        save(current)
    }

    fun remove(url: String) {
        save(getAll().filter { it.url != url })
    }

    private fun save(list: List<Bookmark>) {
        val arr = JSONArray()
        list.forEach {
            val obj = JSONObject()
            obj.put("title", it.title)
            obj.put("url", it.url)
            arr.put(obj)
        }
        prefs.edit().putString("list", arr.toString()).apply()
    }
}
