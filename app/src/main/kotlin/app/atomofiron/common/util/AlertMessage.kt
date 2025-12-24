package app.atomofiron.common.util

import androidx.annotation.StringRes
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.other.UniText

sealed interface Alert {
    companion object {
        operator fun invoke(@StringRes stringId: Int, mod: Mod? = null) = Uni(stringId, mod)
        operator fun invoke(message: String, mod: Mod? = null) = Uni(message, mod)
        operator fun invoke(error: NodeError, mod: Mod? = null) = Err(error, mod)
    }

    enum class Mod {
        Important,
        Err,
    }

    val mod: Mod?
    val isErr: Boolean get() = mod == Mod.Err
    val important: Boolean get() = mod != null

    data class Uni(
        val message: UniText,
        override val mod: Mod? = null,
    ) : Alert {
        constructor(message: String, mod: Mod? = null) : this(UniText(message), mod)
        constructor(message: Int, mod: Mod? = null) : this(UniText(message), mod)
    }

    data class Err(val error: NodeError, override val mod: Mod? = null) : Alert

    abstract class Other(override val mod: Mod? = null) : Alert
}

@Suppress("FunctionName")
fun AlertErr(message: String) = Alert.Uni(message, Alert.Mod.Err)

@Suppress("FunctionName")
fun AlertErr(@StringRes message: Int) = Alert.Uni(message, Alert.Mod.Err)
