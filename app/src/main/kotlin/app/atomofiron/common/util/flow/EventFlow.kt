package app.atomofiron.common.util.flow

import kotlinx.coroutines.flow.MutableSharedFlow

@Suppress("FunctionName")
fun <T> EventFlow(): MutableSharedFlow<T> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)