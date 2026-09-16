package com.oxbrowser.app

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.text.DateFormat

class DownloadsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloads)

        val store = DownloadStore(this)
        val listView = findViewById<ListView>(R.id.downloadsList)
        val emptyLabel = findViewById<android.widget.TextView>(R.id.emptyLabel)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val entries = store.getAll()
        emptyLabel.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE

        val labels = entries.map {
            val time = DateFormat.getDateTimeInstance().format(it.timestamp)
            "${it.fileName}\n$time"
        }
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)
    }
}
