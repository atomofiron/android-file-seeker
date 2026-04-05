package app.atomofiron.searchboxapp.screens.preferences

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.AndroidSdk
import app.atomofiron.common.util.extension.unit
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.android.Intents
import javax.inject.Inject

@PreferenceScope
class PreferenceRouter @Inject constructor(
    fragmentProperty: WeakProperty<out Fragment>,
) : BaseRouter(fragmentProperty) {

    override val currentDestinationId = R.id.preferenceFragment

    fun goToGithub() = context { startActivity(Intents.github) }.unit()

    fun goToForPda() = context { startActivity(Intents.forPda) }.unit()

    fun goToDonate() = context { startActivity(Intents.donate) }.unit()

    fun goTelegram(info: ResolveInfo) = context {
        Intents.telegram.apply {
            component = ComponentName(info.activityInfo.packageName, info.activityInfo.name)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
    }

    fun goToLicenses() = navigate(R.id.licensesFragment)

    @RequiresApi(AndroidSdk.TIRAMISU)
    fun showLocaleSettings() = activity { startActivity(Intents.locales) }
}