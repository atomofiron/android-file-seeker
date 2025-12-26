package app.atomofiron.searchboxapp.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.di.dependencies.db.dao.ExplorerDao
import app.atomofiron.searchboxapp.di.dependencies.db.Database
import app.atomofiron.searchboxapp.di.dependencies.db.dao.FinderDao
import javax.inject.Singleton

@Module
open class DbModule {

    @Provides
    @Singleton
    open fun provideDatabase(context: Context): Database = Database(context)

    @Provides
    @Singleton
    open fun provideExplorerDao(db: Database): ExplorerDao = db.explorer()

    @Provides
    @Singleton
    open fun provideFinderDao(db: Database): FinderDao = db.finder()
}