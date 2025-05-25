package app.atomofiron.searchboxapp.screens.viewer

import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.MuonsDrawable
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.SearchTask
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsState
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsStateDelegate
import app.atomofiron.searchboxapp.screens.viewer.state.TextViewerDockState
import app.atomofiron.searchboxapp.utils.toInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class TextViewerViewState(
    private val scope: CoroutineScope,
    private val session: TextViewerSession,
    preferenceStore: PreferenceStore,
) : FinderItemsState by FinderItemsStateDelegate(isLocal = true) {

    data class Status(
        val loading: Boolean = false,
        val count: Int = 0,
        val countMax: Int = 0,
    ) {
        fun clear(): Status = copy(count = 0, countMax = 0)
    }

    @JvmInline
    value class MatchCursor(val value: Long = -1) {
        val isEmpty get() = value < 0
        val lineIndex get() = if (isEmpty) -1 else value.shr(32).toInt()
        val lineMatchIndex get() = if (isEmpty) -1 else value.toInt()

        constructor(lineIndex: Int, matchIndex: Int = 0) : this(lineIndex.toLong().shl(32) + matchIndex.toLong())

        fun copy(lineIndex: Int = this.lineIndex, matchIndex: Int = this.lineMatchIndex): MatchCursor {
            return MatchCursor(lineIndex.toLong().shl(32) + matchIndex.toLong())
        }
    }

    val insertInQuery = ChannelFlow<String>()

    private val status = MutableStateFlow(Status())
    /** line index -> line match index */
    val matchesCursor = MutableStateFlow(MatchCursor())

    val composition = preferenceStore.explorerItemComposition.value
    val item = session.item
    val tasks = session.tasks
    val textLines = session.textLines
    val currentTask = MutableStateFlow<SearchTask?>(null)

    val dock = status.map { state ->
        var index: Int? = null
        var count: Int? = null
        val label = if (state.countMax == 0) DockItem.Label.Empty else {
            index = state.count
            count = state.countMax
            DockItem.Label("$index / $count")
        }
        TextViewerDockState.Default.run {
            val status = when {
                state.loading -> status.with(MuonsDrawable())
                else -> status.with(R.drawable.ic_circle_check)
            }
            copy(
                status = status.copy(label = label, progress = state.loading),
                previous = previous.copy(enabled = !state.loading && index != null && index > 1),
                next = next.copy(enabled = !state.loading && count != null && index != count),
            )
        }
    }

    /** @return value >= 0 если нужно подгрузить файл. */
    fun changeCursor(increment: Boolean): Int {
        val none = -1
        val cursor = matchesCursor.value
        val result = currentTask.value?.result as SearchResult.TextSearchResult?
        result ?: return none
        val matchesMap = result.matchesMap
        val indexes = result.indexes
        if (cursor.isEmpty) {
            matchesCursor.value = MatchCursor(lineIndex = indexes.first(), matchIndex = 0)
            status.value = status.value.copy(count = 1)
            return none
        }
        var lineIndex = cursor.lineIndex
        var matchIndex = cursor.lineMatchIndex

        if (increment) {
            matchIndex++
            val matches = matchesMap[lineIndex] ?: return none
            if (matchIndex == matches.size) {
                val index = indexes.indexOf(lineIndex)
                if (index == indexes.lastIndex) return none
                lineIndex = indexes[index.inc()]
                matchIndex = 0
            }
        } else {
            matchIndex--
            if (matchIndex < 0) {
                val index = indexes.indexOf(lineIndex)
                if (index <= 0) return none
                lineIndex = indexes[index.dec()]
                matchIndex = matchesMap[lineIndex]!!.lastIndex
            }
        }
        if (lineIndex > textLines.value.lastIndex) {
            return lineIndex
        }
        matchesCursor.value = MatchCursor(lineIndex, matchIndex)
        status.run {
            value = value.copy(count = value.count + increment.toInt())
        }
        return none
    }

    fun setTasks(tasks: List<SearchTask>) {
        val items = tasks.map { FinderStateItem.ProgressItem(it) }
        progressItems.clear()
        progressItems.addAll(items)
        updateState()
    }

    fun sendInsertInQuery(value: String) {
        insertInQuery[scope] = value
    }

    fun setLoading(loading: Boolean) {
        status.run {
            value = value.copy(loading = loading)
        }
    }

    fun dropTask() {
        matchesCursor.value = MatchCursor()
        status.value = status.value.clear()
        currentTask.value = null
    }

    fun trySelectTask(task: SearchTask): Boolean {
        return (task.isEnded && task.count > 0).also { isOk ->
            if (isOk) {
                matchesCursor.value = MatchCursor()
                currentTask.value = task
                status.run {
                    value = value.copy(count = 0, countMax = task.count)
                }
            }
        }
    }
}