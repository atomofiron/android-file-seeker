package app.atomofiron.searchboxapp.di.dependencies.dao

import android.content.Context
import androidx.room.Database as DatabaseInfo
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.atomofiron.searchboxapp.model.explorer.other.Deepest

@DatabaseInfo(
    entities = [Deepest::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(NodeRefConverter::class)
abstract class Database : RoomDatabase() {
    companion object {
        operator fun invoke(context: Context): Database = Room.databaseBuilder(
            context,
            Database::class.java,
            name = "file-seeker",
        ).addMigrations(Migrations.MIGRATION_1_1).build()
    }
    abstract fun dao(): ExplorerDao
}
