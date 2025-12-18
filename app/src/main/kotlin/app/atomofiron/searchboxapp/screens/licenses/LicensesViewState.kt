package app.atomofiron.searchboxapp.screens.licenses

import app.atomofiron.searchboxapp.screens.licenses.state.License
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@LicensesScope
class LicensesViewState @Inject constructor() {

    private val _items = MutableStateFlow<List<License>>(emptyList())
    val items: StateFlow<List<License>> = _items

    fun set(licenses: List<License>) {
        _items.value = licenses
    }
}
