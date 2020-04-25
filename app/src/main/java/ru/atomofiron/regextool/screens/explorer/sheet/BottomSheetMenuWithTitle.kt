package ru.atomofiron.regextool.screens.explorer.sheet

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.model.preference.ExplorerItemComposition
import ru.atomofiron.regextool.screens.explorer.adapter.ExplorerHolder
import ru.atomofiron.regextool.custom.view.bottom_sheet_menu.BottomSheetMenu
import ru.atomofiron.regextool.custom.view.bottom_sheet_menu.BottomSheetMenuListener

class BottomSheetMenuWithTitle(
        private val context: Context,
        menuItemClickListener: BottomSheetMenuListener
) : BottomSheetMenu(R.layout.sheet_explorer_options, context, R.menu.explorer_item_options, menuItemClickListener) {
    private val tvTitle: TextView get() = bottomSheetView.contentView.findViewById(R.id.explorer_menu_tv_title)
    private val exItem: View get() = bottomSheetView.contentView.findViewById(R.id.explorer_menu_item)
    private val exItemCheckBox: View get() = exItem.findViewById(R.id.item_explorer_cb)
    override val recyclerView: RecyclerView get() = bottomSheetView.contentView.findViewById(R.id.explorer_menu_rv)
    override var contentView: View? = null

    override fun show(items: List<Int>) = throw Exception()

    fun show(options: ExplorerItemOptions) {
        super.show(options.ids)
        when (options.items.size) {
            1 -> {
                tvTitle.visibility = View.GONE
                exItem.visibility = View.VISIBLE
                val holder = ExplorerHolder(exItem)
                holder.bind(options.items[0])
                holder.bindComposition(options.composition)
            }
            else -> {
                tvTitle.visibility = View.VISIBLE
                exItem.visibility = View.GONE
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
                tvTitle.text = string.toString()
            }
        }
        disableClicks()
    }

    private fun disableClicks() {
        exItem.isFocusable = false
        exItem.isClickable = false
        exItem.background = null
        exItemCheckBox.isFocusable = false
        exItemCheckBox.isClickable = false
    }

    data class ExplorerItemOptions(
            val ids: List<Int>,
            val items: List<XFile>,
            val composition: ExplorerItemComposition
    )
}