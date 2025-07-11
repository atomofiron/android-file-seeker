package app.atomofiron.searchboxapp.screens.viewer.presenter.curtain

import android.view.LayoutInflater
import android.widget.EditText
import androidx.recyclerview.widget.GridLayoutManager
import app.atomofiron.common.recycler.FinderSpanSizeLookup
import app.atomofiron.common.util.flow.collect
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainTextViewerSearchBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapter
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.viewer.TextViewerViewState
import app.atomofiron.searchboxapp.utils.ExtType
import kotlinx.coroutines.CoroutineScope
import lib.atomofiron.insets.insetsPadding

class CurtainSearchDelegate(
    output: FinderAdapterOutput,
    private val viewState: TextViewerViewState,
    scope: CoroutineScope,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {

    private val node: Node get() = viewState.item.value
    private val composition = viewState.composition

    private val finderAdapter = FinderAdapter(output)

    init {
        viewState.items.collect(scope, collector = finderAdapter::submitList)
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

        val layoutManager = GridLayoutManager(binding.root.context, 1)
        val spanSizeLookup = FinderSpanSizeLookup(finderAdapter, layoutManager, binding.root.resources)
        finderAdapter.holderListener = spanSizeLookup
        binding.sheetViewerSearchRv.adapter = finderAdapter
        binding.sheetViewerSearchRv.itemAnimator = null
        binding.sheetViewerSearchRv.layoutManager = layoutManager

        binding.root.insetsPadding(ExtType.curtain, top = true)
        binding.sheetViewerSearchRv.insetsPadding(ExtType.curtain, bottom = true)

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