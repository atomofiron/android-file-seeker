package app.atomofiron.searchboxapp.screens.explorer.presenter.curtain

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.*
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuImpl
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.databinding.CurtainExplorerOptionsBinding
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerHolder
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets

class OptionsDelegate(
    private val menuId: Int,
    private val output: MenuListener,
) {

    fun getView(options: ExplorerItemOptions, inflater: LayoutInflater, container: ViewGroup): View {
        val binding = CurtainExplorerOptionsBinding.inflate(inflater, container, false)
        binding.menuView.run {
            val menu = inflateMenu(menuId)
            menu.hideExtra(options.ids)
            setMenuListener(output)
            markAsDangerous(R.id.menu_remove)
        }
        binding.init(container.context, options)
        binding.root.applyPaddingInsets(top = true, withProxying = true)
        binding.menuView.applyPaddingInsets(bottom = true)
        return binding.root
    }

    fun CurtainExplorerOptionsBinding.init(context: Context, options: ExplorerItemOptions) {
        when (options.items.size) {
            1 -> {
                explorerMenuTvTitle.isGone = true
                explorerMenuItem.root.isVisible = true
                val holder = ExplorerHolder(explorerMenuItem.root)
                holder.bind(options.items.first())
                holder.bindComposition(options.composition.copy(visibleBg = true))

                val binder = ExplorerItemBinderImpl(explorerMenuItem.root)
                binder.setGreyBackgroundColor()
                binder.disableClicks()
                explorerMenuItem.itemExplorerCb.isEnabled = false
            }
            else -> {
                explorerMenuTvTitle.isVisible = true
                explorerMenuItem.root.isGone = true
                var files = 0
                var dirs = 0
                options.items.forEach {
                    if (it.isDirectory) dirs++ else files++
                }
                val string = StringBuilder()
                if (dirs > 0) {
                    string.append(context.resources.getQuantityString(R.plurals.x_dirs, dirs, dirs))
                }
                if (dirs > 0 && files > 0) {
                    string.append(", ")
                }
                if (files > 0) {
                    string.append(context.resources.getQuantityString(R.plurals.x_files, files, files))
                }
                explorerMenuTvTitle.text = string.toString()
            }
        }
    }

    private fun MenuImpl.hideExtra(ids: List<Int>) {
        val iter = iterator()
        while (iter.hasNext()) {
            if (!ids.contains(iter.next().itemId)) {
                iter.remove()
            }
        }
    }
}