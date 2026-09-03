package app.atomofiron.searchboxapp.screens.explorer.presenter

import app.atomofiron.searchboxapp.model.explorer.NodeRootOption
import app.atomofiron.searchboxapp.screens.explorer.ExplorerScope
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewState
import app.atomofiron.searchboxapp.screens.explorer.di.ExplorerInteractor
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.options.RootOptionAdapter.RootOptionListener
import javax.inject.Inject

@ExplorerScope
class RootOptionDelegate @Inject constructor(
    private val viewState: ExplorerViewState,
    private val interactor: ExplorerInteractor,
) : RootOptionListener {

    private val currentTab get() = viewState.currentTab.value

    override fun onClick(target: NodeRootOption.CameraToggle) {
        interactor.setCameraOption(currentTab, target)
    }
}