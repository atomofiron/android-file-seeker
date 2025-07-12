package app.atomofiron.searchboxapp.screens.finder.presenter

import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.finder.FinderViewState
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.TargetsHolder
import app.atomofiron.searchboxapp.utils.mutate
import app.atomofiron.searchboxapp.utils.removeOneIf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

class FinderTargetsPresenterDelegate(
    scope: CoroutineScope,
    private val viewState: FinderViewState,
    explorerStore: ExplorerStore,
) : TargetsHolder.FinderTargetsOutput {

    init {
        val merged = combine(explorerStore.current, explorerStore.searchTargets, explorerStore.storageRoot) { current, targets, storage ->
            when {
                current != null -> targets.mutate {
                    removeOneIf { it.uniqueId == current.uniqueId }
                    add(0, current)
                }
                targets.isEmpty() -> listOfNotNull(storage)
                else -> targets
            }
        }
        // zip() ignores new values from 'current'
        combine(merged, viewState.inactiveTargets) { targets, inactive ->
            viewState.inactiveTargets.update { value ->
                value.filter { id -> targets.any { it.uniqueId == id } }
            }
            targets.map { it.copy(isChecked = !inactive.contains(it.uniqueId)) }
        }.collect(scope, viewState::updateTargets)
    }

    override fun onTargetClick(node: Node, toChecked: Boolean) {
        viewState.inactiveTargets.update {
            it.mutate {
                when {
                    toChecked -> remove(node.uniqueId)
                    contains(node.uniqueId) -> return
                    else -> add(node.uniqueId)
                }
            }
        }
    }
}
