package app.atomofiron.searchboxapp.di.dependencies.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.atomofiron.searchboxapp.di.dependencies.db.dao.ExplorerDao
import app.atomofiron.searchboxapp.di.dependencies.db.dao.FinderDao

private const val TMP = "tmp"

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
    val MIGRATION_3_4 get() = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE $TMP (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    stopped INTEGER NOT NULL,
                    params BLOB NOT NULL,
                    result BLOB NOT NULL,
                    version INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("""
                INSERT INTO $TMP (stopped, params, result, version)
                SELECT stopped, params, result, version FROM ${FinderDao.RESULT}
                """.trimIndent()
            )
            db.execSQL("DROP TABLE ${FinderDao.RESULT}")
            db.execSQL("ALTER TABLE $TMP RENAME TO ${FinderDao.RESULT}")
        }
    }
}