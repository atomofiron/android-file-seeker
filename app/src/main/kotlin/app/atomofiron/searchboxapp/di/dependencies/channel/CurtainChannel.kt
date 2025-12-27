package app.atomofiron.searchboxapp.di.dependencies.channel

import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.model.Response
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

typealias CurtainResponse = Response<CurtainApi.Controller?>

@Singleton
class CurtainChannel @Inject constructor(
    private val scope: AppScope,
) {
    private val mutableFlow = EventFlow<CurtainResponse>()
    var flow: Flow<CurtainResponse> = mutableFlow

    fun emit(response: CurtainResponse) {
        mutableFlow[scope] = response
    }
}