package app.atomofiron.searchboxapp.screens.licenses

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.arch.Recipient
import app.atomofiron.common.util.extension.launchOnDefault
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.android.WebClient
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.licenses.delegate.LicenseCurtainDelegate
import app.atomofiron.searchboxapp.screens.licenses.di.LicensesService
import app.atomofiron.searchboxapp.screens.licenses.state.License
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@LicensesScope
class LicensesPresenter @Inject constructor(
    scope: CoroutineScope,
    viewState: LicensesViewState,
    router: LicensesRouter,
    curtains: CurtainChannel,
    service: LicensesService,
    private val webClient: WebClient,
) : BasePresenter<LicensesViewModel, LicensesRouter>(scope, router), Recipient {

    private var license: License? = null

    init {
        curtains.flow.collectForMe(scope) { controller ->
            controller ?: return@collectForMe
            val license = license ?: return@collectForMe
            val adapter: CurtainApi.Adapter<*> = when (controller.requestId) {
                R.layout.curtain_license -> LicenseCurtainDelegate(webClient, license)
                else -> return@collectForMe
            }
            adapter.setController(controller)
        }
        scope.launchOnDefault {
            val licenses = service.readLicences()
            viewState.set(licenses)
        }
    }

    override fun onSubscribeData() = Unit

    fun onLicenseClick(license: License) {
        this.license = license
        router.showCurtain(recipient, R.layout.curtain_license)
    }
}
