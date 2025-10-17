package app.atomofiron.common.util.flow

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException

class WaitingGate {

    private val channel = Channel<Unit>(capacity = Channel.RENDEZVOUS)

    suspend fun await() {
        try {
            channel.receive()
        } catch (_: ClosedReceiveChannelException) {
        }
    }

    fun finish() {
        channel.close()
    }
}
