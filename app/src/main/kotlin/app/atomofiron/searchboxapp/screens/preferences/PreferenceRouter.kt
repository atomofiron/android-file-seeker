package app.atomofiron.searchboxapp.screens.preferences

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.android.Intents

class PreferenceRouter(fragmentProperty: WeakProperty<out Fragment>) : BaseRouter(fragmentProperty) {
    override val currentDestinationId = R.id.preferenceFragment

    fun goToGithub() = context { startActivity(Intents.github) }

    fun goToForPda() = context { startActivity(Intents.forPda) }
}