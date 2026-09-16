package com.oxbrowser.app

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var tabStrip: LinearLayout
    private lateinit var webviewContainer: FrameLayout
    private lateinit var urlBar: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnReload: ImageButton
    private lateinit var btnBookmark: ImageButton
    private lateinit var btnMenu: ImageButton
    private lateinit var btnNewTab: ImageButton

    private lateinit var bookmarkStore: BookmarkStore
    private lateinit var historyStore: HistoryStore
    private lateinit var downloadStore: DownloadStore
    private lateinit var settings: SettingsStore

    private val tabs = mutableListOf<BrowserTab>()
    private var activeTabId: Long = -1
    private var tabCounter = 0L

    private val historyLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val url = result.data?.getStringExtra("open_url")
        if (url != null) navigate(url)
    }

    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Theme may have changed; recreate to apply new day/night colors immediately.
        recreate()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bookmarkStore = BookmarkStore(this)
        historyStore = HistoryStore(this)
        downloadStore = DownloadStore(this)
        settings = SettingsStore(this)

        tabStrip = findViewById(R.id.tabStrip)
        webviewContainer = findViewById(R.id.webviewContainer)
        urlBar = findViewById(R.id.urlBar)
        progressBar = findViewById(R.id.progressBar)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        btnBack = findViewById(R.id.btnBack)
        btnForward = findViewById(R.id.btnForward)
        btnReload = findViewById(R.id.btnReload)
        btnBookmark = findViewById(R.id.btnBookmark)
        btnMenu = findViewById(R.id.btnMenu)
        btnNewTab = findViewById(R.id.btnNewTab)

        setupToolbar()
        createTab(settings.homepage, isPrivate = false)
    }

    private fun setupToolbar() {
        btnBack.setOnClickListener {
            activeWebView()?.let { if (it.canGoBack()) it.goBack() }
        }
        btnForward.setOnClickListener {
            activeWebView()?.let { if (it.canGoForward()) it.goForward() }
        }
        btnReload.setOnClickListener {
            activeWebView()?.reload()
        }
        btnNewTab.setOnClickListener {
            createTab(settings.homepage, isPrivate = false)
        }
        btnBookmark.setOnClickListener {
            val tab = activeTab() ?: return@setOnClickListener
            bookmarkStore.toggle(tab.title, tab.url)
            updateBookmarkIcon()
        }
        btnMenu.setOnClickListener { showMainMenu(it) }

        swipeRefresh.setOnRefreshListener {
            activeWebView()?.reload()
        }

        urlBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                navigate(urlBar.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun showMainMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, "New tab")
        popup.menu.add(0, 2, 1, "New private tab")
        popup.menu.add(0, 3, 2, "Bookmarks")
        popup.menu.add(0, 4, 3, "History")
        popup.menu.add(0, 5, 4, "Downloads")
        popup.menu.add(0, 6, 5, "Settings")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> createTab(settings.homepage, isPrivate = false)
                2 -> createTab(settings.homepage, isPrivate = true)
                3 -> showBookmarksDialog()
                4 -> historyLauncher.launch(android.content.Intent(this, HistoryActivity::class.java))
                5 -> startActivity(android.content.Intent(this, DownloadsActivity::class.java))
                6 -> settingsLauncher.launch(android.content.Intent(this, SettingsActivity::class.java))
            }
            true
        }
        popup.show()
    }

    private fun normalizeInput(raw: String): String {
        val value = raw.trim()
        if (value.isEmpty()) return settings.homepage

        return when {
            value.startsWith("http://") || value.startsWith("https://") -> value
            Patterns.WEB_URL.matcher(value).matches() && !value.contains(" ") -> "https://$value"
            else -> settings.searchUrlFor(value)
        }
    }

    private fun navigate(input: String) {
        val url = normalizeInput(input)
        activeWebView()?.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createTab(url: String, isPrivate: Boolean) {
        val id = ++tabCounter

        val webView = WebView(this)
        webView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = !isPrivate
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

        if (isPrivate) {
            webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
            CookieManager.getInstance().setAcceptCookie(false)
            webView.clearCache(true)
            webView.clearHistory()
        } else {
            CookieManager.getInstance().setAcceptCookie(true)
        }

        val tab = BrowserTab(id = id, webView = webView, url = url, isPrivate = isPrivate)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, finishedUrl: String) {
                super.onPageFinished(view, finishedUrl)
                tab.url = finishedUrl
                if (!tab.isPrivate) {
                    historyStore.add(tab.title, finishedUrl)
                }
                if (id == activeTabId) {
                    urlBar.setText(finishedUrl)
                    updateNavButtons()
                    updateBookmarkIcon()
                }
                swipeRefresh.isRefreshing = false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (id == activeTabId) {
                    progressBar.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
                    progressBar.progress = newProgress
                }
            }

            override fun onReceivedTitle(view: WebView, title: String?) {
                super.onReceivedTitle(view, title)
                tab.title = title ?: tab.url
                renderTabStrip()
            }
        }

        webView.setDownloadListener { downloadUrl, userAgent, contentDisposition, mimeType, _ ->
            startDownload(downloadUrl, userAgent, contentDisposition, mimeType)
        }

        webviewContainer.addView(webView)
        tabs.add(tab)
        webView.loadUrl(url)

        renderTabStrip()
        activateTab(id)
    }

    private fun startDownload(url: String, userAgent: String, contentDisposition: String, mimeType: String) {
        try {
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimeType)
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file")
            request.setTitle(fileName)
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            downloadStore.add(fileName, url)
            Toast.makeText(this, "Downloading $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Download failed to start", Toast.LENGTH_SHORT).show()
        }
    }

    private fun closeTab(id: Long) {
        val idx = tabs.indexOfFirst { it.id == id }
        if (idx == -1) return

        val tab = tabs[idx]
        webviewContainer.removeView(tab.webView)
        tab.webView.destroy()
        tabs.removeAt(idx)

        if (tabs.isEmpty()) {
            createTab(settings.homepage, isPrivate = false)
            return
        }

        if (activeTabId == id) {
            val next = tabs.getOrNull(idx) ?: tabs.getOrNull(idx - 1)
            next?.let { activateTab(it.id) }
        }

        renderTabStrip()
    }

    private fun activateTab(id: Long) {
        activeTabId = id
        tabs.forEach { it.webView.visibility = if (it.id == id) View.VISIBLE else View.GONE }

        activeTab()?.let {
            urlBar.setText(it.url)
        }

        updateNavButtons()
        updateBookmarkIcon()
        renderTabStrip()
    }

    private fun activeTab(): BrowserTab? = tabs.find { it.id == activeTabId }
    private fun activeWebView(): WebView? = activeTab()?.webView

    private fun updateNavButtons() {
        val wv = activeWebView()
        btnBack.alpha = if (wv?.canGoBack() == true) 1.0f else 0.4f
        btnForward.alpha = if (wv?.canGoForward() == true) 1.0f else 0.4f
    }

    private fun updateBookmarkIcon() {
        val tab = activeTab() ?: return
        val bookmarked = bookmarkStore.isBookmarked(tab.url)
        btnBookmark.alpha = if (bookmarked) 1.0f else 0.6f
    }

    private fun renderTabStrip() {
        tabStrip.removeAllViews()
        val inflater = LayoutInflater.from(this)

        tabs.forEach { tab ->
            val chip = inflater.inflate(R.layout.tab_item, tabStrip, false)
            val titleView = chip.findViewById<TextView>(R.id.tabTitle)
            val closeView = chip.findViewById<TextView>(R.id.tabClose)

            val label = tab.title.ifBlank { "New Tab" }
            titleView.text = if (tab.isPrivate) "\uD83D\uDD75 $label" else label
            chip.alpha = if (tab.id == activeTabId) 1.0f else 0.6f

            if (tab.isPrivate) {
                chip.setBackgroundResource(R.drawable.tab_chip_private_background)
            } else {
                chip.setBackgroundResource(R.drawable.tab_chip_background)
            }

            chip.setOnClickListener { activateTab(tab.id) }
            closeView.setOnClickListener { closeTab(tab.id) }

            tabStrip.addView(chip)
        }
    }

    private fun showBookmarksDialog() {
        val bookmarks = bookmarkStore.getAll()
        val titles = if (bookmarks.isEmpty()) {
            arrayOf("No bookmarks yet")
        } else {
            bookmarks.map { it.title.ifBlank { it.url } }.toTypedArray()
        }

        AlertDialog.Builder(this)
            .setTitle("Bookmarks")
            .setItems(titles) { _, which ->
                if (bookmarks.isNotEmpty()) {
                    navigate(bookmarks[which].url)
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    override fun onBackPressed() {
        val wv = activeWebView()
        if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
