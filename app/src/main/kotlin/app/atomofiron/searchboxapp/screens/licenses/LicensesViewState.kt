package app.atomofiron.searchboxapp.screens.licenses

import app.atomofiron.searchboxapp.screens.licenses.state.License
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@LicensesScope
class LicensesViewState @Inject constructor() {

    val items: StateFlow<List<License>>
        field = MutableStateFlow(emptyList())

    fun set(licenses: List<License>) {
        items.value = licenses
    }
}
