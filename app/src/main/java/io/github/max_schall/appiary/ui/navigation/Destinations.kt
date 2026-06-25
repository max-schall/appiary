package io.github.max_schall.appiary.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Hexagon
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The five top-level destinations shown in the bottom bar / navigation rail.
 * Routes are plain strings for the v1 NavHost; typed routes can come later.
 */
enum class TopDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Today("today", "Today", Icons.Outlined.Today),
    Apiaries("apiaries", "Apiaries", Icons.Outlined.Grass),
    Hives("hives", "Hives", Icons.Outlined.Hexagon),
    Tasks("tasks", "Tasks", Icons.AutoMirrored.Outlined.Assignment),
    Settings("settings", "Settings", Icons.Outlined.Settings);

    companion object {
        fun fromRoute(route: String?): TopDestination? =
            entries.firstOrNull { it.route == route?.substringBefore('/') }
    }
}

/** Quick-add actions surfaced from the central FAB. */
enum class QuickAddAction(val label: String) {
    Inspection("Inspection"),
    Feeding("Feeding"),
    MiteCheck("Mite check"),
    Treatment("Treatment"),
    Harvest("Harvest"),
    Note("Note / photo"),
}
