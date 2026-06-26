package io.github.max_schall.appiary.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Appiary palette — "modern field notebook".
 *
 * Anchors: honey gold, leaf green, bark brown, warm cream, muted berry — sampled
 * from the app logo (see the Logo* tokens below, which drive both themes). The
 * goal is a calm, grounded, sunlight-readable surface that reads like a
 * beekeeping journal rather than a generic dashboard. Values are hand-tuned (not
 * dynamic-generated) so the brand stays consistent outdoors and across devices.
 */

// --- Honey / amber (due-soon status) ----------------------------------------
val Honey = Color(0xFFD9A441)

// --- Berry (error) ----------------------------------------------------------
val Berry = Color(0xFF9B4A4A)
val BerryContainer = Color(0xFFF3D9D6)
val BerryOnContainer = Color(0xFF3A0E0E)

// --- Logo palette (drives the default light theme) --------------------------
// Sampled straight from the app icon: golden skep, bark-brown linework, leaf
// green, and the warm cream the artwork sits on.
val LogoHoneyGold = Color(0xFFDDA53A)     // the skep body
val LogoHoneyDeep = Color(0xFFB47B22)     // deeper gold, readable as primary
val LogoHoneyContainer = Color(0xFFF7E4BB)
val LogoHoneyOnContainer = Color(0xFF492F0A)
val LogoBark = Color(0xFF5C3B22)          // brown outlines (text / tertiary)
val LogoBarkDark = Color(0xFF3E2817)
val LogoBarkContainer = Color(0xFFEADBC8)
val LogoLeaf = Color(0xFF5E7A38)          // the leaf
val LogoLeafDark = Color(0xFF324319)
val LogoLeafContainer = Color(0xFFDDE7C6)
val LogoCream = Color(0xFFFBF6EA)         // artwork background
val LogoSurface = Color(0xFFFFFDF7)
val LogoSurfaceVariant = Color(0xFFEDE4D2)
val LogoInk = Color(0xFF2C2317)           // warm near-black text
val LogoInkMuted = Color(0xFF59503F)
val LogoOutline = Color(0xFF8A8270)
val LogoOutlineVariant = Color(0xFFD6CCB7)

// --- Logo palette, dark variant (drives the dark theme) ---------------------
// Same honey-gold / leaf / bark identity, lightened & desaturated for legibility
// on warm near-black surfaces so dark mode shares the light theme's brand.
val LogoHoneyLight = Color(0xFFE8C074)    // lightened skep gold (dark primary)
val LogoHoneyOnDark = Color(0xFF452C08)   // text/icon on the gold
val LogoHoneyDarkContainer = Color(0xFF6E4E14)
val LogoHoneyOnDarkContainer = Color(0xFFF7E4BB)
val LogoLeafLight = Color(0xFFAAC57E)     // lightened leaf (dark secondary)
val LogoLeafOnDark = Color(0xFF253410)
val LogoLeafDarkContainer = Color(0xFF44561F)
val LogoLeafOnDarkContainer = Color(0xFFDDE7C6)
val LogoBarkLight = Color(0xFFD3B595)     // warm tan (dark tertiary)
val LogoBarkOnDark = Color(0xFF3E2817)
val LogoBarkDarkContainer = Color(0xFF5C3B22)
val LogoBarkOnDarkContainer = Color(0xFFEADBC8)
val LogoNightBg = Color(0xFF16120B)       // warm near-black background
val LogoNightSurface = Color(0xFF1D1810)
val LogoNightSurfaceVariant = Color(0xFF4A4032)
val LogoChalk = Color(0xFFEDE4D2)         // warm off-white text on dark
val LogoChalkMuted = Color(0xFFCFC4AE)
val LogoOutlineDark = Color(0xFF978C76)
val LogoOutlineVariantDark = Color(0xFF4A4234)

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
