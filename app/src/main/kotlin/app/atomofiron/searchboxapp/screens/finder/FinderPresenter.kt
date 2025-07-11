package app.atomofiron.searchboxapp.screens.finder

import android.Manifest
import android.os.Environment
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.ButtonsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.CharactersHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.OptionsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditCharactersHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditMaxDepthHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditMaxSizeHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.QueryFieldHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.TaskHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.TargetsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.TestHolder
import app.atomofiron.searchboxapp.screens.finder.presenter.FinderAdapterPresenterDelegate
import app.atomofiron.searchboxapp.screens.finder.presenter.FinderTargetsPresenterDelegate
import kotlinx.coroutines.CoroutineScope

class FinderPresenter(
    scope: CoroutineScope,
    private val viewState: FinderViewState,
    router: FinderRouter,
    finderAdapterDelegate: FinderAdapterPresenterDelegate,
    targetsDelegate: FinderTargetsPresenterDelegate,
    private val preferenceStore: PreferenceStore,
    private val preferenceChannel: PreferenceChannel
) : BasePresenter<FinderViewModel, FinderRouter>(scope, router),
    FinderAdapterOutput,
    QueryFieldHolder.OnActionListener by finderAdapterDelegate,
    CharactersHolder.OnActionListener by finderAdapterDelegate,
    EditCharactersHolder.OnEditCharactersListener by finderAdapterDelegate,
    EditMaxDepthHolder.OnEditMaxDepthListener by finderAdapterDelegate,
    TestHolder.OnTestChangeListener by finderAdapterDelegate,
    EditMaxSizeHolder.OnEditMaxSizeListener by finderAdapterDelegate,
    OptionsHolder.FinderConfigListener by finderAdapterDelegate,
    ButtonsHolder.FinderButtonsListener by finderAdapterDelegate,
    TaskHolder.OnActionListener by finderAdapterDelegate,
    TargetsHolder.FinderTargetsOutput by targetsDelegate
{

    init {
        onSubscribeData()
    }

    override fun onSubscribeData() {
        preferenceStore.drawerGravity.collect(scope) { gravity ->
            viewState.historyDrawerGravity.value = gravity
        }
        viewState.reloadHistory.collect(scope) {
            preferenceChannel.notifyHistoryImported()
        }
    }

    fun onDrawerGravityChange(gravity: Int) = preferenceStore { setDrawerGravity(gravity) }

    fun onExplorerOptionSelected() {
        when {
            Android.Below.R -> {
                router.permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .granted { router.showExplorer() }
                    .denied { _, _ ->
                        viewState.showPermissionRequiredWarning()
                    }
            }
            Environment.isExternalStorageManager() -> router.showExplorer()
            else -> router.showSystemPermissionsAppSettings()
        }
    }

    fun onSettingsOptionSelected() = router.showSettings()

    fun onHistoryItemClick(node: String) = viewState.replaceQuery(node)

    fun onAllowStorageClick() = router.showSystemPermissionsAppSettings()
}