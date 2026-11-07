package com.keizuka.vapeconfig.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseFactory {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE catalog_items ADD COLUMN connection TEXT NOT NULL DEFAULT ''")
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS battery_profiles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    kind TEXT NOT NULL,
                    model TEXT NOT NULL,
                    format TEXT NOT NULL,
                    capacityMah TEXT NOT NULL,
                    currentRatingA TEXT NOT NULL,
                    manufacturerSource TEXT NOT NULL,
                    notes TEXT NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

    fun create(context: Context): VapeConfigDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            VapeConfigDatabase::class.java,
            "vapeconfig.db",
        ).addMigrations(MIGRATION_1_2).build()
}
