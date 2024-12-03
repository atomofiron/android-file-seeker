package app.atomofiron.common.util.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class ChannelFlow<T>(channel: Channel<T> = Channel()) : Channel<T> by channel, Flow<T> by channel.receiveAsFlow()

operator fun Channel<Unit>.invoke(scope: CoroutineScope) {
    this[scope] = Unit
}

operator fun <T> Channel<T>.set(scope: CoroutineScope, value: T) {
    scope.launch { send(value) }
}
