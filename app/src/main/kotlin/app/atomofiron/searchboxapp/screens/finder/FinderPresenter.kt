package app.atomofiron.searchboxapp.screens.finder

import android.Manifest
import android.os.Environment
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.ButtonsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.CharactersHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.ConfigHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.FieldHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.ProgressHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.TargetsHolder
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
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
    private val finderStore: FinderStore,
    private val preferenceChannel: PreferenceChannel
) : BasePresenter<FinderViewModel, FinderRouter>(scope, router),
    FinderAdapterOutput,
    FieldHolder.OnActionListener by finderAdapterDelegate,
    CharactersHolder.OnActionListener by finderAdapterDelegate,
    ConfigHolder.FinderConfigListener by finderAdapterDelegate,
    ButtonsHolder.FinderButtonsListener by finderAdapterDelegate,
    ProgressHolder.OnActionListener by finderAdapterDelegate,
    TargetsHolder.FinderTargetsOutput by targetsDelegate
{

    init {
        viewState.run {
            uniqueItems.add(FinderStateItem.SearchAndReplaceItem())
            uniqueItems.add(FinderStateItem.SpecialCharactersItem(arrayOf()))
            uniqueItems.add(FinderStateItem.TestItem())
            uniqueItems.add(FinderStateItem.ButtonsItem)
        }
        onSubscribeData()
        viewState.switchConfigItemVisibility()
    }

    override fun onSubscribeData() {
        preferenceStore.excludeDirs.collect(scope) { excludeDirs ->
            viewState.setExcludeDirsValue(excludeDirs)
            viewState.updateState()
        }
        preferenceStore.dockGravity.collect(scope) { gravity ->
            viewState.historyDrawerGravity.value = gravity
        }
        preferenceStore.specialCharacters.collect(scope) { chs ->
            viewState.updateUniqueItem(FinderStateItem.SpecialCharactersItem(chs))
        }
        viewState.reloadHistory.collect(scope) {
            preferenceChannel.notifyHistoryImported()
        }
        finderStore.tasksFlow.collect(scope) { tasks ->
            viewState.progressItems.clear()
            viewState.progressItems.addAll(tasks.map { FinderStateItem.ProgressItem(it) })
            viewState.progressItems.reverse()
            viewState.updateState()
        }
    }

    fun onDockGravityChange(gravity: Int) = preferenceStore { setDockGravity(gravity) }

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