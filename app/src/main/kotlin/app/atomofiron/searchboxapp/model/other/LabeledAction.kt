package app.atomofiron.searchboxapp.model.other

class LabeledAction(
    val label: UniText,
    val action: (() -> Unit)? = null,
) {
    constructor(label: String, action: (() -> Unit)? = null) : this(label.toUni(), action)
    constructor(stringId: Int, action: (() -> Unit)? = null) : this(UniText(stringId), action)
}