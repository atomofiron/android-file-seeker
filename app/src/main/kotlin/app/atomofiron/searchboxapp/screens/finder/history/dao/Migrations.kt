package app.atomofiron.searchboxapp.screens.finder.history.dao


import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.atomofiron.searchboxapp.poop

object Migrations {
    val MIGRATION_1_2 get() = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.beginTransaction()
            db.execSQL("CREATE TABLE ${HistoryDao.TABLE_NAME} (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, pinned INTEGER NOT NULL)")
            db.execSQL("INSERT INTO ${HistoryDao.TABLE_NAME} (id, title, pinned) SELECT id, title, pinned FROM ItemHistory")
            db.execSQL("DROP TABLE ItemHistory")
            db.setTransactionSuccessful()
            db.endTransaction()
        }
    }
}