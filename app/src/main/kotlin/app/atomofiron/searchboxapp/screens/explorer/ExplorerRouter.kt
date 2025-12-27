package app.atomofiron.searchboxapp.screens.explorer

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.arch.Registerable
import app.atomofiron.common.util.permission.PermissionDelegate
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.router.FileSharingDelegate
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.screens.common.RootRoutingModel
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
import javax.inject.Inject

@ExplorerScope
class ExplorerRouter @Inject constructor(
    property: WeakProperty<out Fragment>,
    private val sharing: FileSharingDelegate,
) : BaseRouter(property), Registerable {

    override val currentDestinationId = R.id.rootFragment

    private val routingModel = property.value?.let { RootRoutingModel(it) }

    val permissions = PermissionDelegate(activityProperty)

    override fun register() {
        fragment {
            permissions.register(this)
        }
    }

    fun showFinder() {
        routingModel?.showSearch()
    }

    fun showSettings() = navigate(R.id.preferenceFragment)

    fun showFile(item: Node) {
        if (item.content is NodeContent.Text) {
            val arguments = TextViewerParams.arguments(item.ref)
            navigate(R.id.textViewerFragment, arguments)
        } else {
            sharing.openWith(item)
        }
    }
}