package com.oxbrowser.app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class HistoryActivity : AppCompatActivity() {

    private lateinit var historyStore: HistoryStore
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        historyStore = HistoryStore(this)
        listView = findViewById(R.id.historyList)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        findViewById<android.widget.Button>(R.id.btnClearHistory).setOnClickListener {
            historyStore.clear()
            refreshList()
        }

        refreshList()
    }

    private fun refreshList() {
        val entries = historyStore.getAll()
        val labels = entries.map { it.title.ifBlank { it.url } }
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)

        listView.setOnItemClickListener { _, _, position, _ ->
            val url = entries[position].url
            val result = Intent()
            result.putExtra("open_url", url)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}
