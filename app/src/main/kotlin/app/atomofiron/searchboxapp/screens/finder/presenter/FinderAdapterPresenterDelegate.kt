package app.atomofiron.searchboxapp.screens.finder.presenter

import app.atomofiron.searchboxapp.injectable.interactor.FinderInteractor
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.finder.FinderRouter
import app.atomofiron.searchboxapp.screens.finder.FinderViewState
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.ButtonsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.CharactersHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.ConfigHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.FieldHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.ProgressHolder
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class FinderAdapterPresenterDelegate(
    private val viewState: FinderViewState,
    private val router: FinderRouter,
    private val interactor: FinderInteractor
) :
    FieldHolder.OnActionListener,
    CharactersHolder.OnActionListener,
    ConfigHolder.FinderConfigListener,
    ButtonsHolder.FinderButtonsListener,
    ProgressHolder.OnActionListener {

    override fun onConfigChange(item: FinderStateItem.ConfigItem) = viewState.updateConfig(item)

    override fun onConfigVisibilityClick() = viewState.switchConfigItemVisibility()

    override fun onHistoryClick() = viewState.showHistory()

    override fun onCharacterClick(value: String) = viewState.insertInQuery(value)

    override fun onSearchChange(value: String) = viewState.updateSearchQuery(value)

    override fun onItemClick(item: FinderStateItem.ProgressItem) {
        router.showResult(item.task.uniqueId)
    }

    override fun onProgressStopClick(item: FinderStateItem.ProgressItem) {
        interactor.stop(item.task.uuid)
    }

    override fun onProgressRemoveClick(item: FinderStateItem.ProgressItem) {
        interactor.drop(item.task)
    }

    override fun onReplaceClick(value: String) {
    }

    override fun onSearchClick(value: String) {
        val targets = viewState.targets.filter { it.isChecked }.run {
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
        val config = viewState.configItem
        interactor.search(query, targets, config.ignoreCase, config.useRegex, config.excludeDirs, config.searchInContent)
    }
}