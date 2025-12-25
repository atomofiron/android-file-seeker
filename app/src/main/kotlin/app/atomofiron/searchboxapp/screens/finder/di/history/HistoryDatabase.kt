package app.atomofiron.searchboxapp.screens.finder.di.history

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ItemHistory::class], version = 2, exportSchema = false)
abstract class HistoryDatabase : RoomDatabase() {
    companion object {
        operator fun invoke(context: Context): HistoryDatabase = Room.databaseBuilder(
            context,
            HistoryDatabase::class.java,
            name = "history",
        ).addMigrations(Migrations.MIGRATION_1_2)
            .build()
    }
    abstract fun historyDao(): HistoryDao
}