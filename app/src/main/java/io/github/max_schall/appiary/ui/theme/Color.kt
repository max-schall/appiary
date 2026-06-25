package io.github.max_schall.appiary.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Appiary palette — "modern field notebook".
 *
 * Anchors: deep moss green, warm paper, honey amber, muted berry. The goal is a
 * calm, grounded, sunlight-readable surface that reads like a beekeeping journal
 * rather than a generic dashboard. Values are hand-tuned (not dynamic-generated)
 * so the brand stays consistent outdoors and across devices.
 */

// --- Brand greens -----------------------------------------------------------
val Moss = Color(0xFF2E5141)          // deep moss / chalkboard green (primary)
val MossDark = Color(0xFF11271D)
val MossLight = Color(0xFF9CC3A9)
val SageContainer = Color(0xFFC9D8CB)
val Sage = Color(0xFF66785A)          // muted sage / wax green (secondary)
val SageDark = Color(0xFF1E2615)
val SageContainerLight = Color(0xFFDCE3CE)

// --- Honey / amber (warnings, due-soon) -------------------------------------
val Honey = Color(0xFFD9A441)
val HoneyDeep = Color(0xFFB07A2B)
val HoneyContainer = Color(0xFFF4E2BE)
val HoneyOnContainer = Color(0xFF3A2A08)

// --- Paper / stone neutrals -------------------------------------------------
val Paper = Color(0xFFF5F2E9)         // warm off-white background
val PaperSurface = Color(0xFFF8F5EC)
val Stone = Color(0xFFE3E0D2)         // surfaceVariant
val StoneDeep = Color(0xFFC9C7B7)
val Ink = Color(0xFF1F231E)           // primary text on light
val InkMuted = Color(0xFF494A40)

// --- Berry (error) ----------------------------------------------------------
val Berry = Color(0xFF9B4A4A)
val BerryContainer = Color(0xFFF3D9D6)
val BerryOnContainer = Color(0xFF3A0E0E)

// --- Dark surfaces ----------------------------------------------------------
val NightBg = Color(0xFF15140F)
val NightSurface = Color(0xFF1B1A14)
val NightSurfaceVariant = Color(0xFF45463B)
val Chalk = Color(0xFFEDEAE0)         // off-white text on dark
val ChalkMuted = Color(0xFFC9C7B7)

// --- Urgency status colors (paired with icon + label in UI; never color-only)
// Light variants
val StatusDoNow = Color(0xFFB23A2F)        // urgent terracotta-red
val StatusDueSoon = Honey                  // honey amber
val StatusWatchlist = Color(0xFF8A7F4E)    // muted ochre/olive
val StatusHealthy = Color(0xFF4E7A52)      // subdued leaf green
// Dark variants (lighter/desaturated for contrast on dark surfaces)
val StatusDoNowDark = Color(0xFFE6897E)
val StatusDueSoonDark = Color(0xFFE8C074)
val StatusWatchlistDark = Color(0xFFC9BC83)
val StatusHealthyDark = Color(0xFF8FBE92)
