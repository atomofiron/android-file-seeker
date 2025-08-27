package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainPreferenceJoystickBinding
import app.atomofiron.searchboxapp.custom.drawable.setStrokedBackground
import app.atomofiron.searchboxapp.custom.view.effect
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.model.preference.JoystickHaptic
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.performHapticEffect
import app.atomofiron.searchboxapp.utils.setHapticEffect
import lib.atomofiron.insets.insetsPadding

class JoystickDelegate(
    private val preferences: PreferenceStore,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {
    companion object {
        private fun JoystickComposition.withPrimary(context: Context): JoystickComposition {
            val color = context.findColorByAttr(MaterialAttr.colorPrimary)
            return copy(red = Color.red(color), green = Color.green(color), blue = Color.blue(color))
        }
    }

    private var entity: JoystickComposition = preferences.joystickComposition.value
    private val hapticFeedbackWasEnabled: Boolean = preferences.hapticFeedback.value
    private val hapticFeedback: Boolean get() = preferences.hapticFeedback.value

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
        hapticScale.max = JoystickHaptic.entries.lastIndex

        bind()
        val listener = Listener(this)
        sbRed.setOnSeekBarChangeListener(listener)
        sbGreen.setOnSeekBarChangeListener(listener)
        sbBlue.setOnSeekBarChangeListener(listener)
        invForTheme.setOnCheckedChangeListener(listener)
        invHighlight.setOnCheckedChangeListener(listener)
        hapticScale.setOnSeekBarChangeListener(listener)
        btnDefault.setOnClickListener(listener)
        colorPicker.setStrokedBackground(vertical = R.dimen.padding_half)
    }

    private fun CurtainPreferenceJoystickBinding.bind() {
        sbRed.progress = entity.red
        sbGreen.progress = entity.green
        sbBlue.progress = entity.blue
        invForTheme.isChecked = entity.invForDark
        invHighlight.isChecked = entity.invGlowing
        hapticScale.progress = when {
            hapticFeedback -> entity.haptic.index
            else -> JoystickHaptic.None.index
        }
    }

    private inner class Listener(
        private val binding: CurtainPreferenceJoystickBinding,
    ) : SeekBar.OnSeekBarChangeListener,
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            val new = when (seekBar.id) {
                R.id.sb_red -> entity.copy(red = progress, overrideTheme = true)
                R.id.sb_green -> entity.copy(green = progress, overrideTheme = true)
                R.id.sb_blue -> entity.copy(blue = progress, overrideTheme = true)
                R.id.haptic_scale -> {
                    if (fromUser) seekBar.onNewHaptic(progress)
                    return
                }
                else -> throw Exception()
            }
            if (new != entity) {
                onNewColor(new)
            }
        }

        private fun onNewColor(new: JoystickComposition) {
            entity = new
            binding.preferenceJoystickTvTitle.text = entity.colorText()
            preferences { setJoystickComposition(entity) }
        }

        private fun View.onNewHaptic(new: Int) {
            val haptic = JoystickHaptic.index(new)
            entity = entity.copy(haptic = haptic)
            preferences {
                setJoystickComposition(entity)
                when {
                    haptic != JoystickHaptic.None -> setHapticFeedback(true)
                    !hapticFeedbackWasEnabled -> setHapticFeedback(false)
                }
            }
            when {
                haptic != JoystickHaptic.None -> setHapticEffect(true)
                !hapticFeedbackWasEnabled -> setHapticEffect(false)
            }
            performHapticEffect(haptic.effect(press = true))
            postDelayed({
                performHapticEffect(haptic.effect(press = false))
            }, Const.SMALL_DELAY)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

        override fun onStopTrackingTouch(seekBar: SeekBar?) = preferences { setJoystickComposition(entity) }

        override fun onClick(view: View) {
            entity = when (view.id) {
                R.id.btn_default -> JoystickComposition.Default.withPrimary(view.context)
                else -> throw Exception()
            }
            binding.bind()
            preferences { setJoystickComposition(entity) }
        }

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            entity = when (button.id) {
                R.id.inv_for_theme -> entity.copy(invForDark = button.isChecked, overrideTheme = true)
                R.id.inv_highlight -> entity.copy(invGlowing = button.isChecked)
                else -> throw Exception()
            }
            binding.bind()
            preferences { setJoystickComposition(entity) }
        }
    }
}