package app.atomofiron.searchboxapp.di.module

import android.content.Context
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources
import app.atomofiron.searchboxapp.di.dependencies.store.AppUpdateStore
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.di.dependencies.store.ResultStore
import app.atomofiron.searchboxapp.di.dependencies.store.TextViewerStore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class StoreModule {

    @Provides
    @Singleton
    open fun appResources(context: Context) = AppResources(context.resources)

    @Provides
    @Singleton
    open fun provideFinderStore(scope: AppScope): FinderStore = FinderStore(scope)

    @Provides
    @Singleton
    open fun provideResultStore(): ResultStore = ResultStore()

    @Provides
    @Singleton
    open fun provideTextViewerStore(): TextViewerStore = TextViewerStore()

    @Provides
    @Singleton
    open fun provideExplorerStore(): ExplorerStore = ExplorerStore()

    @Provides
    @Singleton
    open fun providePreferenceStore(context: Context, scope: AppScope): PreferenceStore {
        return PreferenceStore(context, scope)
    }

    @Provides
    @Singleton
    open fun provideUpdateStore() = AppUpdateStore()
}
