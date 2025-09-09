package app.atomofiron.searchboxapp.screens.result.presenter

import android.view.LayoutInflater
import app.atomofiron.common.arch.Recipient
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.flow.collect
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.interactor.ApkInteractor
import app.atomofiron.searchboxapp.di.dependencies.interactor.ResultInteractor
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.common.delegates.Operations
import app.atomofiron.searchboxapp.screens.explorer.curtain.OptionsDelegate
import app.atomofiron.searchboxapp.screens.result.ResultRouter
import kotlinx.coroutines.CoroutineScope

class ResultCurtainMenuDelegate(
    scope: CoroutineScope,
    private val router: ResultRouter,
    private val interactor: ResultInteractor,
    private val apks: ApkInteractor,
    curtainChannel: CurtainChannel,
    private val utils: UtilService,
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

    override fun onMenuItemSelected(id: Int) {
        val data = data ?: return
        val items = data.items
        when (id) {
            Operations.CopyPath.id -> {
                interactor.copyToClipboard(items.first())
                if (Android.Below.T) controller?.showSnackbar(R.string.copied)
            }
            Operations.OpenWith.id -> router.openWith(items.first())
            Operations.Share.id -> router.shareWith(items.filter { it.isFile })
            Operations.Delete.id -> {
                controller?.close(irrevocably = true)
                interactor.deleteItems(items)
            }
            Operations.InstallApp.id -> apks.install(items.first())
            Operations.LaunchApp.id -> apks.launch(items.first())
            Operations.UseAs.id -> utils.useAs(items.first())
        }
    }

    fun showOptions(options: ExplorerItemOptions) {
        data = options
        router.showCurtain(recipient, 0)
    }
}