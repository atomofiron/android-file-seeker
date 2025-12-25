package app.atomofiron.searchboxapp.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface CoroutineLauncher {

    val scope: CoroutineScope

    fun launch(
        block: suspend CoroutineScope.() -> Unit,
    ) = scope.launch(block = block)

    fun default(
        block: suspend CoroutineScope.() -> Unit,
    ) = scope.launch(Dispatchers.Default, block = block)

    fun io(
        block: suspend CoroutineScope.() -> Unit,
    ) = scope.launch(Dispatchers.IO, block = block)

    fun main(
        immediate: Boolean = false,
        block: suspend CoroutineScope.() -> Unit,
    ) = scope.launch(if (immediate) Dispatchers.Main.immediate else Dispatchers.Main, block = block)
}