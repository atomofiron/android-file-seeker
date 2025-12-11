package app.atomofiron.searchboxapp.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.di.dependencies.db.dao.ExplorerDao
import app.atomofiron.searchboxapp.di.dependencies.db.dao.ExplorerDatabase
import javax.inject.Singleton

@Module
open class DbModule {

    @Provides
    @Singleton
    open fun provideExplorerDao(context: Context): ExplorerDao = ExplorerDatabase(context).dao()
}