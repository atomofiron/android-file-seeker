package app.atomofiron.searchboxapp.di.dependencies.service

import app.atomofiron.common.util.extension.indexOfFirst
import app.atomofiron.common.util.extension.launchOnDefault
import app.atomofiron.common.util.extension.launchOnIO
import app.atomofiron.searchboxapp.android.NativeBridge
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.di.dependencies.store.TextViewerStore
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.ItemMatch
import app.atomofiron.searchboxapp.model.finder.LocalSearchResult
import app.atomofiron.searchboxapp.model.finder.QueryParams
import app.atomofiron.searchboxapp.model.finder.SearchTask
import app.atomofiron.searchboxapp.model.finder.LocalSearchTask
import app.atomofiron.searchboxapp.model.textviewer.MutableMatchMap
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.screens.viewer.TextViewerScope
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toNodeError
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.ifOk
import app.atomofiron.searchboxapp.utils.map
import app.atomofiron.searchboxapp.utils.removeOneIf
import kotlinx.coroutines.CoroutineScope
import uniffi.native_lib.CancellationState
import uniffi.native_lib.TextSearchProgress
import java.util.UUID
import javax.inject.Inject
import kotlin.math.max

@TextViewerScope
class TextViewerService @Inject constructor(
    private val scope: CoroutineScope,
    private val preferences: PreferenceStore,
    private val store: TextViewerStore,
    private val finderStore: FinderStore,
) {
    private val NotCancelable = object : CancellationState { // todo make cancelable
        override fun cancelled(): Boolean = false
    }
    private val asSu: Boolean get() = preferences.asSu.value

    fun getFileSession(ref: NodeRef): Rslt<TextViewerSession> {
        return findSession(ref)
            ?.let { Rslt.Ok(it) }
            ?: NativeBridge.readFile(ref, asSu)
                .map { TextViewerSession(it, ref) }
                .ifOk {
                    store.sessions[ref.uniqueId] = it
                    scope.launchOnIO { readFile(ref) }
                }
    }

    suspend fun fetchTask(ref: NodeRef, taskId: UUID): LocalSearchTask? {
        val finderTask = finderStore.tasks.find { it.uuid == taskId }
        finderTask ?: return null
        val session = findSession(ref)
        session ?: return null
        val result = finderTask.result
        val item = result.matches.find {
            it.uniqueId == ref.uniqueId
        } as? ItemMatch.Many
        item ?: return null
        val local = LocalSearchResult(item.count, item.matches, item.hash)
        val task = LocalSearchTask(finderTask.query, result = local, finderTask.uuid, finderTask.status, finderTask.error)
        session.tasks { add(task) }
        return task
    }

    /** @return true if success */
    suspend fun readFile(ref: NodeRef, targetLineIndex: Int = 0, callback: ((Boolean) -> Unit)? = null) {
        val session = findSession(ref)
        if (session == null) {
            callback?.invoke(false)
            return
        }
        val count = max(Const.TEXT_FILE_PAGINATION_STEP, targetLineIndex.inc() - session.lines.value.size)
        val paginationThreshold = session.lines.value.size - Const.TEXT_FILE_PAGINATION_STEP_OFFSET
        when {
            session.loading.value -> callback?.invoke(false)
            session.isFullyRead -> callback?.invoke(false)
            targetLineIndex < paginationThreshold -> callback?.invoke(false)
            else -> session.readNextLines(count)
        }
        callback?.invoke(true)
    }

    fun closeSession(ref: NodeRef) {
        val session = store.sessions.remove(ref.uniqueId)
        session?.close()
    }

    suspend fun removeTask(ref: NodeRef, taskId: Int) {
        findSession(ref)?.tasks {
            removeOneIf { it.uniqueId == taskId }
        }
    }

    suspend fun search(ref: NodeRef, params: QueryParams) {
        val session = findSession(ref) ?: return
        val uuid = SearchTask(params, LocalSearchResult())
            .also { session.tasks { add(it) } }
            .uuid
        val result = NativeBridge.findLocalText(params, ref, asSu, NotCancelable)
        session.update(uuid) {
            when (result) {
                is TextSearchProgress.Match -> {
                    val map: MutableMatchMap = hashMapOf()
                    result.v3.forEach {
                        val index = it.line.toInt()
                        map.getOrPut(index) { mutableListOf() }.add(it)
                    }
                    toEnded(result = LocalSearchResult(result.v3.size, map, removable = false))
                }
                is TextSearchProgress.Skip -> toEnded()
                is TextSearchProgress.Err -> toEnded(error = result.v1.error?.toNodeError())
            }
        }
    }

    private fun findSession(ref: NodeRef): TextViewerSession? = store.sessions[ref.uniqueId]

    private suspend fun TextViewerSession.readNextLines(count: Int) {
        textLines {
            loading.value = true
            val lines = ArrayList<TextLine>(count)
            while (lines.size < count) {
                val line = readLine() ?: break
                lines.add(line)
            }
            addAll(lines)
            loading.value = false
        }
    }

    private inline fun TextViewerSession.update(uuid: UUID, crossinline action: LocalSearchTask.() -> LocalSearchTask) {
        scope.launchOnDefault {
            tasks {
                val index = indexOfFirst { it.uuid == uuid }
                if (index < 0) return@tasks
                set(index, get(index).action())
            }
        }
    }
}