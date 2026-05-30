package app.atomofiron.searchboxapp.screens.finder.presenter

import android.Manifest.permission.POST_NOTIFICATIONS
import app.atomofiron.searchboxapp.di.dependencies.db.dao.FinderDao
import app.atomofiron.searchboxapp.screens.finder.di.FinderInteractor
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.other.ByteSize
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.screens.common.delegates.StoragePermissionDelegate
import app.atomofiron.searchboxapp.screens.finder.FinderRouter
import app.atomofiron.searchboxapp.screens.finder.FinderScope
import app.atomofiron.searchboxapp.screens.finder.FinderViewState
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.ButtonsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.CharactersHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditCharactersHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditMaxDepthHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditMaxSizeHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditOptionsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.QueryFieldHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.SearchTaskHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.TestHolder
import app.atomofiron.searchboxapp.screens.finder.di.history.HistoryDao
import app.atomofiron.searchboxapp.screens.finder.di.history.ItemHistory
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.utils.CoroutineLauncher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@FinderScope
class FinderAdapterPresenterDelegate @Inject constructor(
    private val scope: CoroutineScope,
    private val viewState: FinderViewState,
    private val router: FinderRouter,
    private val storagePermissionDelegate: StoragePermissionDelegate,
    private val interactor: FinderInteractor,
    private val preferences: PreferenceStore,
    private val history: HistoryDao,
    private val cache: FinderDao,
) : CoroutineLauncher by CoroutineLauncher(scope),
    QueryFieldHolder.OnActionListener,
    CharactersHolder.OnActionListener,
    EditOptionsHolder.FinderConfigListener,
    EditCharactersHolder.OnEditCharactersListener,
    EditMaxDepthHolder.OnEditMaxDepthListener,
    EditMaxSizeHolder.OnEditMaxSizeListener,
    TestHolder.OnTestChangeListener,
    ButtonsHolder.FinderButtonsListener,
    SearchTaskHolder.OnActionListener<SearchResult> {

    override fun onOptionsChange(options: SearchOptions) {
        preferences { setSearchOptions(options) }
    }

    override fun onConfigVisibilityClick() = preferences {
        setShowSearchOptions(!showSearchOptions.value)
    }

    override fun onHistoryClick() = viewState.showHistory()

    override fun onCharacterClick(value: String) = viewState.insertInQuery(value)

    override fun onSearchChange(value: String) = viewState.updateSearchQuery(value)

    override fun onTestTextChange(value: String?) = preferences { setTestField(value) }

    override fun onItemClick(item: FinderStateItem.Task<SearchResult>) {
        router.showResult(item.task.uniqueId)
    }

    override fun onTaskStopClick(item: FinderStateItem.Task<SearchResult>) {
        interactor.stop(item.task.uuid)
    }

    override fun onTaskRemoveClick(item: FinderStateItem.Task<SearchResult>) {
        interactor.drop(item.task)
        io { cache.drop(item.task.uniqueId) }
    }

    override fun onReplaceClick(value: String) = Unit

    override fun onSearchClick(value: String) {
        val targets = viewState.targets.value
            .filter { it.isChecked }
            .run {
                filter { checked ->
                    !any { checked.parentRef.isChildOf(it.ref) }
                }
            }
        if (targets.isNotEmpty()) {
            router.permissions
                .request(POST_NOTIFICATIONS)
                .any {
                    storagePermissionDelegate.request(
                        granted = { startSearch(value, targets.map { it.ref }) },
                        denied = { viewState.showPermissionRequiredWarning() }
                    )
                }
        }
    }

    private fun startSearch(query: String, targets: List<NodeRef>) {
        io {
            if (history.exists(query)) history.delete(query)
            history.put(ItemHistory(query = query))
        }
        val config = viewState.toggles.value.toggles
        interactor.search(query, targets, config)
    }

    override fun onEditCharacters(new: List<String>) = preferences { setSpecialCharacters(new.toTypedArray()) }

    override fun onEditMaxDepth(new: Int) = preferences { setMaxDepthForSearch(new) }

    override fun onEditMaxSize(new: ByteSize) = preferences { setMaxFileSizeForSearch(new) }
}