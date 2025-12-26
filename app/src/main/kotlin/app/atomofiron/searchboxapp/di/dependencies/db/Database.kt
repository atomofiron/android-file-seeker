package app.atomofiron.searchboxapp.di.dependencies.db

import android.content.Context
import androidx.room.Database as DatabaseInfo
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.searchboxapp.di.dependencies.db.dao.ExplorerDao
import app.atomofiron.searchboxapp.di.dependencies.db.dao.FinderDao
import app.atomofiron.searchboxapp.model.explorer.other.Deepest
import app.atomofiron.searchboxapp.model.finder.SearchResultCache

@DatabaseInfo(
    entities = [Deepest::class, SearchResultCache::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(NodeRefConverter::class)
abstract class Database : RoomDatabase() {
    companion object {
        operator fun invoke(context: Context): Database = Room.databaseBuilder(
            context,
            Database::class.java,
            name = "file-seeker",
        ).addMigrations()
            .fallbackToDestructiveMigration(BuildConfig.DEBUG)
            .build()
    }
    abstract fun explorer(): ExplorerDao
    abstract fun finder(): FinderDao
}
