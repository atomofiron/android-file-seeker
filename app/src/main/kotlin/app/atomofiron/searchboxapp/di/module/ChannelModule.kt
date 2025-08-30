package app.atomofiron.searchboxapp.di.module

import app.atomofiron.searchboxapp.injectable.AppScope
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.channel.MainChannel
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.channel.ResultChannel
import app.atomofiron.searchboxapp.injectable.store.AppUpdateStore
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
}