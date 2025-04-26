package app.atomofiron.searchboxapp.screens.viewer.presenter

import app.atomofiron.common.arch.Recipient
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.interactor.TextViewerInteractor
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.finder.SearchParams
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.screens.viewer.TextViewerRouter
import app.atomofiron.searchboxapp.screens.viewer.TextViewerViewState
import app.atomofiron.searchboxapp.screens.viewer.presenter.curtain.CurtainSearchDelegate
import kotlinx.coroutines.CoroutineScope

class SearchAdapterPresenterDelegate(
    scope: CoroutineScope,
    private val viewState: TextViewerViewState,
    private val router: TextViewerRouter,
    private val interactor: TextViewerInteractor,
    preferenceStore: PreferenceStore,
    curtainChannel: CurtainChannel,
) : Recipient, FinderAdapterOutput {

    private val curtainDelegate = CurtainSearchDelegate(this, viewState, scope)

    init {
        viewState.run {
            uniqueItems.add(FinderStateItem.SearchAndReplaceItem())
            val characters = preferenceStore.specialCharacters.value
            uniqueItems.add(FinderStateItem.SpecialCharactersItem(characters))
            uniqueItems.add(FinderStateItem.ConfigItem(isLocal = true))
            uniqueItems.add(FinderStateItem.TestItem())
            updateState()
        }
        curtainChannel.flow.collectForMe(scope) { controller ->
            curtainDelegate.setController(controller)
        }
    }

    fun show() = router.showCurtain(recipient, R.layout.curtain_text_viewer_search)

    override fun onConfigChange(item: FinderStateItem.ConfigItem) = viewState.updateConfig(item)

    override fun onConfigVisibilityClick() = Unit

    override fun onHistoryClick() = Unit

    override fun onCharacterClick(value: String) = viewState.sendInsertInQuery(value)

    override fun onSearchChange(value: String) = viewState.updateSearchQuery(value)

    override fun onSearchClick(value: String) {
        val config = viewState.getUniqueItem(FinderStateItem.ConfigItem::class)
        val params = SearchParams(value, config.ignoreCase, config.useRegex)
        interactor.search(viewState.item.value, params)
    }

    override fun onItemClick(item: FinderStateItem.ProgressItem) {
        if (viewState.trySelectTask(item.task)) {
            curtainDelegate.controller?.close()
        }
    }

    override fun onProgressRemoveClick(item: FinderStateItem.ProgressItem) {
        interactor.removeTask(viewState.item.value, item.task.uniqueId)
        viewState.dropTask()
    }

    override fun onReplaceClick(value: String) = Unit

    override fun onProgressStopClick(item: FinderStateItem.ProgressItem) = Unit
}