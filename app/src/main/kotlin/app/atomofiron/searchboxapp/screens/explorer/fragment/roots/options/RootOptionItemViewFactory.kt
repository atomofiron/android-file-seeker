package app.atomofiron.searchboxapp.screens.explorer.fragment.roots.options;

import android.view.LayoutInflater
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemRootOptionCameraBinding
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.options.RootOptionAdapter.RootOptionListener

enum class RootOptionItemViewFactory(val viewType: Int) {
    Camera(viewType = R.layout.item_root_option_camera) {
        override fun createHolder(inflater: LayoutInflater, output: RootOptionListener): RootOptionViewHolder {
            val binding = ItemRootOptionCameraBinding.inflate(inflater)
            return RootOptionViewHolder(binding, output)
        }
    },
    ;
    abstract fun createHolder(inflater: LayoutInflater, output: RootOptionListener): RootOptionViewHolder
}