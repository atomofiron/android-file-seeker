package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.model.Response
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

typealias CurtainResponse = Response<CurtainApi.Controller?>

class CurtainChannel(
    private val scope: CoroutineScope,
) {
    private val mutableFlow = EventFlow<CurtainResponse>()
    var flow: Flow<CurtainResponse> = mutableFlow

    fun emit(response: CurtainResponse) {
        mutableFlow[scope] = response
    }
}