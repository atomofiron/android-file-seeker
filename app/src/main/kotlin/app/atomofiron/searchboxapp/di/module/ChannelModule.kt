package app.atomofiron.searchboxapp.di.module

import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.channel.ApkChannel
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.channel.MainChannel
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.di.dependencies.channel.PreferenceChannel
import app.atomofiron.searchboxapp.di.dependencies.channel.ResultChannel
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources
import app.atomofiron.searchboxapp.di.dependencies.store.AppUpdateStore
import javax.inject.Singleton


@Module
open class ChannelModule {

    @Provides
    @Singleton
    open fun providePreferenceChannel(scope: AppScope, updateStore: AppUpdateStore): PreferenceChannel = PreferenceChannel(scope, updateStore.state)

    @Provides
    @Singleton
    open fun provideResultChannel(): ResultChannel = ResultChannel()

    @Provides
    @Singleton
    open fun provideCurtainChannel(appScope: AppScope): CurtainChannel = CurtainChannel(appScope)

    @Provides
    @Singleton
    open fun provideMainChannel(): MainChannel = MainChannel()

    @Provides
    @Singleton
    open fun provideApkChannel(appScope: AppScope, resources: AppResources): ApkChannel = ApkChannel(appScope, resources)
}