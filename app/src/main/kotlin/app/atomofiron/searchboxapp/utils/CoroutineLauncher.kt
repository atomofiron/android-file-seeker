package app.atomofiron.searchboxapp.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

interface CoroutineLauncher {
    fun launch(block: suspend CoroutineScope.() -> Unit): Job
    fun default(block: suspend CoroutineScope.() -> Unit): Job
    fun io(block: suspend CoroutineScope.() -> Unit): Job
    fun main(immediate: Boolean = false, block: suspend CoroutineScope.() -> Unit): Job
    fun <T> Flow<T>.collectOn(context: CoroutineContext = Dispatchers.Default, block: suspend (T) -> Unit): Job

    companion object {
        operator fun invoke(scope: CoroutineScope): CoroutineLauncher = Launcher(scope)
    }
}

private class Launcher(private val scope: CoroutineScope) : CoroutineLauncher {

    override fun launch(
        block: suspend CoroutineScope.() -> Unit,
    ) = scope.launch(block = block)

    override fun default(
        block: suspend CoroutineScope.() -> Unit,
    ) = scope.launch(Dispatchers.Default, block = block)

    override fun io(
        block: suspend CoroutineScope.() -> Unit,
    ) = scope.launch(Dispatchers.IO, block = block)

    override fun main(
        immediate: Boolean,
        block: suspend CoroutineScope.() -> Unit,
    ) = scope.launch(if (immediate) Dispatchers.Main.immediate else Dispatchers.Main, block = block)

    override fun <T> Flow<T>.collectOn(
        context: CoroutineContext,
        block: suspend (T) -> Unit,
    ): Job = default {
        collect(block)
    }
}