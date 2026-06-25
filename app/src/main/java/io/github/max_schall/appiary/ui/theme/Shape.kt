package io.github.max_schall.appiary.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Medium, tactile rounding — labeled-inspection-sheet feel, not bubble UI.
 * Cards land on [medium] (12.dp); chips/segmented controls use [small].
 */
val AppiaryShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)
