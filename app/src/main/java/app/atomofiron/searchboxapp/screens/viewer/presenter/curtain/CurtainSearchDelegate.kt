package app.atomofiron.searchboxapp.screens.viewer.presenter.curtain

import android.view.LayoutInflater
import android.widget.EditText
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.CurtainTextViewerSearchBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapter
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.viewer.TextViewerViewState
import kotlinx.coroutines.CoroutineScope
import lib.atomofiron.insets.insetsPadding

class CurtainSearchDelegate(
    output: FinderAdapterOutput,
    private val viewState: TextViewerViewState,
    scope: CoroutineScope,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {

    private val node: Node get() = viewState.item.value
    private val composition = viewState.composition

    private val finderAdapter = FinderAdapter()

    init {
        finderAdapter.output = output
        viewState.searchItems.collect(scope, collector = finderAdapter::submitList)
        viewState.insertInQuery.collect(scope, collector = ::insertInQuery)
    }

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainTextViewerSearchBinding.inflate(inflater, null, false)

        val holder = ExplorerHolder(binding.itemExplorer.root)
        holder.bind(node)
        holder.bindComposition(composition)
        holder.disableClicks()
        holder.hideCheckBox()
        holder.setGreyBackgroundColor()

        // todo inverse
        binding.sheetViewerSearchRv.adapter = finderAdapter
        binding.sheetViewerSearchRv.itemAnimator = null

        binding.root.insetsPadding(top = true)
        binding.sheetViewerSearchRv.insetsPadding(bottom = true)

        return CurtainApi.ViewHolder(binding.root)
    }

    private fun insertInQuery(value: String) {
        holder<CurtainApi.ViewHolder> {
            view.findViewById<EditText>(R.id.item_find_rt_find)
                ?.takeIf { it.isFocused }
                ?.apply {
                    text.replace(selectionStart, selectionEnd, value)
                }
        }
    }
}