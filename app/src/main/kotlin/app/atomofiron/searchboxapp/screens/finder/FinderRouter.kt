package app.atomofiron.searchboxapp.screens.finder

import android.os.Build
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.permission.PermissionDelegate
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.android.Intents
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

    fun showSystemPermissionsAppSettings() {
        activity {
            when {
                Build.VERSION.SDK_INT >= Android.R -> startActivity(Intents.storagePermissionIntent)
                else -> startActivity(Intents.settingsIntent)
            }
        }
    }
}