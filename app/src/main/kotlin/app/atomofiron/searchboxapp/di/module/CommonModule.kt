package app.atomofiron.searchboxapp.di.module

import android.content.ClipboardManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import app.atomofiron.common.util.Android
import app.atomofiron.searchboxapp.android.BluetoothFilesObserver
import app.atomofiron.searchboxapp.android.BluetoothFilesObserverImpl
import app.atomofiron.searchboxapp.android.WebClient
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.BluetoothFilesProvider
import app.atomofiron.searchboxapp.di.dependencies.channel.PreferenceChannel
import app.atomofiron.searchboxapp.di.dependencies.service.ApkService
import app.atomofiron.searchboxapp.di.dependencies.service.AppUpdateService
import app.atomofiron.searchboxapp.di.dependencies.store.AppUpdateStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CommonModule {

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
    fun provideBluetoothFilesProvider(scope: AppScope): BluetoothFilesProvider? = when {
        Android.Q -> BluetoothFilesProvider(scope)
        else -> null
    }

    @Provides
    @Singleton
    fun provideBluetoothFilesObserver(
        context: Context,
        provider: BluetoothFilesProvider?,
    ): BluetoothFilesObserver? = when {
        provider == null -> null
        Android.Q -> BluetoothFilesObserverImpl(context, provider)
        else -> null
    }

    @Provides
    @Singleton
    fun provideWebClient() = WebClient()

    @Provides
    @Singleton
    fun updateService(
        factory: AppUpdateService.Factory,
        context: Context,
        scope: AppScope,
        apkService: ApkService,
        updateStore: AppUpdateStore,
        preferences: PreferenceStore,
        preferenceChannel: PreferenceChannel,
    ): AppUpdateService = factory.new(
        context,
        scope,
        apkService,
        updateStore,
        preferences,
        preferenceChannel,
    )
}
