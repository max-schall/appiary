package io.github.max_schall.appiary.util

import android.content.Context
import org.osmdroid.config.Configuration

/**
 * One-time osmdroid setup. Sets a unique user-agent (OpenStreetMap tile servers
 * reject the default) and keeps the tile cache inside app-private storage so no
 * storage permission is needed. Tiles are cached for offline reuse after first view.
 */
object OsmConfig {
    @Volatile private var done = false

    fun ensure(context: Context) {
        if (done) return
        synchronized(this) {
            if (done) return
            val app = context.applicationContext
            val cfg = Configuration.getInstance()
            cfg.userAgentValue = app.packageName
            cfg.osmdroidBasePath = app.cacheDir
            cfg.osmdroidTileCache = app.cacheDir.resolve("osmdroid-tiles")
            done = true
        }
    }
}
