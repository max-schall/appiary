package io.github.max_schall.appiary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Moss,
    onPrimary = Paper,
    primaryContainer = SageContainer,
    onPrimaryContainer = MossDark,
    secondary = Sage,
    onSecondary = Color.White,
    secondaryContainer = SageContainerLight,
    onSecondaryContainer = SageDark,
    tertiary = HoneyDeep,
    onTertiary = Color.White,
    tertiaryContainer = HoneyContainer,
    onTertiaryContainer = HoneyOnContainer,
    background = Paper,
    onBackground = Ink,
    surface = PaperSurface,
    onSurface = Ink,
    surfaceVariant = Stone,
    onSurfaceVariant = InkMuted,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3EFE4),
    surfaceContainer = Color(0xFFEDE9DC),
    surfaceContainerHigh = Color(0xFFE7E3D5),
    surfaceContainerHighest = Color(0xFFE1DDCE),
    outline = Color(0xFF797A6E),
    outlineVariant = StoneDeep,
    error = Berry,
    onError = Color.White,
    errorContainer = BerryContainer,
    onErrorContainer = BerryOnContainer,
)

private val DarkColors = darkColorScheme(
    primary = MossLight,
    onPrimary = MossDark,
    primaryContainer = Moss,
    onPrimaryContainer = SageContainer,
    secondary = Color(0xFFBFCBA9),
    onSecondary = SageDark,
    secondaryContainer = Color(0xFF3C4632),
    onSecondaryContainer = SageContainerLight,
    tertiary = Honey,
    onTertiary = Color(0xFF3A2A08),
    tertiaryContainer = HoneyDeep,
    onTertiaryContainer = HoneyContainer,
    background = NightBg,
    onBackground = Chalk,
    surface = NightSurface,
    onSurface = Chalk,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = ChalkMuted,
    surfaceContainerLowest = Color(0xFF100F0A),
    surfaceContainerLow = Color(0xFF1B1A14),
    surfaceContainer = Color(0xFF1F1E17),
    surfaceContainerHigh = Color(0xFF2A2920),
    surfaceContainerHighest = Color(0xFF35342A),
    outline = Color(0xFF918F80),
    outlineVariant = Color(0xFF45463B),
    error = Color(0xFFE49494),
    onError = Color(0xFF3A0E0E),
    errorContainer = Color(0xFF5C2A2A),
    onErrorContainer = BerryContainer,
)

/**
 * Extra brand colors that have no Material role. Carried via CompositionLocal so
 * the four urgency buckets render consistently and adapt to light/dark.
 */
@Immutable
data class AppiaryStatusColors(
    val doNow: Color,
    val onDoNow: Color,
    val dueSoon: Color,
    val onDueSoon: Color,
    val watchlist: Color,
    val onWatchlist: Color,
    val healthy: Color,
    val onHealthy: Color,
)

private val LightStatusColors = AppiaryStatusColors(
    doNow = StatusDoNow, onDoNow = Color.White,
    dueSoon = StatusDueSoon, onDueSoon = Color(0xFF3A2A08),
    watchlist = StatusWatchlist, onWatchlist = Color.White,
    healthy = StatusHealthy, onHealthy = Color.White,
)

private val DarkStatusColors = AppiaryStatusColors(
    doNow = StatusDoNowDark, onDoNow = Color(0xFF3A0E0E),
    dueSoon = StatusDueSoonDark, onDueSoon = Color(0xFF3A2A08),
    watchlist = StatusWatchlistDark, onWatchlist = Color(0xFF2A2710),
    healthy = StatusHealthyDark, onHealthy = Color(0xFF11271D),
)

val LocalAppiaryStatusColors = staticCompositionLocalOf { LightStatusColors }

/** Access brand status colors: `MaterialTheme.statusColors.doNow`. */
val MaterialTheme.statusColors: AppiaryStatusColors
    @Composable @ReadOnlyComposable
    get() = LocalAppiaryStatusColors.current

@Composable
fun AppiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S ->
            if (darkTheme) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
        darkTheme -> DarkColors
        else -> LightColors
    }
    // Brand status colors stay consistent regardless of dynamic color.
    val statusColors = if (darkTheme) DarkStatusColors else LightStatusColors

    CompositionLocalProvider(
        LocalAppiaryStatusColors provides statusColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppiaryTypography,
            shapes = AppiaryShapes,
            content = content,
        )
    }
}
