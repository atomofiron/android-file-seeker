package app.atomofiron.searchboxapp.screens.viewer

import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.flow.DataFlow
import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.MuonsDrawable
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.LocalSearchResult
import app.atomofiron.searchboxapp.model.finder.LocalSearchTask
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsState
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsStateDelegate
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
import app.atomofiron.searchboxapp.screens.viewer.state.CursorResult
import app.atomofiron.searchboxapp.screens.viewer.state.MatchCursor
import app.atomofiron.searchboxapp.screens.viewer.state.Status
import app.atomofiron.searchboxapp.screens.viewer.state.TextViewerDockState
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toNode
import app.atomofiron.searchboxapp.utils.toAlert
import app.atomofiron.searchboxapp.utils.toInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@TextViewerScope
class TextViewerViewState private constructor(
    ref: NodeRef,
    private val scope: CoroutineScope,
    private val session: TextViewerSession?,
    preferenceStore: PreferenceStore,
    error: NodeError?,
) : FinderItemsState by FinderItemsStateDelegate(
    isLocal = true,
    preferenceStore,
    session?.tasks ?: emptyFlow(),
) {
    val insertInQuery = EventFlow<String>()

    private val status = MutableStateFlow(Status())
    val matchingCursor = DataFlow<MatchCursor?>(null)

    val composition = preferenceStore.explorerItemComposition.value
    val item: StateFlow<Node> = session?.item ?: MutableStateFlow(ref.toNode())
    val textLines: StateFlow<List<TextLine>> = session?.lines ?: MutableStateFlow(emptyList())
    val currentTask = MutableStateFlow<LocalSearchTask?>(null)
    private val _alerts: MutableStateFlow<Alert?> = DataFlow((session?.error?.value ?: error)?.toAlert())
    val alerts: SharedFlow<Alert?> = _alerts

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

    @Inject constructor(
        params: TextViewerParams,
        scope: CoroutineScope,
        preferenceStore: PreferenceStore,
        session: TextViewerSessionResult,
    ) : this(params.ref, scope, session.result.ok()?.value, preferenceStore, session.error)

    fun switchCursor(forward: Boolean): CursorResult {
        val result = currentTask.value?.result
            ?: return CursorResult.Err("no search result")

        val cursor = matchingCursor.value
            ?: return result.startNavigation(forward)

        return result.switch(cursor, forward)
    }

    private fun LocalSearchResult.switch(cursor: MatchCursor, forward: Boolean): CursorResult {
        var lineIndex = cursor.lineIndex
        var matches = matches[lineIndex]
            ?: return noMatchesErr(lineIndex)

        var matchIndex = cursor.matchIndex + forward.toInt()
        var index = indexes.indexOf(lineIndex)
        if (forward && matchIndex == matches.size) {
            index = index.inc() % indexes.size
            lineIndex = indexes[index]
            this.matches[lineIndex]
                ?: return noMatchesErr(lineIndex)
            matchIndex = 0
        } else if (!forward && matchIndex < 0) {
            index = indexes.run { (size + index.dec()) % size }
            lineIndex = indexes[index]
            matches = this.matches[lineIndex]
                ?: return noMatchesErr(lineIndex)
            matchIndex = matches.lastIndex
        }
        if (lineIndex > textLines.value.lastIndex) {
            return CursorResult.Load(lineIndex)
        }
        matchingCursor.value = MatchCursor(lineIndex, matchIndex)
        status.run {
            value = value.jump(forward)
        }
        return CursorResult.Ok
    }

    private fun noMatchesErr(lineIndex: Int) = CursorResult.Err("no matches for line index $lineIndex (max: ${currentTask.value?.result?.matches?.keys?.sorted()?.max()})")

    private fun LocalSearchResult.startNavigation(forward: Boolean): CursorResult {
        val lineIndex = if (forward) indexes.first() else indexes.last()
        if (lineIndex >= textLines.value.size) {
            return CursorResult.Load(lineIndex)
        }
        val statusIndex = if (forward) 1 else status.value.max
        status.update {
            it.copy(current = statusIndex)
        }
        val matchIndex = if (forward) 0 else matches[lineIndex]?.lastIndex ?: 0
        matchingCursor.value = MatchCursor(lineIndex = lineIndex, matchIndex = matchIndex)
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
        matchingCursor.value = null
    }

    fun showAlert(alert: Alert) {
        _alerts.value = alert
    }

    fun trySelectTask(task: LocalSearchTask): Boolean {
        return (task.isEnded && task.count > 0 && task.error == null).also { isOk ->
            if (isOk) {
                currentTask.value = task
                matchingCursor.value = null
                status.run {
                    value = value.copy(current = 0, max = task.count)
                }
            }
        }
    }
}