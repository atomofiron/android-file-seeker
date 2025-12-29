package app.atomofiron.searchboxapp.screens.result.presenter

import android.view.LayoutInflater
import app.atomofiron.common.arch.Recipient
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.common.delegates.FileOperationDelegate
import app.atomofiron.searchboxapp.screens.common.delegates.Operations
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.curtain.OptionsDelegate
import app.atomofiron.searchboxapp.screens.result.ResultRouter
import app.atomofiron.searchboxapp.screens.result.ResultScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@ResultScope
class ResultCurtainMenuDelegate @Inject constructor(
    scope: CoroutineScope,
    private val router: ResultRouter,
    private val operations: FileOperationDelegate,
    curtainChannel: CurtainChannel,
) : Recipient, CurtainApi.Adapter<CurtainApi.ViewHolder>(), MenuListener {

    private val optionsDelegate = OptionsDelegate(this)
    override var data: ExplorerItemOptions? = null

    init {
        curtainChannel.flow.filterForMe().collect(scope, ::setController)
    }

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder? {
        val data = data ?: return null
        val view = optionsDelegate.getView(data, inflater)
        return CurtainApi.ViewHolder(view)
    }

    override fun onMenuItemSelected(item: MenuItem) {
        val data = data ?: return
        val targets = data.items
        val (alert, new) = operations.action(item, targets)
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
        new?.let { optionsDelegate.bind(it) }
    }

    fun showOptions(options: ExplorerItemOptions) {
        data = options
        router.showCurtain(recipient, 0)
    }
}