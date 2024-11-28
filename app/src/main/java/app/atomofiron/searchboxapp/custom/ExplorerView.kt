package app.atomofiron.searchboxapp.custom

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import app.atomofiron.searchboxapp.databinding.ViewExplorerBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeTabItems
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerListDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerSpanSizeLookup
import app.atomofiron.searchboxapp.screens.explorer.fragment.SwipeMarkerDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.OnScrollIdleSubmitter
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootViewHolder.Companion.getTitle
import app.atomofiron.searchboxapp.utils.scrollToTop

@SuppressLint("ViewConstructor")
class ExplorerView(
    context: Context,
    private val output: ExplorerViewOutput,
) : FrameLayout(context) {

    private val binding = ViewExplorerBinding.inflate(LayoutInflater.from(context), this)
    var title: String? = null
        private set

    val recyclerView = binding.recyclerView
    val headerView = binding.explorerHeader
    val systemUiView = binding.systemUiBackground

    private val rootAdapter = RootAdapter()
    private val explorerAdapter = ExplorerAdapter()

    private lateinit var listDelegate: ExplorerListDelegate
    private lateinit var spanSizeLookup: ExplorerSpanSizeLookup

    private val submitter = OnScrollIdleSubmitter(binding.recyclerView, explorerAdapter)

    init {
        explorerAdapter.itemActionListener = output
        explorerAdapter.separatorClickListener = ::onSeparatorClick
        rootAdapter.clickListener = output

        binding.applyInsets()
        binding.init()
    }

    private fun ViewExplorerBinding.init() {
        val layoutManager = GridLayoutManager(context, 1)
        spanSizeLookup = ExplorerSpanSizeLookup(recyclerView, layoutManager, rootAdapter)
        layoutManager.spanSizeLookup = spanSizeLookup
        recyclerView.layoutManager = layoutManager
        val config = ConcatAdapter.Config.Builder()
            .setStableIdMode(ConcatAdapter.Config.StableIdMode.SHARED_STABLE_IDS)
            .build()
        recyclerView.adapter = ConcatAdapter(config, rootAdapter, explorerAdapter)
        recyclerView.addOnItemTouchListener(SwipeMarkerDelegate(resources))

        listDelegate = ExplorerListDelegate(
            recyclerView,
            rootAdapter,
            explorerAdapter,
            explorerHeader,
            output,
        )
    }

    private fun ViewExplorerBinding.applyInsets() {
        // todo recyclerView.applyPaddingInsets()
        // todo explorerHeader.applyPaddingInsets(start = true, top = true, end = true)
    }

    fun onInsetsApplied() = spanSizeLookup.updateSpanCount(recyclerView)

    fun scrollTo(item: Node) = listDelegate.scrollTo(item)

    fun scrollToTop() = recyclerView.scrollToTop()

    fun isCurrentDirVisible(): Boolean? = listDelegate.isCurrentDirVisible()

    fun submitList(items: NodeTabItems) {
        rootAdapter.submitList(items.roots)
        submitter.trySubmitList(items.items, items.current?.path)
        listDelegate.setCurrentDir(items.current)
        title = items.current?.getTitle(resources)
    }

    fun setComposition(composition: ExplorerItemComposition) {
        listDelegate.setComposition(composition)
        explorerAdapter.setComposition(composition)
    }

    private fun onSeparatorClick(item: Node) = when {
        listDelegate.isVisible(item) -> listDelegate.highlight(item)
        else -> listDelegate.scrollTo(item)
    }

    interface ExplorerViewOutput : ExplorerItemActionListener, RootAdapter.RootClickListener
}