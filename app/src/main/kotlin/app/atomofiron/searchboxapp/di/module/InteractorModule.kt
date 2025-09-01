package app.atomofiron.searchboxapp.di.module

import app.atomofiron.searchboxapp.injectable.AppScope
import app.atomofiron.searchboxapp.injectable.channel.ApkChannel
import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import app.atomofiron.searchboxapp.injectable.service.ApkService
import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class InteractorModule {

    @Provides
    @Singleton
    fun apks(
        appScope: AppScope,
        apkService: ApkService,
        explorerService: ExplorerService,
        apkChannel: ApkChannel,
    ): ApkInteractor = ApkInteractor(appScope, apkService, explorerService, apkChannel)
}