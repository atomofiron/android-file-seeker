package app.atomofiron.searchboxapp.screens.main

import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.navigation.CustomNavHostFragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.Const

class MainRouter(activityProperty: WeakProperty<out FragmentActivity>) : BaseRouter(activityProperty) {

    override val currentDestinationId = 0
    override val isCurrentDestination: Boolean = true

    private val fragmentManager: FragmentManager? get() = activity?.supportFragmentManager
        ?.fragments
        ?.firstOrNull()
        ?.let { it as CustomNavHostFragment }
        ?.childFragmentManager

    private val fragments: List<Fragment>? get() = fragmentManager?.fragments

    val lastVisibleFragment get() = fragments.findLastVisibleFragment()

    fun recreateActivity() {
        activity {
            recreate()
        }
    }

    fun showSettings() = navigate(R.id.preferenceFragment)

    fun onBack(soft: Boolean): Boolean {
        val lastVisibleFragment = lastVisibleFragment
        val consumed = lastVisibleFragment?.onBack(soft) == true
        return consumed || navigation {
            navigateUp()
        } ?: false
    }

    fun returnSingle(uri: Uri) {
        activity {
            val data = Intent().apply {
                setDataAndType(uri, contentResolver.getType(uri))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            setResult(RESULT_OK, data)
            finish()
        }
    }

    fun returnMultiple(uris: List<Uri>) {
        activity {
            val clip = ClipData.newUri(contentResolver, Const.SELECTED, uris.first())
            for (i in 1..<uris.size) {
                clip.addItem(ClipData.Item(uris[i]))
            }
            val data = Intent()
            data.clipData = clip
            data.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setResult(RESULT_OK, data)
            finish()
        }
    }
}