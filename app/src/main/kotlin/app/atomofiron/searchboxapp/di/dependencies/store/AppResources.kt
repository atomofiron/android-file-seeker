package app.atomofiron.searchboxapp.di.dependencies.store

import android.content.res.Resources
import androidx.annotation.StringRes
import app.atomofiron.common.util.property.MutableStrongProperty
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.utils.getString

interface Strings {
    operator fun get(@StringRes resId: Int): String
    operator fun get(error: NodeError): String
}

class AppResources(resources: Resources) : MutableStrongProperty<Resources>(resources), Strings {

    operator fun invoke() = value

    override fun get(resId: Int) = value.getString(resId)

    override fun get(error: NodeError): String = value.getString(error)
}
