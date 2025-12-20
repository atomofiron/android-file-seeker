package app.atomofiron.searchboxapp.screens.viewer

import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.MuonsDrawable
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.TextSearchTask
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsState
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsStateDelegate
import app.atomofiron.searchboxapp.screens.viewer.state.MatchCursor
import app.atomofiron.searchboxapp.screens.viewer.state.CursorResult
import app.atomofiron.searchboxapp.screens.viewer.state.Status
import app.atomofiron.searchboxapp.screens.viewer.state.TextViewerDockState
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toNode
import app.atomofiron.searchboxapp.utils.toInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TextViewerViewState(
    ref: NodeRef,
    private val scope: CoroutineScope,
    private val session: TextViewerSession?,
    preferenceStore: PreferenceStore,
    error: String?,
) : FinderItemsState by FinderItemsStateDelegate(
    isLocal = true,
    preferenceStore,
    session?.tasks ?: emptyFlow(),
) {

    val insertInQuery = ChannelFlow<String>()

    private val status = MutableStateFlow(Status())
    /** line index -> line match index */
    val matchesCursor = MutableStateFlow<MatchCursor?>(null)

    val composition = preferenceStore.explorerItemComposition.value
    val item: StateFlow<Node> = session?.item ?: MutableStateFlow(ref.toNode())
    val tasks: StateFlow<List<TextSearchTask>> = session?.tasks ?: MutableStateFlow(emptyList())
    val textLines: StateFlow<List<TextLine>> = session?.lines ?: MutableStateFlow(emptyList())
    val currentTask = MutableStateFlow<TextSearchTask?>(null)
    val error = session?.error ?: MutableStateFlow(error) // todo

    val dock = status.map { state ->
        var index: Int? = null
        var count: Int? = null
        TextViewerDockState.Default.run {
            val navigation = state.max > 0
            val label = if (navigation) {
                index = state.current
                count = state.max
                DockItem.Label("$index / $count")
            } else if (state.loading) {
                DockItem.Label(R.string.loading)
            } else {
                DockItem.Label(R.string.status)
            }
            val status = when {
                state.loading -> status.with(MuonsDrawable())
                else -> status.with(R.drawable.ic_circle_check)
            }
            copy(
                status = status.copy(label = label, progress = state.loading),
                previous = previous.copy(enabled = !state.loading && navigation),
                next = next.copy(enabled = !state.loading && navigation),
            )
        }
    }

    fun changeCursor(increment: Boolean): CursorResult {
        val result = currentTask.value?.result
            ?: return CursorResult.Err("no search result")
        val cursor = matchesCursor.value
            ?: return startNavigation(result, increment)
        var lineIndex = cursor.lineIndex
        val matches = result.matches[lineIndex]
            ?: return CursorResult.Err("no matches for line index $lineIndex (max: ${result.matches.keys.sorted().max()})")
        val indexes = result.indexes
        var matchIndex = cursor.matchIndex + increment.toInt()
        var index = indexes.indexOf(lineIndex)
        if (increment && matchIndex == matches.size) {
            index = index.inc() % indexes.size
            lineIndex = indexes[index]
            matchIndex = 0
        } else if (!increment && matchIndex < 0) {
            index = indexes.run { (size + index.dec()) % size }
            lineIndex = indexes[index]
            matchIndex = matches.lastIndex
        }
        if (lineIndex > textLines.value.lastIndex) {
            return CursorResult.Load(lineIndex)
        }
        matchesCursor.value = MatchCursor(lineIndex, matchIndex)
        status.run {
            value = value.go(forward = increment)
        }
        return CursorResult.Ok
    }

    private fun startNavigation(result: SearchResult.Text, increment: Boolean): CursorResult {
        val indexes = result.indexes
        val matches = result.matches
        val statusIndex = when {
            increment -> 1
            indexes.last() < textLines.value.size -> status.value.max
            else -> return CursorResult.Load(indexes.last())
        }
        status.update {
            it.copy(current = statusIndex)
        }
        val lineIndex = if (increment) indexes.first() else indexes.last()
        val matchIndex = if (increment) 0 else matches[lineIndex]?.lastIndex ?: 0
        matchesCursor.value = MatchCursor(lineIndex = lineIndex, matchIndex = matchIndex)
        return CursorResult.Ok
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
                matchesCursor.value = null
                status.run {
                    value = value.copy(current = 0, max = task.count)
                }
            }
        }
    }
}