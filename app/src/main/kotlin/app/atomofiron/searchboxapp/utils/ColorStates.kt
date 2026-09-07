package app.atomofiron.searchboxapp.utils

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.annotation.ColorInt

enum class ColorState(val value: Int) {
    Checked(android.R.attr.state_checked),
    Unchecked(-android.R.attr.state_checked),
    Selected(android.R.attr.state_selected),
    Unselected(-android.R.attr.state_selected),
    Hovered(android.R.attr.state_hovered),
    Unhovered(-android.R.attr.state_hovered),
    Focused(android.R.attr.state_focused),
    Unfocused(-android.R.attr.state_focused),
    Pressed(android.R.attr.state_pressed),
    Unpressed(-android.R.attr.state_pressed),
    Enabled(android.R.attr.state_enabled),
    Disabled(-android.R.attr.state_enabled),
    Active(android.R.attr.state_active),
    Inactive(-android.R.attr.state_active),
    Activated(android.R.attr.state_activated),
    Inactivated(-android.R.attr.state_activated),
}

interface States {
    val checked get() = ColorState.Checked
    val unchecked get() = ColorState.Unchecked
    val selected get() = ColorState.Selected
    val unselected get() = ColorState.Unselected
    val hovered get() = ColorState.Hovered
    val unhovered get() = ColorState.Unhovered
    val focused get() = ColorState.Focused
    val unfocused get() = ColorState.Unfocused
    val pressed get() = ColorState.Pressed
    val unpressed get() = ColorState.Unpressed
    val enabled get() = ColorState.Enabled
    val disabled get() = ColorState.Disabled
    val active get() = ColorState.Active
    val inactive get() = ColorState.Inactive
    val activated get() = ColorState.Activated
    val inactivated get() = ColorState.Inactivated
}

interface ColorStates : States {

    fun Int.add(first: ColorState, vararg other: ColorState)

    companion object {

        operator fun invoke(@ColorInt default: Int): ColorStateList = ColorStatesImpl().build(default)

        operator fun invoke(
            @ColorInt default: Int,
            builder: ColorStates.() -> Unit,
        ): ColorStateList = ColorStatesImpl()
            .also(builder)
            .build(default)
    }
}

interface ColorForState : States {

    fun get(vararg other: ColorState): Int

    companion object {

        operator fun <T> invoke(
            list: ColorStateList,
            extractor: ColorForState.() -> T,
        ): T = ColorForStateImpl(list).let(extractor)
    }
}

private class ColorStatesImpl : ColorStates {

    private val map = linkedSetOf<Pair<Int, Set<ColorState>>>()

    override fun Int.add(first: ColorState, vararg other: ColorState) {
        val states = buildSet {
            add(first)
            addAll(other)
        }
        map.add(this to states)
    }

    fun build(@ColorInt default: Int): ColorStateList {
        map.add(default to emptySet())
        val (colors, states) = map.asSequence().map { (color, states) ->
            color to states.map { it.value }.toIntArray()
        }.unzip()
        map.clear()
        return ColorStateList(states.toTypedArray(), colors.toIntArray())
    }
}

private class ColorForStateImpl(
    private val list: ColorStateList,
) : ColorForState {

    override fun get(vararg other: ColorState): Int {
        val states = other.map { it.value }.toIntArray()
        return list.getColorForState(states, Color.MAGENTA)
    }
}
