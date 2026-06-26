package io.github.max_schall.appiary

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import io.github.max_schall.appiary.data.settings.AppPrefs
import io.github.max_schall.appiary.data.settings.ThemeMode
import io.github.max_schall.appiary.nfc.NfcController
import io.github.max_schall.appiary.ui.navigation.AppiaryApp
import io.github.max_schall.appiary.ui.theme.AppiaryTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settings = (application as AppiaryApplication).container.settingsRepository
        handleNfcIntent(intent)
        setContent {
            val prefs by settings.appPrefs.collectAsState(initial = AppPrefs())
            val darkTheme = when (prefs.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            AppiaryTheme(darkTheme = darkTheme, dynamicColor = prefs.dynamicColor) {
                val windowSizeClass = calculateWindowSizeClass(this)
                AppiaryApp(widthSizeClass = windowSizeClass.widthSizeClass)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Enable NFC foreground dispatch only when the user has turned it on.
        lifecycleScope.launch {
            if (NfcController.isAvailable(this@MainActivity) &&
                (application as AppiaryApplication).container.settingsRepository.getAppPrefs().nfcEnabled
            ) {
                NfcController.enableForeground(this@MainActivity)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (NfcController.isAvailable(this)) NfcController.disableForeground(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    /** Resolve a scanned tag to a hive (open it) and remember it for linking. */
    private fun handleNfcIntent(intent: Intent?) {
        val tagId = intent?.let { NfcController.tagIdFrom(it) } ?: return
        NfcController.lastScannedTag.value = tagId
        val container = (application as AppiaryApplication).container
        lifecycleScope.launch {
            container.hiveRepository.getByNfcTag(tagId)?.let {
                NfcController.openHiveRequests.emit(it.id)
            }
        }
    }
}
