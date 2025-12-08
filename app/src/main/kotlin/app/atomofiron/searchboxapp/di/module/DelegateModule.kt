package app.atomofiron.searchboxapp.di.module

import app.atomofiron.common.util.ActivityProperty
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.common.util.dialog.DialogDelegateImpl
import app.atomofiron.searchboxapp.di.dependencies.interactor.ApkInteractor
import app.atomofiron.searchboxapp.di.dependencies.router.FilePickingDelegate
import app.atomofiron.searchboxapp.di.dependencies.router.FileSharingDelegate
import app.atomofiron.searchboxapp.di.dependencies.router.FileSharingDelegateImpl
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.common.delegates.FileOperationsDelegate
import dagger.Module
import dagger.Provides

@Module
open class DelegateModule {

    @Provides
    open fun provideDialogDelegate(activityProperty: ActivityProperty): DialogDelegate = DialogDelegateImpl(activityProperty)

    @Provides
    open fun provideFileOperationsDelegate(
        preferenceStore: PreferenceStore,
        apks: ApkInteractor,
        dialogs: DialogDelegate,
        utils: UtilService,
    ): FileOperationsDelegate {
        return FileOperationsDelegate(preferenceStore, apks, dialogs, utils)
    }

    @Provides
    fun fileSharingDelegate(
        activityProperty: ActivityProperty,
        preferences: PreferenceStore,
    ): FileSharingDelegate = FileSharingDelegateImpl(activityProperty, preferences)

    @Provides
    fun filePickingDelegate(
        activityProperty: ActivityProperty,
        preferences: PreferenceStore,
    ): FilePickingDelegate = FileSharingDelegateImpl(activityProperty, preferences)
}
