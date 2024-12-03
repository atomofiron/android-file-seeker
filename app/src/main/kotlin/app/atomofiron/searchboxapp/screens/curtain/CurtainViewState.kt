package app.atomofiron.searchboxapp.screens.curtain

import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.DataFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainAction
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainPresenterParams
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class CurtainViewState(
    private val params: CurtainPresenterParams,
    val scope: CoroutineScope,
) {

    val initialLayoutId = params.layoutId
    val adapter = DataFlow<CurtainApi.Adapter<*>>()
    val action = ChannelFlow<CurtainAction>()
    val cancelable = MutableStateFlow(true)

    fun setCurtainAdapter(factory: CurtainApi.Adapter<*>) {
        adapter[scope] = factory
    }
}