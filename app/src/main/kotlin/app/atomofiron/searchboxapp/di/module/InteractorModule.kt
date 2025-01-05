package app.atomofiron.searchboxapp.di.module

import android.content.Context
import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import app.atomofiron.searchboxapp.injectable.interactor.DialogInteractor
import app.atomofiron.searchboxapp.injectable.service.ApkService
import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class InteractorModule {

    @Provides
    @Singleton
    fun apks(
        context: Context,
        appStore: AppStore,
        apkService: ApkService,
        explorerService: ExplorerService,
    ): ApkInteractor = ApkInteractor(context, appStore.scope, apkService, explorerService)

    @Provides
    @Singleton
    fun dialogs(appStore: AppStore) = DialogInteractor(appStore)
}