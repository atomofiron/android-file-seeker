package app.atomofiron.searchboxapp.screens.viewer

import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.MuonsDrawable
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.finder.TextSearchTask
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsState
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsStateDelegate
import app.atomofiron.searchboxapp.screens.viewer.state.TextViewerDockState
import app.atomofiron.searchboxapp.utils.toInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TextViewerViewState(
    private val scope: CoroutineScope,
    private val session: TextViewerSession,
    preferenceStore: PreferenceStore,
) : FinderItemsState by FinderItemsStateDelegate(
    isLocal = true,
    preferenceStore,
    session.tasks,
) {

    data class Status(
        val loading: Boolean = false,
        val current: Int = 0,
        val max: Int = 0,
    ) {
        fun clear(): Status = copy(current = 0, max = 0)
        fun go(forward: Boolean): Status = copy(current = ((max + current.dec() + forward.toInt()) % max).inc())
    }

    @JvmInline
    value class MatchCursor(val value: Long = 0) {
        val lineIndex get() = value.shr(32).toInt()
        val lineMatchIndex get() = value.toInt()

        constructor(lineIndex: Int, matchIndex: Int = 0) : this(lineIndex.toLong().shl(32) + matchIndex.toLong())

        fun copy(lineIndex: Int = this.lineIndex, matchIndex: Int = this.lineMatchIndex): MatchCursor {
            return MatchCursor(lineIndex.toLong().shl(32) + matchIndex.toLong())
        }
    }

    val insertInQuery = ChannelFlow<String>()

    private val status = MutableStateFlow(Status())
    /** line index -> line match index */
    val matchesCursor = MutableStateFlow<MatchCursor?>(null)

    val composition = preferenceStore.explorerItemComposition.value
    val item = session.item
    val tasks = session.tasks
    val textLines = session.lines
    val currentTask = MutableStateFlow<TextSearchTask?>(null)

    val dock = status.map { state ->
        var index: Int? = null
        var count: Int? = null
        val label = if (state.max == 0) DockItem.Label.Empty else {
            index = state.current
            count = state.max
            DockItem.Label("$index / $count")
        }
        TextViewerDockState.Default.run {
            val status = when {
                state.loading -> status.with(MuonsDrawable())
                else -> status.with(R.drawable.ic_circle_check)
            }
            copy(
                status = status.copy(label = label, progress = state.loading),
                previous = previous.copy(enabled = !state.loading),
                next = next.copy(enabled = !state.loading),
            )
        }
    }

    /** @return value >= 0 если нужно подгрузить файл. */
    fun changeCursor(increment: Boolean): Int {
        val none = -1
        val cursor = matchesCursor.value
        val result = currentTask.value?.result
        result ?: return none
        val matches = result.matches
        val indexes = result.indexes
        if (cursor == null) {
            val statusIndex = when {
                increment -> 1
                indexes.last() < textLines.value.size -> status.value.max
                else -> return indexes.last()
            }
            status.update {
                it.copy(current = statusIndex)
            }
            val lineIndex = if (increment) indexes.first() else indexes.last()
            val matchIndex = if (increment) 0 else matches[lineIndex]?.lastIndex ?: 0
            matchesCursor.value = MatchCursor(lineIndex = lineIndex, matchIndex = matchIndex)
            return none
        }
        var lineIndex = cursor.lineIndex
        var matchIndex = cursor.lineMatchIndex

        if (increment) {
            matchIndex++
            val matches = matches[lineIndex] ?: return none
            if (matchIndex == matches.size) {
                var index = indexes.indexOf(lineIndex)
                index = index.inc() % indexes.size
                lineIndex = indexes[index]
                matchIndex = 0
            }
        } else {
            matchIndex--
            if (matchIndex < 0) {
                var index = indexes.indexOf(lineIndex)
                if (index < 0) return none
                index = indexes.run { (size + index.dec()) % size }
                lineIndex = indexes[index]
                matchIndex = matches[lineIndex]!!.lastIndex
            }
        }
        if (lineIndex > textLines.value.lastIndex) {
            return lineIndex
        }
        matchesCursor.value = MatchCursor(lineIndex, matchIndex)
        status.run {
            value = value.go(forward = increment)
        }
        return none
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
        status.value = status.value.clear()
        currentTask.value = null
        matchesCursor.value = null
    }

    fun trySelectTask(task: TextSearchTask): Boolean {
        return (task.isEnded && task.count > 0).also { isOk ->
            if (isOk) {
                currentTask.value = task
                status.run {
                    value = value.copy(current = 0, max = task.count)
                }
            }
        }
    }
}