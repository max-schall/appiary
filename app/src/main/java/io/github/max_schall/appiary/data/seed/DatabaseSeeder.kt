package io.github.max_schall.appiary.data.seed

import io.github.max_schall.appiary.data.db.AppiaryDatabase
import io.github.max_schall.appiary.data.entity.SeasonalProfileEntity
import io.github.max_schall.appiary.domain.model.Hemisphere
import io.github.max_schall.appiary.util.newId

/**
 * Ensures the minimum configuration the app needs exists — currently just a
 * default seasonal profile. No demo apiaries/hives are created; the app starts
 * empty and the beekeeper adds their own.
 */
class DatabaseSeeder(
    private val db: AppiaryDatabase,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend fun ensureDefaults() {
        if (db.seasonalProfileDao().count() == 0) {
            val now = clock()
            db.seasonalProfileDao().upsert(
                SeasonalProfileEntity(
                    id = newId(), name = "Temperate · Northern", hemisphere = Hemisphere.NORTHERN,
                    activeSeasonStartMonth = 3, activeSeasonEndMonth = 9,
                    harvestStartMonth = 7, harvestEndMonth = 9, winterPrepMonth = 9,
                    selected = true, createdAt = now, updatedAt = now,
                ),
            )
        }
    }

    /** Wipe all user data (apiaries, hives, history, recommendations) and restore defaults. */
    suspend fun clearAllData() {
        db.clearAllTables()
        ensureDefaults()
    }
}
