package com.oxbrowser.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class DownloadEntry(val fileName: String, val url: String, val timestamp: Long)

class DownloadStore(context: Context) {
    private val prefs = context.getSharedPreferences("ox_downloads", Context.MODE_PRIVATE)

    fun getAll(): List<DownloadEntry> {
        val raw = prefs.getString("list", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val result = mutableListOf<DownloadEntry>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            result.add(DownloadEntry(obj.getString("name"), obj.getString("url"), obj.getLong("ts")))
        }
        return result.sortedByDescending { it.timestamp }
    }

    fun add(fileName: String, url: String) {
        val current = getAll().toMutableList()
        current.add(0, DownloadEntry(fileName, url, System.currentTimeMillis()))
        save(current)
    }

    fun clear() {
        prefs.edit().putString("list", "[]").apply()
    }

    private fun save(list: List<DownloadEntry>) {
        val arr = JSONArray()
        list.forEach {
            val obj = JSONObject()
            obj.put("name", it.fileName)
            obj.put("url", it.url)
            obj.put("ts", it.timestamp)
            arr.put(obj)
        }
        prefs.edit().putString("list", arr.toString()).apply()
    }
}
