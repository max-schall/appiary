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

// Default light theme, built from the app-logo palette: honey-gold skep,
// leaf green, and bark-brown linework on warm cream.
private val LightColors = lightColorScheme(
    primary = LogoHoneyDeep,
    onPrimary = Color.White,
    primaryContainer = LogoHoneyContainer,
    onPrimaryContainer = LogoHoneyOnContainer,
    secondary = LogoLeaf,
    onSecondary = Color.White,
    secondaryContainer = LogoLeafContainer,
    onSecondaryContainer = LogoLeafDark,
    tertiary = LogoBark,
    onTertiary = Color.White,
    tertiaryContainer = LogoBarkContainer,
    onTertiaryContainer = LogoBarkDark,
    background = LogoCream,
    onBackground = LogoInk,
    surface = LogoSurface,
    onSurface = LogoInk,
    surfaceVariant = LogoSurfaceVariant,
    onSurfaceVariant = LogoInkMuted,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F2E4),
    surfaceContainer = Color(0xFFF2ECDC),
    surfaceContainerHigh = Color(0xFFEDE5D3),
    surfaceContainerHighest = Color(0xFFE7DECB),
    outline = LogoOutline,
    outlineVariant = LogoOutlineVariant,
    error = Berry,
    onError = Color.White,
    errorContainer = BerryContainer,
    onErrorContainer = BerryOnContainer,
)

// Dark theme, same logo identity as LightColors: honey-gold skep, leaf green,
// and bark-tan linework lightened for warm near-black surfaces.
private val DarkColors = darkColorScheme(
    primary = LogoHoneyLight,
    onPrimary = LogoHoneyOnDark,
    primaryContainer = LogoHoneyDarkContainer,
    onPrimaryContainer = LogoHoneyOnDarkContainer,
    secondary = LogoLeafLight,
    onSecondary = LogoLeafOnDark,
    secondaryContainer = LogoLeafDarkContainer,
    onSecondaryContainer = LogoLeafOnDarkContainer,
    tertiary = LogoBarkLight,
    onTertiary = LogoBarkOnDark,
    tertiaryContainer = LogoBarkDarkContainer,
    onTertiaryContainer = LogoBarkOnDarkContainer,
    background = LogoNightBg,
    onBackground = LogoChalk,
    surface = LogoNightSurface,
    onSurface = LogoChalk,
    surfaceVariant = LogoNightSurfaceVariant,
    onSurfaceVariant = LogoChalkMuted,
    surfaceContainerLowest = Color(0xFF100D07),
    surfaceContainerLow = Color(0xFF1D1810),
    surfaceContainer = Color(0xFF221C13),
    surfaceContainerHigh = Color(0xFF2D261A),
    surfaceContainerHighest = Color(0xFF383023),
    outline = LogoOutlineDark,
    outlineVariant = LogoOutlineVariantDark,
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
