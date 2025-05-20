package app.atomofiron.common.util.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun withMain(action: CoroutineScope.() -> Unit) = withContext(Dispatchers.Main, action)


inline infix fun <T> Boolean.then(action: () -> T): T? {
    return if (this) action() else null
}

@Suppress("NOTHING_TO_INLINE")
inline infix fun <T> Boolean.then(value: T): T? {
    return value.takeIf { this }
}