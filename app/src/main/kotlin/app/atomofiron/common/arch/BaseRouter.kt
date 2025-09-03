package app.atomofiron.common.arch

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.common.util.dialog.DialogDelegateImpl
import app.atomofiron.common.util.property.RoProperty
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.injectable.router.FileSharingDelegate
import app.atomofiron.searchboxapp.injectable.router.FileSharingDelegateImpl
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainPresenterParams

abstract class BaseRouter(
    fragmentProperty: RoProperty<out Fragment?>,
    protected val activityProperty: RoProperty<out FragmentActivity?> = fragmentProperty.map { it?.requireActivity() },
) : FileSharingDelegate by FileSharingDelegateImpl(activityProperty)
    , DialogDelegate by DialogDelegateImpl(activityProperty)
{
    companion object {
        const val RECIPIENT = "RECIPIENT"

        val navOptions: NavOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.fragment_enter_scale)
            .setExitAnim(R.anim.fragment_nothing)
            .setPopEnterAnim(R.anim.fragment_nothing)
            .setPopExitAnim(R.anim.fragment_exit_scale)
            .setLaunchSingleTop(true)
            .build()

        val navExitOptions: NavOptions
            get() = NavOptions.Builder()
                .setPopEnterAnim(R.anim.fragment_nothing)
                .setPopExitAnim(R.anim.fragment_exit_scale)
                .setLaunchSingleTop(true)
                .build()

        val curtainOptions: NavOptions get() = NavOptions.Builder().setLaunchSingleTop(true).build()
    }

    protected abstract val currentDestinationId: Int

    constructor(activityProperty: WeakProperty<out FragmentActivity>) : this(WeakProperty(), activityProperty)

    protected open val isCurrentDestination: Boolean
        get() = navigation {
            currentDestination?.id == currentDestinationId || currentDestination?.id == R.id.curtainFragment
        } == true

    val isWrongDestination: Boolean get() = !isCurrentDestination

    val fragment: Fragment? by fragmentProperty

    val activity: FragmentActivity? by activityProperty

    val context: Context? get() = fragment?.context ?: activity

    inline fun <R> fragment(action: Fragment.() -> R): R? = fragment?.run(action)

    inline fun <R> activity(action: FragmentActivity.() -> R): R? = activity?.run(action)

    inline fun <R> context(action: Context.() -> R): R? = context?.run(action)

    inline fun <T> navigation(action: NavController.() -> T): T? {
        return activity?.run {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navHostFragment.findNavController().run(action)
        }
    }

    open fun navigateBack(): Boolean {
        var navigated = false
        navigation {
            navigated = navigateUp()
        }
        return navigated
    }

    fun minimize() {
        activity?.moveTaskToBack(true)
    }

    fun finish() {
        activity?.finish()
    }

    fun navigate(actionId: Int, args: Bundle? = null, navOptions: NavOptions = Companion.navOptions) {
        navigation {
            if (isCurrentDestination) {
                navigate(actionId, args, navOptions)
            }
        }
    }

    fun showCurtain(recipient: Int, layoutId: Int) {
        navigation {
            val args = CurtainPresenterParams.args(recipient, layoutId)
            navigate(R.id.curtainFragment, args, curtainOptions)
        }
    }

    protected fun List<Fragment>?.findLastVisibleFragment() = this
        ?.filter { it is BaseFragment<*,*,*,*> }
        ?.run { lastOrNull { it.isVisible } ?: lastOrNull { !it.isHidden } }
            as? BaseFragment<*,*,*,*>
}