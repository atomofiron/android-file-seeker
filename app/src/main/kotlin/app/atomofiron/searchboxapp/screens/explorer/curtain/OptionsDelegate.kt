package app.atomofiron.searchboxapp.screens.explorer.curtain

import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.*
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.fileseeker.databinding.CurtainExplorerOptionsBinding
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerHolder
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.insetsPadding

class OptionsDelegate(
    private val menuId: Int,
    private val output: MenuListener,
) {

    fun getView(options: ExplorerItemOptions, inflater: LayoutInflater): View {
        val binding = CurtainExplorerOptionsBinding.inflate(inflater, null, false)
        binding.menuView.run {
            val menu = inflateMenu(menuId)
            options.bind(menu)
            setMenuListener(output)
            markAsDangerous(R.id.menu_delete)
        }
        binding.init(options)
        binding.root.insetsPadding(ExtType.curtain, top = true)
        binding.menuView.insetsPadding(ExtType.curtain, bottom = true)
        return binding.root
    }

    fun CurtainExplorerOptionsBinding.init(options: ExplorerItemOptions) {
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
                val resources = root.resources
                explorerMenuTvTitle.isVisible = true
                explorerMenuItem.root.isGone = true
                var files = 0
                var dirs = 0
                options.items.forEach {
                    if (it.isDirectory) dirs++ else files++
                }
                val string = StringBuilder()
                if (dirs > 0) {
                    string.append(resources.getQuantityString(R.plurals.x_dirs, dirs, dirs))
                }
                if (dirs > 0 && files > 0) {
                    string.append(", ")
                }
                if (files > 0) {
                    string.append(resources.getQuantityString(R.plurals.x_files, files, files))
                }
                explorerMenuTvTitle.text = string.toString()
            }
        }
    }

    private fun ExplorerItemOptions.bind(menu: Menu) {
        val iter = menu.iterator()
        while (iter.hasNext()) {
            bind(iter)
        }
    }

    private fun ExplorerItemOptions.bind(iterator: MutableIterator<MenuItem>) {
        val item = iterator.next()
        if (!ids.contains(item.itemId)) {
            iterator.remove()
            return
        }
        item.isChecked = checked.contains(item.itemId)
        item.isEnabled = !disabled.contains(item.itemId)
        item.subMenu?.let { bind(it) }
    }
}