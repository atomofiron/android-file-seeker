package app.atomofiron.searchboxapp.screens.finder.state

import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.finder.FinderScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@FinderScope
class CharsetProvider @Inject constructor(
    preferences: PreferenceStore,
) {

    val charset: StateFlow<String>
        field = MutableStateFlow(preferences.searchCharset.value)

    fun setCharset(name: String) {
        charset.value = name
    }
}