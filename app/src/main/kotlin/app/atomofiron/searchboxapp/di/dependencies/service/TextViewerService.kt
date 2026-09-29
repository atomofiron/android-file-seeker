package app.atomofiron.searchboxapp.di.dependencies.service

import app.atomofiron.common.util.extension.indexOfFirst
import app.atomofiron.common.util.extension.invoke
import app.atomofiron.common.util.extension.launchOnDefault
import app.atomofiron.common.util.extension.launchOnIO
import app.atomofiron.searchboxapp.android.NativeBridge
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.di.dependencies.store.SupportedCharsets
import app.atomofiron.searchboxapp.di.dependencies.store.TextViewerStore
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.ItemMatch
import app.atomofiron.searchboxapp.model.finder.LocalSearchResult
import app.atomofiron.searchboxapp.model.finder.LocalSearchTask
import app.atomofiron.searchboxapp.model.finder.QueryParams
import app.atomofiron.searchboxapp.model.finder.SearchTask
import app.atomofiron.searchboxapp.model.finder.SearchType
import app.atomofiron.searchboxapp.model.textviewer.MutableMatchMap
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.screens.viewer.TextViewerScope
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toNodeError
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.ifOk
import app.atomofiron.searchboxapp.utils.map
import app.atomofiron.searchboxapp.utils.removeOne
import kotlinx.coroutines.CoroutineScope
import uniffi.native_lib.CancellationState
import uniffi.native_lib.TextSearchProgress
import javax.inject.Inject
import kotlin.math.max
import kotlin.uuid.Uuid

@TextViewerScope
class TextViewerService @Inject constructor(
    private val scope: CoroutineScope,
    private val preferences: PreferenceStore,
    private val store: TextViewerStore,
    private val finderStore: FinderStore,
    private val charsets: SupportedCharsets,
) {
    private val NotCancelable = object : CancellationState { // todo make cancelable
        override fun cancelled(): Boolean = false
    }
    private val asSu: Boolean get() = preferences.asSu.value
    private var localId = 1

    fun getFileSession(ref: NodeRef, length: ULong, charset: String?): Rslt<TextViewerSession> {
        return findSession(ref)
            ?.apply { scope.launchOnIO { setCharset(charset) } }
            ?.let { Rslt.Ok(it) }
            ?: NativeBridge.readFile(ref, asSu)
                .map { TextViewerSession(it, length, ref, charsets, charset) }
                .ifOk {
                    store.sessions[ref.uniqueId] = it
                    scope.launchOnIO { readFile(ref) }
                }
    }

    suspend fun syncTasks(ref: NodeRef, taskId: Uuid?): LocalSearchTask? {
        val session = findSession(ref)
        session ?: return null
        val tasks = finderStore.tasks.mapNotNull { task ->
            val result = task.result
            val item = result.matches
                .find { it.uniqueId == ref.uniqueId }
                    as? ItemMatch.Many
                ?: return@mapNotNull null
            val type = result.type as SearchType.Text
            val local = LocalSearchResult(item.count, item.matches, type.charset, hash = item.hash, removable = false, error = task.error)
            LocalSearchTask(task.query, result = local, task.uuid, uniqueId = localId++, status = task.status, cached = task.cached)
        }
        session.tasks {
            clear()
            addAll(tasks)
        }
        return tasks
            .takeIf { taskId != null }
            ?.find { it.uuid == taskId }
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

    suspend fun removeTask(ref: NodeRef, taskId: Int) {
        findSession(ref)?.tasks {
            removeOne { it.uniqueId == taskId }
        }
    }

    suspend fun search(ref: NodeRef, params: QueryParams) {
        val session = findSession(ref) ?: return
        val uuid = SearchTask(params, LocalSearchResult(params.charset), uniqueId = localId++)
            .also { session.tasks { add(it) } }
            .uuid
        val progress = NativeBridge.findLocalText(params, ref, params.charset, asSu, NotCancelable)
        session.update(uuid) {
            when (progress) {
                is TextSearchProgress.Match -> {
                    val map: MutableMatchMap = hashMapOf()
                    progress.v3.forEach {
                        val index = it.line.toInt()
                        map.getOrPut(index) { mutableListOf() }.add(it)
                    }
                    toEnded(result = result.copy(count = progress.v3.size, matches = map))
                }
                is TextSearchProgress.Skip -> toEnded()
                is TextSearchProgress.Err -> toEnded(result = result.copy(error = progress.v1.error?.toNodeError()))
            }
        }
    }

    fun setCharset(ref: NodeRef, name: String?) {
        scope {
            findSession(ref)?.let { session ->
                if (session.setCharset(name)) {
                    session.readNextLines(Const.TEXT_FILE_PAGINATION_STEP)
                }
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

    private inline fun TextViewerSession.update(uuid: Uuid, crossinline action: LocalSearchTask.() -> LocalSearchTask) {
        scope.launchOnDefault {
            tasks {
                val index = indexOfFirst { it.uuid == uuid }
                if (index < 0) return@tasks
                set(index, get(index).action())
            }
        }
    }
}