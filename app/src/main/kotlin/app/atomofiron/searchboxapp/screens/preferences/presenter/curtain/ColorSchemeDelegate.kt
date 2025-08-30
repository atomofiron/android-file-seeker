package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.updatePaddingRelative
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainColorSchemeBinding
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.updateLayoutParams
import lib.atomofiron.insets.insetsPadding

class ColorSchemeDelegate : CurtainApi.Adapter<CurtainApi.ViewHolder>() {

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainColorSchemeBinding.inflate(inflater, null, false)
        binding.init()
        binding.root.insetsPadding(ExtType.curtain, vertical = true)
        return CurtainApi.ViewHolder(binding.root)
    }

    private fun CurtainColorSchemeBinding.init() {
        for (attr in attrs()) {
            val textView = TextView(root.context)
            val padding = root.resources.getDimensionPixelSize(R.dimen.padding_half)
            textView.updatePaddingRelative(start = padding, top = padding, end = padding, bottom = padding)
            textView.text = root.resources.getResourceEntryName(attr)
            val color = root.context.findColorByAttr(attr)
            textView.setBackgroundColor(color)
            when (ColorUtils.calculateLuminance(color) > 0.5) {
                true -> textView.setTextColor(Color.BLACK)
                false -> textView.setTextColor(Color.WHITE)
            }
            root.addView(textView)
            textView.updateLayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        }
    }
}

private fun attrs() = listOf(
    MaterialAttr.colorPrimary,
    MaterialAttr.colorOnPrimary,
    MaterialAttr.colorPrimaryContainer,
    MaterialAttr.colorOnPrimaryContainer,
    MaterialAttr.colorSecondary,
    MaterialAttr.colorOnSecondary,
    MaterialAttr.colorSecondaryContainer,
    MaterialAttr.colorOnSecondaryContainer,
    MaterialAttr.colorTertiary,
    MaterialAttr.colorOnTertiary,
    MaterialAttr.colorTertiaryContainer,
    MaterialAttr.colorOnTertiaryContainer,
    MaterialAttr.colorError,
    MaterialAttr.colorErrorContainer,
    MaterialAttr.colorOnError,
    MaterialAttr.colorOnErrorContainer,
    MaterialAttr.colorOnBackground,
    MaterialAttr.colorSurface,
    MaterialAttr.colorOnSurface,
    MaterialAttr.colorSurfaceVariant,
    MaterialAttr.colorOnSurfaceVariant,
    MaterialAttr.colorOutline,
    MaterialAttr.colorOutlineVariant,
    MaterialAttr.colorOnSurfaceInverse,
    MaterialAttr.colorSurfaceInverse,
    MaterialAttr.colorPrimaryInverse,
)