package app.atomofiron.searchboxapp.di.dependencies.service

import app.atomofiron.common.util.extension.indexOfFirst
import app.atomofiron.common.util.extension.launchOnDefault
import app.atomofiron.searchboxapp.android.NativeBridge
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.di.dependencies.store.TextViewerStore
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.ItemMatch
import app.atomofiron.searchboxapp.model.finder.QueryParams
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.SearchResult.Text
import app.atomofiron.searchboxapp.model.finder.SearchState
import app.atomofiron.searchboxapp.model.finder.SearchTask
import app.atomofiron.searchboxapp.model.finder.TextSearchTask
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExplorerUtils.update
import app.atomofiron.searchboxapp.utils.removeOneIf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniffi.native_lib.SimpleResult
import uniffi.native_lib.TextSearchProgress
import java.util.UUID

class TextViewerService(
    private val scope: CoroutineScope,
    private val preferences: PreferenceStore,
    private val store: TextViewerStore,
    private val explorerStore: ExplorerStore,
    private val finderStore: FinderStore,
) {
    private val asSu: Boolean get() = preferences.asSu.value

    fun getFileSession(ref: NodeRef): TextViewerSession {
        val item = explorerStore.currentItems.find { it.ref == ref }
            ?: Node(ref, content = NodeContent.Undefined)
        var session = findSession(item)
        if (session == null) {
            session = TextViewerSession(item)
            store.sessions[item.uniqueId] = session
            scope.launch(Dispatchers.IO) { readFile(item) }
        }
        if (!item.isCached) {
            scope.launch(Dispatchers.IO) {
                val config = CacheConfig(asSu, thumbnailSize = 0)
                session.item.value = item.update(config)
            }
        }
        return session
    }

    suspend fun fetchTask(item: Node, taskId: UUID): TextSearchTask? {
        val finderTask = finderStore.tasks.find { it.uuid == taskId }
        finderTask ?: return null
        val session = findSession(item)
        session ?: return null
        val result = finderTask.result as SearchResult.Files
        val itemMatch = result.matches.find {
            it.item.uniqueId == item.uniqueId
        } as? ItemMatch.Multiply
        itemMatch ?: return null
        val task = SearchTask(
            finderTask.query,
            Text(itemMatch.count, itemMatch.matchesMap),
            finderTask.uuid,
            SearchState.Ended(removable = false),
        )
        session.tasks { add(task) }
        return task
    }

    /** @return true if success */
    suspend fun readFile(item: Node, targetLineIndex: Int = 0, callback: ((Boolean) -> Unit)? = null) {
        val session = findSession(item)
        if (session == null) {
            callback?.invoke(false)
            return
        }
        val paginationThreshold = session.lines.value.size - Const.TEXT_FILE_PAGINATION_STEP_OFFSET
        when {
            session.loading.value -> callback?.invoke(false)
            session.isFullyRead -> callback?.invoke(false)
            targetLineIndex < paginationThreshold -> callback?.invoke(false)
            else -> session.readNextLines()
        }
        callback?.invoke(true)
    }

    fun closeSession(item: Node) {
        val session = store.sessions.remove(item.uniqueId)
        session?.reader?.close()
    }

    suspend fun removeTask(item: Node, taskId: Int) {
        findSession(item)?.tasks {
            removeOneIf { it.uniqueId == taskId }
        }
    }

    suspend fun search(item: Node, params: QueryParams) {
        val session = findSession(item) ?: return
        val uuid = SearchTask(params, Text()).also {
            session.tasks { add(it) }
        }.uuid
        var count = 0
        val matchesMap = hashMapOf<Int, MutableList<TextLineMatch>>()
        val result = NativeBridge.findLocalText(params, item.ref, asSu) { match ->
            session.update(uuid) {
                when (match) {
                    is TextSearchProgress.Ok -> {
                        val lineIndex = match.line?.toInt() ?: return@update this
                        val list = matchesMap.getOrPut(lineIndex) { mutableListOf() }
                        list.add(TextLineMatch(match.offset.toLong(), match.length.toInt()))
                        copy(result = Text(++count, matchesMap))
                    }
                    is TextSearchProgress.Err -> toEnded(error = match.v1.error)
                    is TextSearchProgress.End -> toEnded()
                }
            }
        }
        session.update(uuid) {
            toEnded(error = (result as? SimpleResult.Err)?.v1)
        }
    }

    private fun findSession(item: Node): TextViewerSession? = store.sessions[item.uniqueId]

    private suspend fun TextViewerSession.readNextLines() {
        val reader = reader ?: return
        textLines {
            loading.value = true
            var isFullyRead = isFullyRead
            val lines = ArrayList<TextLine>(Const.TEXT_FILE_PAGINATION_STEP)
            var byteOffset = lastOrNull()?.run { byteOffset + byteCount.inc() } ?: 0
            while (lines.size < Const.TEXT_FILE_PAGINATION_STEP) {
                when (val stringOrNull = reader.readLine()) {
                    null -> {
                        isFullyRead = true
                        break
                    }
                    else -> {
                        val byteCount = stringOrNull.toByteArray().size
                        lines.add(TextLine(byteOffset, byteCount, stringOrNull))
                        byteOffset += byteCount.inc() // \n
                    }
                }
            }
            addAll(lines)
            loading.value = false
            isFullyRead
        }
    }

    private inline fun TextViewerSession.update(uuid: UUID, crossinline action: TextSearchTask.() -> TextSearchTask) {
        scope.launchOnDefault {
            tasks {
                val index = indexOfFirst { it.uuid == uuid }
                if (index < 0) return@tasks
                set(index, get(index).action())
            }
        }
    }
}