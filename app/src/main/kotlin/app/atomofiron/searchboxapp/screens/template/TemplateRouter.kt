package app.atomofiron.searchboxapp.screens.template

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import javax.inject.Inject

@TemplateScope
class TemplateRouter @Inject constructor(
    property: WeakProperty<out Fragment>,
) : BaseRouter(property) {

    override val currentDestinationId = 0
}