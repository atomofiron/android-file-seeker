package app.atomofiron.searchboxapp.screens.preferences

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.R

class PreferenceRouter(fragmentProperty: WeakProperty<out Fragment>) : BaseRouter(fragmentProperty) {
    override val currentDestinationId = R.id.preferenceFragment
}