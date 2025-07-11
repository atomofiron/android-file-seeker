package app.atomofiron.searchboxapp.screens.finder.presenter

import app.atomofiron.searchboxapp.injectable.interactor.FinderInteractor
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.finder.FinderRouter
import app.atomofiron.searchboxapp.screens.finder.FinderViewState
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.ButtonsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.CharactersHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.OptionsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditCharactersHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditMaxDepthHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditMaxSizeHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.QueryFieldHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.TaskHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.TestHolder
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem

class FinderAdapterPresenterDelegate(
    private val viewState: FinderViewState,
    private val router: FinderRouter,
    private val interactor: FinderInteractor,
    private val preferences: PreferenceStore,
) :
    QueryFieldHolder.OnActionListener,
    CharactersHolder.OnActionListener,
    OptionsHolder.FinderConfigListener,
    EditCharactersHolder.OnEditCharactersListener,
    EditMaxDepthHolder.OnEditMaxDepthListener,
    EditMaxSizeHolder.OnEditMaxSizeListener,
    TestHolder.OnTestChangeListener,
    ButtonsHolder.FinderButtonsListener,
    TaskHolder.OnActionListener {

    override fun onConfigChange(item: FinderStateItem.Options) {
        preferences { setSearchOptions(item.toggles) }
    }

    override fun onConfigVisibilityClick() = preferences {
        setShowSearchOptions(!showSearchOptions.value)
    }

    override fun onHistoryClick() = viewState.showHistory()

    override fun onCharacterClick(value: String) = viewState.insertInQuery(value)

    override fun onSearchChange(value: String) = viewState.updateSearchQuery(value)

    override fun onTestTextChange(value: String) = preferences { setTestField(value) }

    override fun onItemClick(item: FinderStateItem.Task) {
        router.showResult(item.task.uniqueId)
    }

    override fun onProgressStopClick(item: FinderStateItem.Task) {
        interactor.stop(item.task.uuid)
    }

    override fun onProgressRemoveClick(item: FinderStateItem.Task) {
        interactor.drop(item.task)
    }

    override fun onReplaceClick(value: String) {
    }

    override fun onSearchClick(value: String) {
        val targets = viewState.targets.value
            .filter { it.isChecked }
            .run {
                filter { checked ->
                    !any { checked.parentPath.startsWith(it.path) }
                }
            }
        if (targets.isNotEmpty()) {
            router.permissions
                .request(android.Manifest.permission.POST_NOTIFICATIONS)
                .any { startSearch(value, targets) }
        }
    }

    private fun startSearch(query: String, targets: List<Node>) {
        viewState.addToHistory(query)
        val config = viewState.toggles.value.toggles
        interactor.search(query, targets, config)
    }

    override fun onEditCharacters(new: List<String>) = preferences { setSpecialCharacters(new.toTypedArray()) }

    override fun onEditMaxDepth(new: Int) = preferences { setMaxDepthForSearch(new) }

    override fun onEditMaxSize(new: Int) = preferences { setMaxFileSizeForSearch(new) }
}