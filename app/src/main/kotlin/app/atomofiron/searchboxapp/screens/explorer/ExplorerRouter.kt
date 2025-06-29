package app.atomofiron.searchboxapp.screens.explorer

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.permission.PermissionDelegate
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.screens.finder.FinderFragment
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams

class ExplorerRouter(
    property: WeakProperty<out Fragment>,
) : BaseRouter(property) {

    override val currentDestinationId = R.id.rootFragment

    val permissions = PermissionDelegate.create(activityProperty)

    fun showFinder() {
        fragment {
            parentFragmentManager.switchScreen { it is FinderFragment }
        }
    }

    fun showSettings() = navigate(R.id.preferenceFragment)

    fun showFile(item: Node) {
        if (item.content is NodeContent.Text) {
            val arguments = TextViewerParams.arguments(item.path)
            navigate(R.id.textViewerFragment, arguments)
        } else {
            openWith(item)
        }
    }
}