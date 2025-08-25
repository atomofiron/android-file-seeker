package app.atomofiron.searchboxapp.screens.viewer.presenter.curtain

import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import app.atomofiron.common.recycler.FlexSpanSizeLookup
import app.atomofiron.common.util.flow.collect
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainTextViewerSearchBinding
import app.atomofiron.searchboxapp.custom.drawable.setStrokedBackground
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.common.SectionBackgroundDecorator
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapter
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
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

    private val finderAdapter = FinderAdapter(isLocal = viewState.isLocal, output)

    init {
        viewState.items.collect(scope) { finderAdapter.submitList(it) }
        viewState.insertInQuery.collect(scope, collector = viewState::updateSearchQuery)
    }

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainTextViewerSearchBinding.inflate(inflater, null, false)

        val holder = ExplorerItemBinderImpl(binding.itemExplorer.root)
        holder.bind(node)
        holder.bindComposition(composition)
        holder.disableClicks()
        holder.hideCheckBox()
        binding.itemExplorer.root.setStrokedBackground(vertical = R.dimen.padding_half)

        binding.recyclerView.run {
            val layoutManager = GridLayoutManager(binding.root.context, 1)
            layoutManager.reverseLayout = true
            adapter = finderAdapter
            itemAnimator = null
            this.layoutManager = layoutManager
            val spanSizeLookup = FlexSpanSizeLookup(binding.recyclerView, finderAdapter, layoutManager, binding.root.resources)
            finderAdapter.holderListener = spanSizeLookup
            addItemDecoration(SectionBackgroundDecorator(context, FinderStateItem.groups))
        }

        binding.root.insetsPadding(ExtType.curtain, top = true)
        binding.recyclerView.insetsPadding(ExtType.curtain, bottom = true)

        return CurtainApi.ViewHolder(binding.root)
    }
}