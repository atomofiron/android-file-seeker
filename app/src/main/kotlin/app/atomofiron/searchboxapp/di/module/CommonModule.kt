package app.atomofiron.searchboxapp.di.module

import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceDataStore
import androidx.work.WorkManager
import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.preferences.fragment.LegacyPreferenceDataStore
import dagger.Module
import dagger.Provides
import debug.LeakWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
open class CommonModule {

    @Provides
    @Singleton
    open fun provideInitialDelegate(context: Context, packageManager: PackageManager): InitialDelegate {
        return InitialDelegate(context, packageManager)
    }

    @Provides
    @Singleton
    open fun provideNotificationManager(context: Context): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
    }

    @Provides
    @Singleton
    open fun provideWorkManager(context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    open fun provideClipboardManager(context: Context): ClipboardManager {
        return context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    @Provides
    @Singleton
    open fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.Default)
    }

    @Provides
    fun preferenceDataStore(
        preferences: PreferenceStore,
        appStore: AppStore,
        watcher: LeakWatcher,
    ): PreferenceDataStore {
        return LegacyPreferenceDataStore(preferences, appStore.appScope, watcher)
    }
}
