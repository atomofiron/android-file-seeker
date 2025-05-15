package app.atomofiron.searchboxapp.di.module

import app.atomofiron.common.util.DialogDelegate
import app.atomofiron.common.util.DialogMaker
import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.delegates.FileOperationsDelegate
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class DelegateModule {

    @Provides
    @Singleton
    open fun provideDialogMaker(appStore: AppStore): DialogMaker = DialogDelegate(appStore.activityProperty)

    @Provides
    @Singleton
    open fun provideFileOperationsDelegate(
        preferenceStore: PreferenceStore,
        apks: ApkInteractor,
        dialogs: DialogMaker,
    ): FileOperationsDelegate {
        return FileOperationsDelegate(preferenceStore, apks, dialogs)
    }
}
