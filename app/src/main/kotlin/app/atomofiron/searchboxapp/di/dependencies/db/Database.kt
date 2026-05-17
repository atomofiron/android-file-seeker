package app.atomofiron.searchboxapp.di.dependencies.db

import android.content.Context
import androidx.room.Database as DatabaseInfo
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.atomofiron.searchboxapp.di.dependencies.db.dao.ExplorerDao
import app.atomofiron.searchboxapp.di.dependencies.db.dao.FinderDao
import app.atomofiron.searchboxapp.model.explorer.other.Deepest
import app.atomofiron.searchboxapp.model.explorer.other.TabRootSorting
import app.atomofiron.searchboxapp.model.finder.SearchResultCache

@DatabaseInfo(
    entities = [Deepest::class, TabRootSorting::class, SearchResultCache::class],
    version = 4,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    companion object {
        operator fun invoke(context: Context): Database = Room.databaseBuilder(
            context,
            Database::class.java,
            name = "db",
        ).addMigrations(Migrations.from2to3())
            .addMigrations(Migrations.from3to4(context))
            .fallbackToDestructiveMigration(app.atomofiron.fileseeker.BuildConfig.DEBUG)
            .build()
    }
    abstract fun explorer(): ExplorerDao
    abstract fun finder(): FinderDao
}
