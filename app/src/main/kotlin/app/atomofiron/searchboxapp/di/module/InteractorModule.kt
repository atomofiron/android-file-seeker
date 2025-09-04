package app.atomofiron.searchboxapp.di.module

import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.channel.ApkChannel
import app.atomofiron.searchboxapp.di.dependencies.interactor.ApkInteractor
import app.atomofiron.searchboxapp.di.dependencies.service.ApkService
import app.atomofiron.searchboxapp.di.dependencies.service.ExplorerService
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