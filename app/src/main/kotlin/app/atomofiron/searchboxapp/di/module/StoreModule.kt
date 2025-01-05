package app.atomofiron.searchboxapp.di.module

import android.content.Context
import app.atomofiron.searchboxapp.injectable.store.*
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
open class StoreModule {

    @Provides
    @Singleton
    open fun appResources(context: Context) = AppResources(context.resources)

    @Provides
    @Singleton
    open fun provideFinderStore(scope: CoroutineScope): FinderStore = FinderStore(scope)

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
    open fun providePreferenceStore(context: Context, scope: CoroutineScope): PreferenceStore {
        return PreferenceStore(context, scope)
    }

    @Provides
    @Singleton
    open fun provideAppStore(context: Context, resources: AppResources, scope: CoroutineScope): AppStore {
        return AppStore(context, scope, resources)
    }

    @Provides
    @Singleton
    open fun provideUpdateStore() = AppUpdateStore()
}
