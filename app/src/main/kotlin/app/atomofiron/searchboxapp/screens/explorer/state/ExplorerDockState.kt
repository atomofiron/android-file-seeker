package app.atomofiron.searchboxapp.screens.explorer.state

import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemChildren
import app.atomofiron.searchboxapp.di.dependencies.channel.PreferenceChannel
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.model.Layout
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.common.delegates.copiable
import app.atomofiron.searchboxapp.screens.common.delegates.pasteable
import app.atomofiron.searchboxapp.screens.explorer.ExplorerScope
import app.atomofiron.searchboxapp.screens.explorer.state.ExplorerDock.Cancel
import app.atomofiron.searchboxapp.screens.explorer.state.ExplorerDock.PasteCopy
import app.atomofiron.searchboxapp.screens.explorer.state.ExplorerDock.PasteMove
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ExplorerScope
class ExplorerDockState @Inject constructor(
    private val mode: ActivityMode,
    store: ExplorerStore,
    preferenceChannel: PreferenceChannel,
) {
    private val ground = MutableStateFlow(Layout.Ground.Bottom)
    private val sorting: Flow<DockItem> = store.currentSorting.map { (_, it) -> sorting(it) }
    private val copyPaste: Flow<List<DockItem>> = combine(store.currentDeepest, store.checked, store.pasteBuffer, ground, transform = ::copyPaste)
    private val settingsConfirm: Flow<DockItem> = combine(store.currentDeepest, preferenceChannel.notification, store.checked, transform = ::settingsConfirmItem)
    val state: Flow<List<DockItem>> = combine(sorting, copyPaste, settingsConfirm, transform = ::dockItems)

    private fun settingsConfirmItem(deepest: Node?, notice: Boolean, checked: List<Node>): DockItem {
        val checked = checked.filter { it.isFile }
        return when (mode) {
            is ActivityMode.Default -> ExplorerDock.Settings.copy(notice = DockItem.Notice.Alert.takeIf { notice })
            is ActivityMode.Receive -> ExplorerDock.Confirm.copy(enabled = deepest?.isDirectory == true)
            is ActivityMode.Share -> ExplorerDock.Confirm.copy(enabled = deepest?.isFile == true || checked.isNotEmpty() && (mode.multiple || checked.size == 1))
        }
    }

    private fun sorting(sorting: NodeSorting?): DockItem {
        var children = ExplorerDock.Sorting.children
        val selected = children
            .find { it.id == sorting }
            ?.copy(selected = true)
        children = when (selected) {
            null -> children
            else -> children.copy {
                if (it.id == selected.id) selected else it
            }
        }
        val icon = selected?.icon ?: ExplorerDock.Sorting.icon
        return ExplorerDock.Sorting.copy(icon = icon, enabled = selected != null, children = children)
    }

    private fun copyPaste(
        deepest: Node?,
        checked: List<Node>,
        copied: List<Node>,
        ground: Layout.Ground,
    ): List<DockItem> = when {
        ground.isBottom -> listOf(copyOrPaste(deepest, checked, copied))
        else -> copyAndPaste(deepest, checked, copied)
    }

    private fun copyOrPaste(
        deepest: Node?,
        checked: List<Node>,
        copied: List<Node>,
    ): DockItem {
        val pasteable = deepest != null && copied.pasteable(deepest)
        val copyable = when {
            checked.isNotEmpty() -> checked.copiable()
            deepest != null -> deepest.copiable()
            else -> false
        }
        val allCopiedAreDirs = copied.isNotEmpty() && copied.all { it.isDirectory }
        val icon = when {
            deepest == null -> R.drawable.ic_copy_file
            !copyable && pasteable && allCopiedAreDirs -> R.drawable.ic_insert_folder
            !copyable && pasteable -> R.drawable.ic_insert_file
            copyable && checked.all { it.isDirectory } -> R.drawable.ic_copy_folder
            copyable && checked.isNotEmpty() -> R.drawable.ic_copy_file
            copyable && deepest.isDirectory -> R.drawable.ic_copy_folder
            copyable -> R.drawable.ic_copy_file
            else -> R.drawable.ic_copy_file
        }.let { DockItem.Icon(it) }
        val notice = DockItem.Notice.Normal.takeIf { copied.isNotEmpty() }
        val children = pasteChildren(copied, allCopiedAreDirs, copyable = copyable, pasteable = pasteable)
        return when {
            !copyable && pasteable -> ExplorerDock.Paste.copy(icon = icon, notice = notice, children = children)
            else -> ExplorerDock.Copy.copy(icon = icon, enabled = copyable, notice = notice, children = children)
        }
    }

    private fun copyAndPaste(
        deepest: Node?,
        checked: List<Node>,
        copied: List<Node>,
    ): List<DockItem> {
        val pasteable = deepest != null && copied.pasteable(deepest)
        val copyable = when {
            checked.isNotEmpty() -> checked.copiable()
            deepest != null -> deepest.copiable()
            else -> false
        }
        val allCopiedAreDirs = copied.isNotEmpty() && copied.all { it.isDirectory }
        val copyIcon = when {
            deepest == null -> R.drawable.ic_copy_file
            copyable && checked.all { it.isDirectory } -> R.drawable.ic_copy_folder
            copyable && checked.isNotEmpty() -> R.drawable.ic_copy_file
            copyable && deepest.isDirectory -> R.drawable.ic_copy_folder
            copyable -> R.drawable.ic_copy_file
            else -> R.drawable.ic_copy_file
        }.let { DockItem.Icon(it) }
        val pasteIcon = when {
            deepest == null -> R.drawable.ic_insert_file
            !copyable && pasteable && allCopiedAreDirs -> R.drawable.ic_insert_folder
            !copyable && pasteable -> R.drawable.ic_insert_file
            else -> R.drawable.ic_insert_file
        }.let { DockItem.Icon(it) }
        val notice = DockItem.Notice.Normal.takeIf { copied.isNotEmpty() }
        val children = pasteChildren(copied, allCopiedAreDirs, copyable = copyable, pasteable = pasteable)
        return listOf(
            ExplorerDock.Copy.copy(icon = copyIcon, enabled = copyable),
            ExplorerDock.Paste.copy(icon = pasteIcon, enabled = pasteable, notice = notice, children = children),
        )
    }

    private fun pasteChildren(
        copied: List<Node>,
        allCopiedAreDirs: Boolean,
        copyable: Boolean,
        pasteable: Boolean,
    ): DockItemChildren {
        val pasteCopy = (if (allCopiedAreDirs) R.drawable.ic_insert_copy_folder else R.drawable.ic_insert_copy_file)
            .let { DockItem.Icon(it) }
            .let { PasteCopy.copy(enabled = pasteable, icon = it) }
        val pasteMove = (if (allCopiedAreDirs) R.drawable.ic_insert_move_folder else R.drawable.ic_insert_move_file)
            .let { DockItem.Icon(it) }
            .let { PasteMove.copy(enabled = pasteable, icon = it) }
        val cancel = Cancel.copy(enabled = copied.isNotEmpty())
        return DockItemChildren(pasteCopy, pasteMove, cancel, secondary = copyable)
    }

    private fun dockItems(
        sorting: DockItem,
        copyPaste: List<DockItem>,
        settingsConfirm: DockItem,
    ): List<DockItem> = buildList {
        add(ExplorerDock.Search)
        add(sorting)
        addAll(copyPaste)
        add(settingsConfirm)
    }

    fun setGround(ground: Layout.Ground) {
        this.ground.value = ground
    }
}