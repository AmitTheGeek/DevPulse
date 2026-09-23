package com.devpulse.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DevPulseDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                ALTER TABLE repositories
                ADD COLUMN ownerListMissingAtEpochMillis INTEGER
                """.trimIndent(),
            )
        }
    }
}
