package io.github.max_schall.appiary.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.max_schall.appiary.data.db.AppiaryDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Room migration tests on the JVM via Robolectric — no emulator needed. The
 * exported schema JSON (app/schemas) is wired in as unit-test assets so
 * [MigrationTestHelper] can validate that each migrated schema matches.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class MigrationTest {

    private val dbName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppiaryDatabase::class.java,
    )

    @Test
    fun migratesFromV1ThroughV7() {
        helper.createDatabase(dbName, 1).close()
        helper.runMigrationsAndValidate(
            dbName, 7, true,
            AppiaryDatabase.MIGRATION_1_2,
            AppiaryDatabase.MIGRATION_2_3,
            AppiaryDatabase.MIGRATION_3_4,
            AppiaryDatabase.MIGRATION_4_5,
            AppiaryDatabase.MIGRATION_5_6,
            AppiaryDatabase.MIGRATION_6_7,
        ).close()
    }

    @Test
    fun v4ToV7_preservesHives_andAddsLineageDefaults() {
        helper.createDatabase(dbName, 4).apply {
            execSQL("INSERT INTO apiaries (id,name,siteId,notes,createdAt,updatedAt) VALUES ('a1','Yard',NULL,NULL,0,0)")
            execSQL(
                "INSERT INTO hives (id,apiaryId,name,status,queenStatus,broodPattern,strength," +
                    "temperament,foodStores,treatmentState,archived,createdAt,updatedAt) " +
                    "VALUES ('h1','a1','Hive 1','ACTIVE','QUEENRIGHT','GOOD','STRONG','CALM','OKAY','NONE',0,0,0)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            dbName, 7, true,
            AppiaryDatabase.MIGRATION_4_5,
            AppiaryDatabase.MIGRATION_5_6,
            AppiaryDatabase.MIGRATION_6_7,
        )

        db.query("SELECT originType, parentHiveId FROM hives WHERE id = 'h1'").use { c ->
            assertTrue("hive row should survive the migration", c.moveToFirst())
            assertEquals("UNKNOWN", c.getString(0))
            assertNull(c.getString(1))
        }
        // The new v6 table exists and is queryable.
        db.query("SELECT COUNT(*) FROM weight_entries").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(0, c.getInt(0))
        }
        // The new v7 inventory table exists and is queryable.
        db.query("SELECT COUNT(*) FROM inventory_items").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(0, c.getInt(0))
        }
        db.close()
    }
}
