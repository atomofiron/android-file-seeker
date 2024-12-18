package app.atomofiron.searchboxapp.screens.explorer.presenter

import android.view.LayoutInflater
import app.atomofiron.common.arch.Recipient
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewState
import app.atomofiron.searchboxapp.screens.explorer.presenter.curtain.CreateDelegate
import app.atomofiron.searchboxapp.screens.explorer.presenter.curtain.OptionsDelegate
import app.atomofiron.searchboxapp.screens.explorer.presenter.curtain.RenameDelegate
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.isParentOf
import app.atomofiron.searchboxapp.utils.showCurtain
import kotlinx.coroutines.CoroutineScope

class ExplorerCurtainMenuDelegate(
    scope: CoroutineScope,
    private val viewState: ExplorerViewState,
    private val router: ExplorerRouter,
    private val explorerStore: ExplorerStore,
    private val explorerInteractor: ExplorerInteractor,
    curtainChannel: CurtainChannel,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>(), Recipient, MenuListener {
    private companion object {
        const val OPTIONS = 111
        const val CREATE = 222
        const val RENAME = 333
    }

    private val optionsDelegate = OptionsDelegate(R.menu.item_options_explorer, output = this)
    private val createDelegate = CreateDelegate(output = this)
    private val renameDelegate = RenameDelegate(output = this)

    private val currentTab get() = viewState.currentTab.value

    override var data: ExplorerItemOptions? = null

    init {
        curtainChannel.flow.collectForMe(scope, ::setController)
    }

    fun showOptions(options: ExplorerItemOptions) {
        data = options
        router.showCurtain(recipient, OPTIONS)
    }

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder? {
        return when (layoutId) {
            OPTIONS -> data?.let {
                optionsDelegate.getView(it, inflater)
            }
            CREATE -> data?.items?.firstOrNull()?.let {
                createDelegate.getView(it, inflater)
            }
            RENAME -> getRenameData()?.let {
                renameDelegate.getView(it, inflater)
            }
            else -> null
        }?.let {
            CurtainApi.ViewHolder(it)
        }
    }

    override fun onMenuItemSelected(id: Int) {
        val options = data ?: return
        val items = options.items
        when (id) {
            R.id.menu_create -> controller?.showNext(CREATE)
            R.id.menu_rename -> controller?.showNext(RENAME)
            R.id.menu_delete -> onRemoveConfirm(items)
            R.id.menu_share -> router.shareWith(items.first())
            R.id.menu_open_with -> router.openWith(items.first())
        }
    }

    fun onCreateConfirm(dir: Node, name: String, directory: Boolean) {
        controller?.close()
        explorerInteractor.create(currentTab, dir, name, directory)
    }

    fun onRenameConfirm(item: Node, name: String) {
        controller?.close()
        explorerInteractor.rename(currentTab, item, name)
    }

    private fun onRemoveConfirm(items: List<Node>) {
        controller?.close()
        explorerInteractor.deleteItems(currentTab, items)
    }

    private fun getRenameData(): RenameDelegate.RenameData? {
        val item = data?.items?.first() ?: return null
        val dirFiles = explorerStore.currentItems
            .find { it.isParentOf(item) }
            ?.children?.map { it.name }
        dirFiles ?: return null
        return RenameDelegate.RenameData(viewState.itemComposition.value, item, dirFiles)
    }
}