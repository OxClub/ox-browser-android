package com.oxbrowser.app

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("ox_settings", Context.MODE_PRIVATE)

    companion object {
        const val ENGINE_GOOGLE = "google"
        const val ENGINE_BING = "bing"
        const val ENGINE_DUCKDUCKGO = "duckduckgo"

        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"
    }

    var homepage: String
        get() = prefs.getString("homepage", "https://www.google.com") ?: "https://www.google.com"
        set(value) = prefs.edit().putString("homepage", value).apply()

    var searchEngine: String
        get() = prefs.getString("search_engine", ENGINE_GOOGLE) ?: ENGINE_GOOGLE
        set(value) = prefs.edit().putString("search_engine", value).apply()

    var theme: String
        get() = prefs.getString("theme", THEME_SYSTEM) ?: THEME_SYSTEM
        set(value) = prefs.edit().putString("theme", value).apply()

    fun searchUrlFor(query: String): String {
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        return when (searchEngine) {
            ENGINE_BING -> "https://www.bing.com/search?q=$encoded"
            ENGINE_DUCKDUCKGO -> "https://duckduckgo.com/?q=$encoded"
            else -> "https://www.google.com/search?q=$encoded"
        }
    }

    fun applyTheme() {
        val mode = when (theme) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
