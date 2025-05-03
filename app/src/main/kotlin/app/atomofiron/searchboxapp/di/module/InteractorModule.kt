package app.atomofiron.searchboxapp.di.module

import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import app.atomofiron.searchboxapp.injectable.interactor.DialogInteractor
import app.atomofiron.searchboxapp.injectable.service.ApkService
import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import app.atomofiron.searchboxapp.injectable.service.UtilService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class InteractorModule {

    @Provides
    @Singleton
    fun apks(
        appStore: AppStore,
        apkService: ApkService,
        explorerService: ExplorerService,
    ): ApkInteractor = ApkInteractor(appStore.scope, apkService, explorerService)

    @Provides
    @Singleton
    fun dialogs(appStore: AppStore, utils: UtilService) = DialogInteractor(appStore, utils)
}