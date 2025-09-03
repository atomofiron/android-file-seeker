package app.atomofiron.searchboxapp.screens.common

import androidx.fragment.app.Fragment

interface ActivityModeProvider {
    val activityMode: ActivityMode
}

val Fragment.activityMode: ActivityMode get() = (activity as? ActivityModeProvider)
    ?.activityMode ?: ActivityMode.Default
