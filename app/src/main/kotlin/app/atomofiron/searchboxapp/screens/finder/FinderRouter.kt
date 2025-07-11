package app.atomofiron.searchboxapp.screens.finder

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.permission.PermissionDelegate
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.explorer.ExplorerFragment
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams

class FinderRouter(fragment: WeakProperty<out Fragment>) : BaseRouter(fragment) {

    override val currentDestinationId = R.id.rootFragment

    val permissions = PermissionDelegate.create(activityProperty)

    fun showExplorer() {
        fragment {
            parentFragmentManager.switchScreen { it is ExplorerFragment }
        }
    }

    fun showSettings() = navigate(R.id.preferenceFragment)

    fun showResult(taskId: Int) = navigate(R.id.resultFragment, ResultPresenterParams.arguments(taskId))
}