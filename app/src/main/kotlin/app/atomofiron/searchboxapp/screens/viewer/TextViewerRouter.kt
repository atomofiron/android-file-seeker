package app.atomofiron.searchboxapp.screens.viewer

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import javax.inject.Inject

@TextViewerScope
class TextViewerRouter @Inject constructor(
    property: WeakProperty<out Fragment>,
) : BaseRouter(property) {

    override val currentDestinationId = R.id.textViewerFragment
}