package app.atomofiron.searchboxapp.di.module

import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInstaller
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import app.atomofiron.searchboxapp.injectable.AppScope
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.service.ApkService
import app.atomofiron.searchboxapp.injectable.service.AppUpdateService
import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import app.atomofiron.searchboxapp.injectable.service.FinderService
import app.atomofiron.searchboxapp.injectable.service.UtilService
import app.atomofiron.searchboxapp.injectable.store.AppResources
import app.atomofiron.searchboxapp.injectable.store.AppUpdateStore
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class ServiceModule {

    @Provides
    @Singleton
    fun explorerService(
        context: Context,
        appScope: AppScope,
        explorerStore: ExplorerStore,
        preferenceStore: PreferenceStore,
    ): ExplorerService = ExplorerService(context, appScope, explorerStore, preferenceStore)

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
        context: Context,
        resources: AppResources,
        clipboardManager: ClipboardManager,
    ): UtilService = UtilService(context, resources, clipboardManager)

    @Provides
    @Singleton
    fun apkService(
        context: Context,
        packageInstaller: PackageInstaller,
    ): ApkService = ApkService(context, packageInstaller)

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
