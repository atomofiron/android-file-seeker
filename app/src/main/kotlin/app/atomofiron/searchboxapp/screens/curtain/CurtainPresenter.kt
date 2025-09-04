package app.atomofiron.searchboxapp.screens.curtain

import com.google.android.material.snackbar.Snackbar
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.set
import app.atomofiron.common.util.flow.valueOrNull
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainResponse
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainAction
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainPresenterParams
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi

class CurtainPresenter(
    private val params: CurtainPresenterParams,
    private val viewState: CurtainViewState,
    router: CurtainRouter,
    private val curtainChannel: CurtainChannel,
) : BasePresenter<CurtainViewModel, CurtainRouter>(viewState.scope, router), CurtainApi.Controller {
    private var adapter: CurtainApi.Adapter<*>? = null

    override val requestFrom: Int = params.recipient
    override val requestId: Int = params.layoutId

    init {
        curtainChannel.emit(CurtainResponse(params.recipient, this))
    }

    override fun onSubscribeData() = Unit

    override fun onCleared() {
        adapter?.clear()
        curtainChannel.emit(CurtainResponse(params.recipient, null))
    }

    override fun setAdapter(adapter: CurtainApi.Adapter<*>) {
        viewState.setCurtainAdapter(adapter)
        this.adapter?.clear()
        this.adapter = adapter
    }

    override fun showNext(layoutId: Int) {
        viewState.action[scope] = CurtainAction.ShowNext(layoutId)
    }

    override fun showPrev() {
        viewState.action[scope] = CurtainAction.ShowPrev
    }

    override fun close(immediately: Boolean, irrevocably: Boolean) {
        when {
            immediately -> router.navigateBack()
            else -> viewState.action[scope] = CurtainAction.Hide(irrevocably)
        }
    }

    override fun showSnackbar(string: String, duration: Int) = showSnackbar {
        Snackbar.make(it, string, duration)
    }

    override fun showSnackbar(stringId: Int, duration: Int) = showSnackbar {
        Snackbar.make(it, stringId, duration)
    }

    override fun showSnackbar(provider: CurtainApi.SnackbarProvider) {
        viewState.action[scope] = CurtainAction.ShowSnackbar(provider)
    }

    override fun setCancelable(value: Boolean) = viewState.cancelable.set(value)

    fun onShown() {
        viewState.adapter.valueOrNull ?: router.navigateBack()
    }

    fun onHidden() = router.navigateBack()

    fun onNullViewGot() = router.navigateBack()
}