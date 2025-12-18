package app.atomofiron.searchboxapp.screens.licenses.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.fileseeker.databinding.ItemLicenseBinding
import app.atomofiron.searchboxapp.screens.licenses.state.License

class LicenseAdapter(private val onItemClick: (License) -> Unit) : GeneralAdapter<License, LicenseHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): LicenseHolder {
        val textView = ItemLicenseBinding.inflate(inflater, parent, false).root
        val holder = LicenseHolder(textView)
        textView.setOnClickListener {
            onItemClick(items[holder.bindingAdapterPosition])
        }
        return holder
    }
}
