package app.atomofiron.searchboxapp.utils

import android.content.res.ColorStateList

enum class State(val value: Int) {
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

interface ColorStates {
    val checked get() = State.Checked
    val unchecked get() = State.Unchecked
    val selected get() = State.Selected
    val unselected get() = State.Unselected
    val hovered get() = State.Hovered
    val unhovered get() = State.Unhovered
    val focused get() = State.Focused
    val unfocused get() = State.Unfocused
    val pressed get() = State.Pressed
    val unpressed get() = State.Unpressed
    val enabled get() = State.Enabled
    val disabled get() = State.Disabled
    val active get() = State.Active
    val inactive get() = State.Inactive
    val activated get() = State.Activated
    val inactivated get() = State.Inactivated

    fun Int.add(vararg states: State)

    companion object {
        operator fun invoke(builder: ColorStates.() -> Unit): ColorStateList = ColorStatesImpl()
            .apply(builder)
            .build()
    }
}

private class ColorStatesImpl : ColorStates {

    private val map = mutableSetOf<Pair<Int, Set<State>>>()

    override fun Int.add(vararg states: State) {
        map.add(this to states.toSet())
    }

    fun build(): ColorStateList {
        val (colors, states) = map.asSequence().map { (color, states) ->
            color to states.map { it.value }.toIntArray()
        }.unzip()
        return ColorStateList(states.toTypedArray(), colors.toIntArray())
    }
}
