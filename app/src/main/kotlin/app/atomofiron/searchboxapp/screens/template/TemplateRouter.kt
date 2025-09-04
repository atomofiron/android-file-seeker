package app.atomofiron.searchboxapp.screens.template

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore

class TemplateRouter(
    property: WeakProperty<out Fragment>,
    preferenceStore: PreferenceStore,
) : BaseRouter(property) {

    override val currentDestinationId = 0

}