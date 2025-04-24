package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.MaterialAttr
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
        preferenceJoystickTvTitle.text = entity.colorText()

        val listener = Listener(this)
        sbRed.setOnSeekBarChangeListener(listener)
        sbGreen.setOnSeekBarChangeListener(listener)
        sbBlue.setOnSeekBarChangeListener(listener)
        invForTheme.setOnCheckedChangeListener(listener)
        invHighlight.setOnCheckedChangeListener(listener)
        withHaptic.setOnCheckedChangeListener(listener)
        btnDefault.setOnClickListener(listener)

        bind(entity)
    }

    private fun CurtainPreferenceJoystickBinding.bind(composition: JoystickComposition) {
        sbRed.progress = composition.red
        sbGreen.progress = composition.green
        sbBlue.progress = composition.blue
        invForTheme.isChecked = composition.invForDark
        invHighlight.isChecked = composition.invGlowing
        withHaptic.isChecked = composition.withHaptic
    }

    private inner class Listener(
        private val binding: CurtainPreferenceJoystickBinding,
    ) : SeekBar.OnSeekBarChangeListener, View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            val new = when (seekBar.id) {
                R.id.sb_red -> entity.copy(red = progress, overrideTheme = true)
                R.id.sb_green -> entity.copy(green = progress, overrideTheme = true)
                R.id.sb_blue -> entity.copy(blue = progress, overrideTheme = true)
                else -> throw Exception()
            }
            if (new.red == entity.red && new.green == entity.green && new.blue == entity.blue) return
            entity = new
            binding.preferenceJoystickTvTitle.text = entity.colorText()
            preferenceStore { setJoystickComposition(entity) }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

        override fun onStopTrackingTouch(seekBar: SeekBar?) = preferenceStore { setJoystickComposition(entity) }

        override fun onClick(view: View) {
            entity = when (view.id) {
                R.id.btn_default -> JoystickComposition.Default.withPrimary(view.context)
                else -> throw Exception()
            }
            binding.bind(entity)
            preferenceStore { setJoystickComposition(entity) }
        }

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            entity = when (button.id) {
                R.id.inv_for_theme -> entity.copy(invForDark = button.isChecked, overrideTheme = true)
                R.id.inv_highlight -> entity.copy(invGlowing = button.isChecked)
                R.id.with_haptic -> entity.copy(withHaptic = button.isChecked)
                else -> throw Exception()
            }
            binding.bind(entity)
            preferenceStore { setJoystickComposition(entity) }
        }
    }
}