package app.atomofiron.searchboxapp.screens.explorer.presenter

import android.view.LayoutInflater
import app.atomofiron.common.arch.Recipient
import app.atomofiron.common.util.Android
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.interactor.ApkInteractor
import app.atomofiron.searchboxapp.di.dependencies.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.common.delegates.Operations
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewState
import app.atomofiron.searchboxapp.screens.explorer.curtain.CloneDelegate
import app.atomofiron.searchboxapp.screens.explorer.curtain.CreateDelegate
import app.atomofiron.searchboxapp.screens.explorer.curtain.OptionsDelegate
import app.atomofiron.searchboxapp.screens.explorer.curtain.RenameDelegate
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isParentOf
import kotlinx.coroutines.CoroutineScope

private const val OPTIONS = 111
private const val CREATE = 222
private const val RENAME = 333
private const val CLONE = 444

class ExplorerCurtainMenuDelegate(
    scope: CoroutineScope,
    private val viewState: ExplorerViewState,
    private val router: ExplorerRouter,
    private val explorerStore: ExplorerStore,
    private val interactor: ExplorerInteractor,
    private val apkInteractor: ApkInteractor,
    private val utils: UtilService,
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
            CLONE -> {
                val target = data?.items?.firstOrNull() ?: return null
                val parent = explorerStore.currentItems
                    .find { it.path == target.parentPath }
                    ?: return null
                cloneDelegate.getView(parent, target, inflater)
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
            Operations.Duplicate.id -> controller?.showNext(CLONE)
            Operations.Create.id -> controller?.showNext(CREATE)
            Operations.Rename.id -> controller?.showNext(RENAME)
            Operations.Delete.id -> onRemoveConfirm(items)
            Operations.Share.id -> router.shareWith(items.filter { it.isFile })
            Operations.OpenWith.id -> router.openWith(items.first())
            Operations.InstallApp.id -> apkInteractor.install(items.first(), viewState.currentTab.value)
            Operations.LaunchApp.id -> apkInteractor.launch(items.first())
            Operations.UseAs.id -> utils.useAs(options.items.first())
            Operations.CopyPath.id -> {
                interactor.copyToClipboard(items.first())
                if (Android.Below.T) controller?.showSnackbar(R.string.copied)
            }
        }
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
        interactor.rename(currentTab, item, name)
    }

    private fun onRemoveConfirm(items: List<Node>) {
        controller?.close(irrevocably = true)
        interactor.deleteItems(currentTab, items)
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