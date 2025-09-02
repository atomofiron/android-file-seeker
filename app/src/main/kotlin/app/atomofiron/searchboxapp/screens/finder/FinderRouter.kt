package app.atomofiron.searchboxapp.screens.finder

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.arch.Registerable
import app.atomofiron.common.util.permission.PermissionDelegate
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import app.atomofiron.searchboxapp.screens.root.RootRoutingModel

class FinderRouter(property: WeakProperty<out Fragment>) : BaseRouter(property), Registerable {

    override val currentDestinationId = R.id.rootFragment

    private val routingModel = property.value?.let { RootRoutingModel(it) }

    val permissions = PermissionDelegate.create(activityProperty)

    override fun register() {
        fragment {
            permissions.register(this)
        }
    }

    fun showResult(taskId: Int) = navigate(R.id.resultFragment, ResultPresenterParams.arguments(taskId))

    fun hide() {
        routingModel?.hideSearch()
    }
}