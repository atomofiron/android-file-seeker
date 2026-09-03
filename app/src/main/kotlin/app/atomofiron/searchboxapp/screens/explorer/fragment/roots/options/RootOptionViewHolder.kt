package app.atomofiron.searchboxapp.screens.explorer.fragment.roots.options

import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemRootOptionCameraBinding
import app.atomofiron.searchboxapp.model.explorer.NodeRootOption
import app.atomofiron.searchboxapp.model.explorer.NodeRootOption.CameraToggle
import app.atomofiron.searchboxapp.utils.check

class RootOptionViewHolder(
    private val binding: ItemRootOptionCameraBinding,
    private val output: OnCameraToggleClickListener,
) : GeneralHolder<NodeRootOption>(binding.root) {

    init {
        binding.group.addOnButtonCheckedListener { _, id, bool ->
            if (!bool) {
                return@addOnButtonCheckedListener
            }
            val target = when (id) {
                R.id.photos -> CameraToggle.Photos
                R.id.all -> CameraToggle.All
                R.id.videos -> CameraToggle.Videos
                else -> return@addOnButtonCheckedListener
            }
            output.onClick(target)
        }
    }

    override fun onBind(item: NodeRootOption, position: Int) = binding.group.run {
        item as CameraToggle
        check(R.id.photos, item.photos())
        check(R.id.all, item.all())
        check(R.id.videos, item.videos())
    }

    interface OnCameraToggleClickListener {
        fun onClick(target: CameraToggle)
    }
}
