package ru.atomofiron.regextool.injectable.channel

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel

object RootChannel {
    val channel = BroadcastChannel<Int>(Channel.BUFFERED)
}