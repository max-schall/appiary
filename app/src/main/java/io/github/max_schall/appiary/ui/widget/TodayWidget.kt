package io.github.max_schall.appiary.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.github.max_schall.appiary.AppiaryApplication
import io.github.max_schall.appiary.MainActivity
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.data.repository.WidgetSnapshot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * "Today" home-screen widget: a glanceable count of urgent tasks plus the single
 * top recommendation, tapping through to the app. Reads the local DB directly on
 * each update — local-first, no network. Refreshed by [TodayWidget.refresh].
 */
class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as AppiaryApplication).container
        val snapshot = container.recommendationRepository.widgetSnapshot()
        provideContent {
            GlanceTheme {
                WidgetContent(context, snapshot)
            }
        }
    }

    companion object {
        /** Push fresh data to all placed widgets (call after data changes). */
        suspend fun refresh(context: Context) = TodayWidget().updateAll(context)
    }
}

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}

@Composable
private fun WidgetContent(context: Context, snapshot: WidgetSnapshot) {
    val onSurface = GlanceTheme.colors.onSurface
    val primary = GlanceTheme.colors.primary
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(16.dp)
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                context.getString(R.string.app_name),
                style = TextStyle(color = onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium),
            )
        }
        Spacer(GlanceModifier.height(6.dp))

        if (snapshot.doNow == 0 && snapshot.dueSoon == 0) {
            Text(
                context.getString(R.string.widget_all_clear),
                style = TextStyle(color = onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold),
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    snapshot.doNow.toString(),
                    style = TextStyle(color = primary, fontSize = 34.sp, fontWeight = FontWeight.Bold),
                )
                Spacer(GlanceModifier.height(1.dp))
                Text(
                    "  " + context.getString(R.string.bucket_do_now),
                    style = TextStyle(color = onSurface, fontSize = 13.sp),
                )
            }
            if (snapshot.dueSoon > 0) {
                Text(
                    context.getString(R.string.widget_due_soon_count, snapshot.dueSoon),
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp),
                )
            }
            snapshot.top?.let { top ->
                Spacer(GlanceModifier.height(6.dp))
                Text(
                    top.title,
                    maxLines = 2,
                    style = TextStyle(color = onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                )
                Text(
                    top.shortReason,
                    maxLines = 1,
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp),
                )
            }
        }
    }
}
