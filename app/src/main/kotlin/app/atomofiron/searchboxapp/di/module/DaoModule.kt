package app.atomofiron.searchboxapp.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.di.dependencies.db.dao.ExplorerDao
import app.atomofiron.searchboxapp.di.dependencies.db.Database
import app.atomofiron.searchboxapp.di.dependencies.db.dao.FinderDao
import javax.inject.Singleton

@Module
class DaoModule {

    @Provides
    @Singleton
    fun provideDatabase(context: Context): Database = Database(context)

    @Provides
    @Singleton
    fun provideExplorerDao(db: Database): ExplorerDao = db.explorer()

    @Provides
    @Singleton
    fun provideFinderDao(db: Database): FinderDao = db.finder()
}