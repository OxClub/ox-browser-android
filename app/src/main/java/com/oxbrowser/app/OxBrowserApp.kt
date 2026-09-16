package com.oxbrowser.app

import android.app.Application

class OxBrowserApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SettingsStore(this).applyTheme()
    }
}
