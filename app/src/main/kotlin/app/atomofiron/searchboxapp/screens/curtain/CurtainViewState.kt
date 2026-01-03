package app.atomofiron.searchboxapp.screens.curtain

import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.common.util.flow.LateinitDataFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainAction
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainParams
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@CurtainScope
class CurtainViewState @Inject constructor(
    params: CurtainParams,
    val scope: CoroutineScope,
) {

    val initialId = params.id
    val adapter = LateinitDataFlow<CurtainApi.Adapter<*>>()
    val action = EventFlow<CurtainAction>()
    val cancelable = MutableStateFlow(true)

    fun setCurtainAdapter(factory: CurtainApi.Adapter<*>) {
        adapter[scope] = factory
    }
}