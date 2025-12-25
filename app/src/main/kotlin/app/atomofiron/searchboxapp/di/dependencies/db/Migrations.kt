package app.atomofiron.searchboxapp.di.dependencies.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_1_1 get() = object : Migration(1, 1) {
        override fun migrate(db: SupportSQLiteDatabase) = Unit
    }
}