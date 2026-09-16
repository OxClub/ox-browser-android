package com.oxbrowser.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class HistoryEntry(val title: String, val url: String, val timestamp: Long)

class HistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("ox_history", Context.MODE_PRIVATE)
    private val maxEntries = 500

    fun getAll(): List<HistoryEntry> {
        val raw = prefs.getString("list", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val result = mutableListOf<HistoryEntry>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            result.add(HistoryEntry(obj.getString("title"), obj.getString("url"), obj.getLong("ts")))
        }
        return result.sortedByDescending { it.timestamp }
    }

    fun add(title: String, url: String) {
        if (url.isBlank() || url == "about:blank") return
        val current = getAll().toMutableList()
        current.removeAll { it.url == url }
        current.add(0, HistoryEntry(title.ifBlank { url }, url, System.currentTimeMillis()))
        save(current.take(maxEntries))
    }

    fun clear() {
        prefs.edit().putString("list", "[]").apply()
    }

    private fun save(list: List<HistoryEntry>) {
        val arr = JSONArray()
        list.forEach {
            val obj = JSONObject()
            obj.put("title", it.title)
            obj.put("url", it.url)
            obj.put("ts", it.timestamp)
            arr.put(obj)
        }
        prefs.edit().putString("list", arr.toString()).apply()
    }
}
