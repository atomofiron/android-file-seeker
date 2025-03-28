package app.atomofiron.searchboxapp.screens.result.presenter

import android.view.LayoutInflater
import app.atomofiron.common.arch.Recipient
import app.atomofiron.common.util.AlertMessage
import app.atomofiron.common.util.flow.collect
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.interactor.ResultInteractor
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.curtain.OptionsDelegate
import app.atomofiron.searchboxapp.screens.result.ResultRouter
import app.atomofiron.searchboxapp.screens.result.ResultViewState
import kotlinx.coroutines.CoroutineScope

class ResultCurtainMenuDelegate(
    scope: CoroutineScope,
    private val viewState: ResultViewState,
    private val router: ResultRouter,
    private val interactor: ResultInteractor,
    appStore: AppStore,
    curtainChannel: CurtainChannel,
) : Recipient, CurtainApi.Adapter<CurtainApi.ViewHolder>(), MenuListener {

    private val optionsDelegate = OptionsDelegate(R.menu.item_options, this)
    override var data: ExplorerItemOptions? = null

    init {
        curtainChannel.flow.filterForMe().collect(scope, ::setController)
    }

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder? {
        val data = data ?: return null
        val view = optionsDelegate.getView(data, inflater)
        return CurtainApi.ViewHolder(view)
    }

    override fun onMenuItemSelected(id: Int) {
        val data = data ?: return
        controller?.close()
        val items = data.items
        when (id) {
            R.id.menu_copy_path -> {
                interactor.copyToClipboard(items.first())
                viewState.showAlert(AlertMessage(R.string.copied))
            }
            R.id.menu_open_with -> router.openWith(items.first())
            R.id.menu_share -> router.shareWith(items.first())
            R.id.menu_delete -> interactor.deleteItems(items)
        }
    }

    fun showOptions(options: ExplorerItemOptions) {
        data = options
        router.showCurtain(recipient, 0)
    }
}