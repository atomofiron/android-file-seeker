package app.atomofiron.searchboxapp.screens.licenses

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import javax.inject.Inject

@LicensesScope
class LicensesRouter @Inject constructor(
    property: WeakProperty<out Fragment>,
) : BaseRouter(property) {

    override val currentDestinationId = R.id.licensesFragment
}