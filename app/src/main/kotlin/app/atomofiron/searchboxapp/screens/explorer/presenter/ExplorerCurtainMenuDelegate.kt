package app.atomofiron.searchboxapp.screens.explorer.presenter

import android.view.LayoutInflater
import app.atomofiron.common.arch.Recipient
import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.common.delegates.FileOperationDelegate
import app.atomofiron.searchboxapp.screens.common.delegates.Operations
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainId
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerScope
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewState
import app.atomofiron.searchboxapp.screens.explorer.curtain.CloneDelegate
import app.atomofiron.searchboxapp.screens.explorer.curtain.CloneCurtainKey
import app.atomofiron.searchboxapp.screens.explorer.curtain.CreateDelegate
import app.atomofiron.searchboxapp.screens.explorer.curtain.CreateCurtainKey
import app.atomofiron.searchboxapp.screens.explorer.curtain.OptionsDelegate
import app.atomofiron.searchboxapp.screens.explorer.curtain.OptionsCurtainKey
import app.atomofiron.searchboxapp.screens.explorer.curtain.RenameDelegate
import app.atomofiron.searchboxapp.screens.explorer.curtain.RenameCurtainKey
import app.atomofiron.searchboxapp.screens.explorer.di.ExplorerInteractor
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isParentOf
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@ExplorerScope
class ExplorerCurtainMenuDelegate @Inject constructor(
    scope: CoroutineScope,
    private val viewState: ExplorerViewState,
    private val router: ExplorerRouter,
    private val explorerStore: ExplorerStore,
    private val operations: FileOperationDelegate,
    private val interactor: ExplorerInteractor,
    curtainChannel: CurtainChannel,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>(), Recipient, MenuListener {

    private val optionsDelegate = OptionsDelegate(output = this)
    private val createDelegate = CreateDelegate(output = this)
    private val cloneDelegate = CloneDelegate(output = this)
    private val renameDelegate = RenameDelegate(output = this)

    private val currentTab get() = viewState.currentTab.value

    override var data: ExplorerItemOptions? = null

    init {
        curtainChannel.flow.collectForMe(scope, ::setController)
    }

    fun showOptions(options: ExplorerItemOptions) {
        data = options
        router.showCurtain(OptionsCurtainKey, recipient)
    }

    override fun getHolder(inflater: LayoutInflater, id: CurtainId): CurtainApi.ViewHolder? {
        val data = data ?: return null
        val first = data.items.firstOrNull() ?: return null
        return when (id) {
            OptionsCurtainKey.id -> optionsDelegate.getView(data, inflater)
            CreateCurtainKey.id -> createDelegate.getView(first, inflater)
            CloneCurtainKey.id -> {
                val parent = explorerStore.currentItems
                    .find { it.ref == first.parentRef }
                    ?: return null
                cloneDelegate.getView(parent, first, inflater)
            }
            RenameCurtainKey.id -> first.getRenameData()
                ?.let { renameDelegate.getView(it, inflater) }
            else -> null
        }?.let {
            CurtainApi.ViewHolder(it)
        }
    }

    override fun onMenuItemSelected(item: MenuItem) {
        val options = data ?: return
        val targets = options.items
        when (item.id) {
            Operations.Duplicate.id -> controller?.showNext(CloneCurtainKey)
            Operations.Create.id -> controller?.showNext(CreateCurtainKey)
            Operations.Rename.id -> controller?.showNext(RenameCurtainKey)
            else -> null
        }?.let { return }
        val (alert, operations) = operations.action(item, targets, viewState.currentTab.value)
        alert?.let { controller?.showSnackbar(it) }
        if (alert?.error == true) {
            return
        }
        when (item.id) {
            Operations.ByCopying.id,
            Operations.ByMoving.id,
            Operations.Delete.id -> controller?.close(irrevocably = true)
                .also { return }
        }
        operations?.let { optionsDelegate.bind(it) }
    }

    fun onCloneConfirm(target: Node, name: String) {
        controller?.close(irrevocably = true)
        interactor.clone(currentTab, target, name)
    }

    fun onCreateConfirm(dir: Node, name: String, directory: Boolean) {
        controller?.close(irrevocably = true)
        interactor.create(currentTab, dir, name, directory)
    }

    fun onRenameConfirm(item: Node, name: String) {
        controller?.close(irrevocably = true)
        interactor.rename(currentTab, item.ref, name)
    }

    private fun Node.getRenameData(): RenameDelegate.RenameData? {
        val dirFiles = explorerStore.currentItems
            .find { it.isParentOf(this) }
            ?.children?.map { it.name }
            ?: return null
        return RenameDelegate.RenameData(viewState.itemComposition.value, this, dirFiles)
    }
}