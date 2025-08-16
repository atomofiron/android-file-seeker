package app.atomofiron.searchboxapp.screens.explorer.curtain

import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainExplorerOptionsBinding
import app.atomofiron.searchboxapp.custom.drawable.setStrokedBackground
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.insetsPadding

class OptionsDelegate(private val output: MenuListener) {

    fun getView(options: ExplorerItemOptions, inflater: LayoutInflater): View {
        val binding = CurtainExplorerOptionsBinding.inflate(inflater, null, false)
        binding.menuView.run {
            submit(options)
            setMenuListener(output)
        }
        binding.init(options)
        binding.root.insetsPadding(ExtType.curtain, top = true)
        binding.menuView.insetsPadding(ExtType.curtain, bottom = true)
        return binding.root
    }

    fun CurtainExplorerOptionsBinding.init(options: ExplorerItemOptions) {
        val single = options.items.size == 1
        itemView.root.isVisible = single
        title.isVisible = !single
        if (single) {
            val binder = ExplorerItemBinderImpl(itemView.root)
            binder.bind(options.items.first())
            binder.bindComposition(options.composition)
            binder.disableClicks()
            itemView.root.setStrokedBackground(vertical = R.dimen.padding_half)
        } else {
            val resources = root.resources
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
            title.text = string.toString()
        }
    }
}