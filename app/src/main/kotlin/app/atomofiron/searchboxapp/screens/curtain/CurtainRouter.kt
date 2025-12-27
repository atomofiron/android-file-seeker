package app.atomofiron.searchboxapp.screens.curtain

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import javax.inject.Inject

@CurtainScope
class CurtainRouter @Inject constructor(fragment: WeakProperty<out Fragment>) : BaseRouter(fragment) {

    override val currentDestinationId = 0
}