package app.atomofiron.searchboxapp.di.dependencies.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.atomofiron.searchboxapp.di.dependencies.db.dao.ExplorerDao

object Migrations {
    val MIGRATION_2_3 get() = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
            CREATE TABLE ${ExplorerDao.SORTING} (
                tabIndex INTEGER NOT NULL,
                rootId INTEGER NOT NULL,
                sorting TEXT,
                PRIMARY KEY(tabIndex, rootId)
            )
            """.trimIndent())
        }
    }
}