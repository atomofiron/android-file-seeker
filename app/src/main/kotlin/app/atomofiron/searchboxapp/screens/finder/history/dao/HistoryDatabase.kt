package app.atomofiron.searchboxapp.screens.finder.history.dao

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ItemHistory::class], version = 2, exportSchema = false)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}