package app.atomofiron.searchboxapp.di.dependencies.db.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Deepest::class], version = 1, exportSchema = false)
@TypeConverters(NodeRefConverter::class)
abstract class ExplorerDatabase : RoomDatabase() {
    companion object {
        operator fun invoke(context: Context): ExplorerDatabase = Room.databaseBuilder(
            context,
            ExplorerDatabase::class.java,
            name = "explorer",
        ).addMigrations(Migrations.MIGRATION_1_1).build()
    }
    abstract fun dao(): ExplorerDao
}
