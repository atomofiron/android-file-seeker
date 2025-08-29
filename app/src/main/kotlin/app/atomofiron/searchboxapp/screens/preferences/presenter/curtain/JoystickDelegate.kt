package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainPreferenceJoystickBinding
import app.atomofiron.searchboxapp.custom.drawable.setStrokedBackground
import app.atomofiron.searchboxapp.custom.view.effect
import app.atomofiron.searchboxapp.custom.view.joystickDefaultColor
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
        if (!entity.overrideColor) {
            // day/night themes has different colors
            entity = entity.withDefaultColor(root.context)
        }
        hapticScale.max = JoystickHaptic.entries.lastIndex

        bind()
        val listener = Listener(this)
        sbRed.setOnSeekBarChangeListener(listener)
        sbGreen.setOnSeekBarChangeListener(listener)
        sbBlue.setOnSeekBarChangeListener(listener)
        invForTheme.setOnCheckedChangeListener(listener)
        invHighlight.setOnCheckedChangeListener(listener)
        hapticScale.setOnSeekBarChangeListener(listener)
        btnDefault.setOnClickListener(listener::onResetDefaultClick)
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
        preferenceJoystickTvTitle.text = entity.colorText()
    }

    private inner class Listener(
        private val binding: CurtainPreferenceJoystickBinding,
    ) : SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            val overrideColor = entity.overrideColor || fromUser
            when (seekBar.id) {
                R.id.sb_red -> entity.copy(red = progress, overrideColor = overrideColor)
                R.id.sb_green -> entity.copy(green = progress, overrideColor = overrideColor)
                R.id.sb_blue -> entity.copy(blue = progress, overrideColor = overrideColor)
                R.id.haptic_scale -> {
                    if (fromUser) seekBar.onNewHaptic(progress)
                    return
                }
                else -> throw Exception()
            }.apply()
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

        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        fun onResetDefaultClick(view: View) {
            when (view.id) {
                R.id.btn_default -> JoystickComposition.Default.withDefaultColor(view.context)
                else -> throw Exception()
            }.apply()
        }

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            when (button.id) {
                R.id.inv_for_theme -> entity.copy(invForDark = button.isChecked, overrideColor = true)
                R.id.inv_highlight -> entity.copy(invGlowing = button.isChecked)
                else -> throw Exception()
            }.apply()
        }

        private fun JoystickComposition.apply() {
            entity = this
            binding.bind()
            preferences { setJoystickComposition(this@apply) }
        }
    }
}

private fun JoystickComposition.withDefaultColor(context: Context): JoystickComposition {
    val color = context.joystickDefaultColor()
    return copy(red = Color.red(color), green = Color.green(color), blue = Color.blue(color), overrideColor = false)
}