package app.atomofiron.searchboxapp.di.module

import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInstaller
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import app.atomofiron.searchboxapp.injectable.AppScope
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.injectable.service.*
import app.atomofiron.searchboxapp.injectable.store.*
import javax.inject.Singleton

@Module
open class ServiceModule {

    @Provides
    @Singleton
    fun explorerService(
        context: Context,
        appStore: AppStore,
        explorerStore: ExplorerStore,
        preferenceStore: PreferenceStore,
    ): ExplorerService = ExplorerService(context, appStore, explorerStore, preferenceStore)

    @Provides
    @Singleton
    fun finderService(
        scope: AppScope,
        workManager: WorkManager,
        notificationManager: NotificationManagerCompat,
        finderStore: FinderStore,
        preferenceStore: PreferenceStore,
        explorerStore: ExplorerStore,
    ): FinderService = FinderService(scope, workManager, notificationManager, finderStore, preferenceStore, explorerStore)

    @Provides
    @Singleton
    fun resultService(
        appStore: AppStore,
        clipboardManager: ClipboardManager,
    ): UtilService = UtilService(appStore, clipboardManager)

    @Provides
    @Singleton
    fun windowService(
        appStore: AppStore,
    ): WindowService = WindowService(appStore)

    @Provides
    @Singleton
    fun apkService(
        appStore: AppStore,
        packageInstaller: PackageInstaller,
    ): ApkService = ApkService(appStore.context, packageInstaller)

    @Provides
    @Singleton
    fun textViewerService(
        scope: AppScope,
        preferenceStore: PreferenceStore,
        textViewerStore: TextViewerStore,
        explorerStore: ExplorerStore,
        finderStore: FinderStore,
    ): TextViewerService = TextViewerService(scope, preferenceStore, textViewerStore, explorerStore, finderStore)

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
