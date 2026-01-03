package app.atomofiron.searchboxapp.di.dependencies.store

import app.atomofiron.searchboxapp.model.explorer.NodeHash
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object ApkInfoCache {

    private val cache = mutableMapOf<NodeHash, ApkInfo>()
    private val mutex = Mutex()

    fun get(
        hash: NodeHash,
        withIcon: Boolean,
        withSignature: Boolean,
    ): ApkInfo? = cache[hash]?.takeIf { (!withIcon || it.withIcon) && (!withSignature || it.withSignature) }

    suspend fun offer(
        hash: NodeHash,
        withIcon: Boolean,
        withSignature: Boolean,
        info: ApkInfo,
    ) {
        val cached = get(hash, withIcon, withSignature)
        when {
            cached == null -> Unit
            cached.icon == null && info.icon != null -> Unit
            cached.signature == null && info.signature != null -> Unit
            cached.withIcon != info.withIcon -> Unit
            cached.withSignature != info.withSignature -> Unit
            else -> return
        }
        val icon = info.icon ?: cached?.icon
        val signature = info.signature ?: cached?.signature
        mutex.withLock {
            cache[hash] = info.copy(
                icon = icon,
                signature = signature,
                withIcon = icon != null || withIcon || cached?.withIcon == true,
                withSignature = signature != null || withSignature || cached?.withSignature == true,
            )
        }
    }
}