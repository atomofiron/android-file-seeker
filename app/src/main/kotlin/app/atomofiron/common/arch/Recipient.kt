package app.atomofiron.common.arch

import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.model.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

interface Recipient {
    val recipient: String get() = this::class.java.name // don't use simpleName because of minification

    fun <D> Flow<Response<D>>.filterForMe() = filter { it.recipient == recipient }.map { it.data }

    fun <D> Flow<Response<D>>.collectForMe(
        scope: CoroutineScope,
        collector: FlowCollector<D>,
    ) = filterForMe().collect(scope, collector)
}