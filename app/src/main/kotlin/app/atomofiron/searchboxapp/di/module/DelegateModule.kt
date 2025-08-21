package app.atomofiron.searchboxapp.di.module

import app.atomofiron.common.util.dialog.DialogDelegateImpl
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import app.atomofiron.searchboxapp.injectable.service.UtilService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.common.delegates.FileOperationsDelegate
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class DelegateModule {

    @Provides
    @Singleton
    open fun provideDialogDelegate(appStore: AppStore): DialogDelegate = DialogDelegateImpl(appStore.activityProperty)

    @Provides
    @Singleton
    open fun provideFileOperationsDelegate(
        preferenceStore: PreferenceStore,
        apks: ApkInteractor,
        dialogs: DialogDelegate,
        utils: UtilService,
    ): FileOperationsDelegate {
        return FileOperationsDelegate(preferenceStore, apks, dialogs, utils)
    }
}
