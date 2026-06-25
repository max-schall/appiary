package io.github.max_schall.appiary.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.ui.theme.AppiaryStatusColors
import io.github.max_schall.appiary.ui.theme.statusColors
import androidx.compose.material3.MaterialTheme

/** Foreground/background pair for an urgency bucket (theme-aware). */
data class BucketColors(val container: Color, val onContainer: Color)

@Composable
fun bucketColors(bucket: UrgencyBucket): BucketColors {
    val s: AppiaryStatusColors = MaterialTheme.statusColors
    return when (bucket) {
        UrgencyBucket.DO_NOW -> BucketColors(s.doNow, s.onDoNow)
        UrgencyBucket.DUE_SOON -> BucketColors(s.dueSoon, s.onDueSoon)
        UrgencyBucket.WATCHLIST -> BucketColors(s.watchlist, s.onWatchlist)
        UrgencyBucket.HEALTHY -> BucketColors(s.healthy, s.onHealthy)
    }
}

/** Icon paired with each bucket — status is never conveyed by color alone. */
fun bucketIcon(bucket: UrgencyBucket): ImageVector = when (bucket) {
    UrgencyBucket.DO_NOW -> Icons.Filled.PriorityHigh
    UrgencyBucket.DUE_SOON -> Icons.Outlined.Schedule
    UrgencyBucket.WATCHLIST -> Icons.Outlined.Visibility
    UrgencyBucket.HEALTHY -> Icons.Outlined.CheckCircle
}

fun categoryIcon(category: RecommendationCategory): ImageVector = when (category) {
    RecommendationCategory.INSPECTION -> Icons.Outlined.Search
    RecommendationCategory.QUEEN -> Icons.Outlined.Star
    RecommendationCategory.SWARM -> Icons.Outlined.Hub
    RecommendationCategory.FEEDING -> Icons.Filled.WaterDrop
    RecommendationCategory.MITE_CHECK -> Icons.Outlined.BugReport
    RecommendationCategory.TREATMENT -> Icons.Outlined.Medication
    RecommendationCategory.COLONY_HEALTH -> Icons.Outlined.MonitorHeart
    RecommendationCategory.HARVEST -> Icons.Outlined.Inventory2
    RecommendationCategory.MANUAL -> Icons.AutoMirrored.Outlined.Assignment
    RecommendationCategory.SEASONAL -> Icons.Outlined.CalendarMonth
    RecommendationCategory.NECTAR_FLOW -> Icons.Outlined.LocalFlorist
    RecommendationCategory.WEATHER -> Icons.Outlined.WbSunny
    RecommendationCategory.COMPLIANCE -> Icons.Outlined.ReceiptLong
}
