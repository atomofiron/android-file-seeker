package app.atomofiron.searchboxapp.di.module

import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceDataStore
import androidx.work.WorkManager
import app.atomofiron.searchboxapp.android.WebClient
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.delegate.InitialDelegate
import app.atomofiron.searchboxapp.di.dependencies.delegate.StorageDelegate
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.preferences.fragment.LegacyPreferenceDataStore
import dagger.Module
import dagger.Provides
import debug.LeakWatcher
import javax.inject.Singleton

@Module
class CommonModule {

    @Provides
    @Singleton
    fun appResources(context: Context) = AppResources(context.resources)

    @Provides
    @Singleton
    fun provideInitialDelegate(context: Context, packageManager: PackageManager): InitialDelegate {
        return InitialDelegate(context, packageManager)
    }

    @Provides
    @Singleton
    fun provideNotificationManager(context: Context): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
    }

    @Provides
    @Singleton
    fun provideWorkManager(context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideClipboardManager(context: Context): ClipboardManager {
        return context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    @Provides
    @Singleton
    fun provideCoroutineScope(): AppScope {
        return AppScope()
    }

    @Provides
    @Singleton
    fun preferenceDataStore(
        preferences: PreferenceStore,
        appScope: AppScope,
        watcher: LeakWatcher,
    ): PreferenceDataStore {
        return LegacyPreferenceDataStore(preferences, appScope, watcher)
    }

    @Provides
    @Singleton
    fun provideStorageDelegate(
        context: Context,
        store: ExplorerStore,
    ): StorageDelegate = StorageDelegate(context, store)

    @Provides
    @Singleton
    fun provideWebClient() = WebClient()
}
