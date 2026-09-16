package com.oxbrowser.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var settings: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settings = SettingsStore(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val homepageInput = findViewById<EditText>(R.id.homepageInput)
        val engineGroup = findViewById<RadioGroup>(R.id.searchEngineGroup)
        val themeGroup = findViewById<RadioGroup>(R.id.themeGroup)

        homepageInput.setText(settings.homepage)

        when (settings.searchEngine) {
            SettingsStore.ENGINE_BING -> engineGroup.check(R.id.radioBing)
            SettingsStore.ENGINE_DUCKDUCKGO -> engineGroup.check(R.id.radioDuckDuckGo)
            else -> engineGroup.check(R.id.radioGoogle)
        }

        when (settings.theme) {
            SettingsStore.THEME_LIGHT -> themeGroup.check(R.id.radioThemeLight)
            SettingsStore.THEME_DARK -> themeGroup.check(R.id.radioThemeDark)
            else -> themeGroup.check(R.id.radioThemeSystem)
        }

        findViewById<Button>(R.id.btnSaveSettings).setOnClickListener {
            val homepage = homepageInput.text.toString().trim()
            settings.homepage = if (homepage.isNotEmpty()) {
                if (homepage.startsWith("http")) homepage else "https://$homepage"
            } else {
                "https://www.google.com"
            }

            settings.searchEngine = when (engineGroup.checkedRadioButtonId) {
                R.id.radioBing -> SettingsStore.ENGINE_BING
                R.id.radioDuckDuckGo -> SettingsStore.ENGINE_DUCKDUCKGO
                else -> SettingsStore.ENGINE_GOOGLE
            }

            val previousTheme = settings.theme
            val newTheme = when (themeGroup.checkedRadioButtonId) {
                R.id.radioThemeLight -> SettingsStore.THEME_LIGHT
                R.id.radioThemeDark -> SettingsStore.THEME_DARK
                else -> SettingsStore.THEME_SYSTEM
            }
            settings.theme = newTheme

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()

            if (newTheme != previousTheme) {
                settings.applyTheme()
            }

            setResult(RESULT_OK)
            finish()
        }
    }
}
