package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainPreferenceJoystickBinding
import app.atomofiron.searchboxapp.custom.drawable.setStrokedBackground
import app.atomofiron.searchboxapp.custom.view.effect
import app.atomofiron.searchboxapp.custom.view.joystickDefaultColor
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.model.preference.JoystickHaptic
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.context
import app.atomofiron.searchboxapp.utils.intValue
import app.atomofiron.searchboxapp.utils.performHapticEffect
import app.atomofiron.searchboxapp.utils.setHapticEffect
import com.google.android.material.slider.Slider
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
        hapticScale.intValue = JoystickHaptic.entries.lastIndex
        colorPicker.setStrokedBackground(vertical = R.dimen.padding_half)

        bind()
        val delegate = LocalDelegate(this)
        red.addOnChangeListener(delegate::onColorChanged)
        red.setLabelFormatter(delegate::colorLabels)
        green.addOnChangeListener(delegate::onColorChanged)
        green.setLabelFormatter(delegate::colorLabels)
        blue.addOnChangeListener(delegate::onColorChanged)
        blue.setLabelFormatter(delegate::colorLabels)
        invForTheme.setOnCheckedChangeListener(delegate::onThemeInvertingChanged)
        invHighlight.setOnCheckedChangeListener(delegate::onHighlightInvertingChanged)
        hapticScale.addOnChangeListener(delegate::onHapticChanged)
        hapticScale.setLabelFormatter(delegate::hapticLabels)
        btnReset.setOnClickListener(delegate::onResetDefaultClick)
    }

    private fun CurtainPreferenceJoystickBinding.bind() {
        red.intValue = entity.red
        green.intValue = entity.green
        blue.intValue = entity.blue
        invForTheme.isChecked = entity.invForDark
        invForTheme.setChipIconResource(entity.invForDark.iconId())
        invHighlight.isChecked = entity.invGlowing
        invHighlight.setChipIconResource(entity.invGlowing.iconId())
        hapticScale.intValue = when {
            hapticFeedback -> entity.haptic.index
            else -> JoystickHaptic.None.index
        }
        title.text = entity.colorText()
        btnReset.isEnabled = entity.overrideColor || entity != JoystickComposition.Default.copy(red = entity.red, green = entity.green, blue = entity.blue)
    }

    private fun Boolean.iconId() = if (this) R.drawable.ic_check_box else R.drawable.ic_check_box_outline

    private inner class LocalDelegate(private val binding: CurtainPreferenceJoystickBinding) {

        fun colorLabels(value: Float): String = entity.colorText(value.toInt())

        fun hapticLabels(value: Float): String {
            return when (JoystickHaptic.index(value.toInt())) {
                JoystickHaptic.None -> R.string.none
                JoystickHaptic.Lite -> R.string.lite
                JoystickHaptic.Double -> R.string.twice
                JoystickHaptic.Heavy -> R.string.heavy
            }.let { binding.context.getString(it) }
        }

        fun onColorChanged(slider: Slider, value: Float, fromUser: Boolean) {
            if (!fromUser) {
                return
            }
            val value = value.toInt()
            when (slider.id) {
                R.id.red -> entity.copy(red = value, overrideColor = true)
                R.id.green -> entity.copy(green = value, overrideColor = true)
                R.id.blue -> entity.copy(blue = value, overrideColor = true)
                else -> throw Exception()
            }.apply()
        }

        fun onHapticChanged(slider: Slider, value: Float, fromUser: Boolean) {
            if (!fromUser) {
                return
            }
            val value = value.toInt()
            val haptic = JoystickHaptic.index(value)
            entity = entity.copy(haptic = haptic)
            binding.bind()
            preferences {
                setJoystickComposition(entity)
                when {
                    haptic != JoystickHaptic.None -> setHapticFeedback(true)
                    !hapticFeedbackWasEnabled -> setHapticFeedback(false)
                }
            }
            binding.root.run {
                when {
                    haptic != JoystickHaptic.None -> setHapticEffect(true)
                    !hapticFeedbackWasEnabled -> setHapticEffect(false)
                }
                performHapticEffect(haptic.effect(press = true))
                postDelayed({
                    performHapticEffect(haptic.effect(press = false))
                }, Const.SMALL_DELAY)
            }
        }

        fun onResetDefaultClick(view: View) {
            JoystickComposition.Default.withDefaultColor(view.context).apply()
        }

        fun onThemeInvertingChanged(button: CompoundButton, isChecked: Boolean) {
            entity.copy(invForDark = isChecked, overrideColor = !isChecked || !entity.isColorDefault()).apply()
        }

        fun onHighlightInvertingChanged(button: CompoundButton, isChecked: Boolean) {
            entity.copy(invGlowing = isChecked).apply()
        }

        private fun JoystickComposition.isColorDefault(): Boolean {
            val default = withDefaultColor(binding.context)
            return default.red == red && default.green == green && default.blue == blue
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