package app.atomofiron.searchboxapp.screens.viewer

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.R

class TextViewerRouter(property: WeakProperty<out Fragment>) : BaseRouter(property) {
    override val currentDestinationId = R.id.textViewerFragment
}