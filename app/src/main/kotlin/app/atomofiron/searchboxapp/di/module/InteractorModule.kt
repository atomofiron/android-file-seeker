package app.atomofiron.searchboxapp.di.module

import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
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
        appStore: AppStore,
        apkService: ApkService,
        explorerService: ExplorerService,
        dialogs: DialogDelegate,
    ): ApkInteractor = ApkInteractor(appStore.appScope, apkService, explorerService, dialogs)
}