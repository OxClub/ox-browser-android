package com.oxbrowser.app

import android.webkit.WebView

data class BrowserTab(
    val id: Long,
    var webView: WebView,
    var title: String = "New Tab",
    var url: String = "",
    var isPrivate: Boolean = false
)
