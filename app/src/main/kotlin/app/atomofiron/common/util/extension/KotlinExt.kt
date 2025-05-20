package app.atomofiron.common.util.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun withMain(action: CoroutineScope.() -> Unit) = withContext(Dispatchers.Main, action)
