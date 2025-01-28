package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainPreferenceJoystickBinding
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.insetsPadding

class JoystickDelegate(
    private val preferenceStore: PreferenceStore,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {
    companion object {
        private fun JoystickComposition.withPrimary(context: Context): JoystickComposition {
            val color = context.findColorByAttr(MaterialAttr.colorPrimary)
            return copy(red = Color.red(color), green = Color.green(color), blue = Color.blue(color))
        }
    }

    private var entity: JoystickComposition = preferenceStore.joystickComposition.value

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainPreferenceJoystickBinding.inflate(inflater, null, false)
        binding.init()
        binding.root.insetsPadding(ExtType.curtain, vertical = true)
        return CurtainApi.ViewHolder(binding.root)
    }

    private fun CurtainPreferenceJoystickBinding.init() {
        if (!entity.overrideTheme) {
            // day/night themes has different primary colors
            entity = entity.withPrimary(root.context)
        }
        preferenceJoystickTvTitle.text = entity.text()

        val listener = Listener(this)
        preferenceSbRed.setOnSeekBarChangeListener(listener)
        preferenceSbGreen.setOnSeekBarChangeListener(listener)
        preferenceSbBlue.setOnSeekBarChangeListener(listener)
        preferenceInvForTheme.setOnClickListener(listener)
        preferenceInvHighlight.setOnClickListener(listener)
        preferenceBtnDefault.setOnClickListener(listener)

        preferenceSbRed.progress = entity.red
        preferenceSbGreen.progress = entity.green
        preferenceSbBlue.progress = entity.blue
        preferenceInvForTheme.isChecked = entity.invForDark
        preferenceInvHighlight.isChecked = entity.invGlowing
    }

    private fun CurtainPreferenceJoystickBinding.bind(composition: JoystickComposition) {
        preferenceSbRed.progress = composition.red
        preferenceSbGreen.progress = composition.green
        preferenceSbBlue.progress = composition.blue
        preferenceInvForTheme.isChecked = composition.invForDark
        preferenceInvHighlight.isChecked = composition.invGlowing
    }

    private inner class Listener(
        private val binding: CurtainPreferenceJoystickBinding,
    ) : SeekBar.OnSeekBarChangeListener, View.OnClickListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            val new = when (seekBar.id) {
                R.id.preference_sb_red -> entity.copy(red = progress, overrideTheme = true)
                R.id.preference_sb_green -> entity.copy(green = progress, overrideTheme = true)
                R.id.preference_sb_blue -> entity.copy(blue = progress, overrideTheme = true)
                else -> throw Exception()
            }
            if (new.red == entity.red && new.green == entity.green && new.blue == entity.blue) return
            entity = new
            binding.preferenceJoystickTvTitle.text = entity.text()
            preferenceStore { setJoystickComposition(entity) }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

        override fun onStopTrackingTouch(seekBar: SeekBar?) = preferenceStore { setJoystickComposition(entity) }

        override fun onClick(view: View) {
            entity = when (view.id) {
                R.id.preference_inv_for_theme -> entity.copy(invForDark = (view as CompoundButton).isChecked, overrideTheme = true)
                R.id.preference_inv_highlight -> entity.copy(invGlowing = (view as CompoundButton).isChecked)
                R.id.preference_btn_default -> entity.withPrimary(view.context).copy(overrideTheme = false, invForDark = false)
                else -> throw Exception()
            }
            binding.bind(entity)
            preferenceStore { setJoystickComposition(entity) }
        }
    }
}