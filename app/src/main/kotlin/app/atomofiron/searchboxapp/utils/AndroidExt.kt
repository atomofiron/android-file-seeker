package app.atomofiron.searchboxapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.os.Parcelable
import android.provider.OpenableColumns
import android.util.LayoutDirection
import android.util.TypedValue
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ScrollingView
import androidx.core.view.isEmpty
import androidx.core.view.updateLayoutParams
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import androidx.work.Data
import app.atomofiron.common.util.Android
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeError
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.Serializable

fun Context.findResIdByAttr(@AttrRes attr: Int): Int = findResIdsByAttr(attr)[0]

fun Context.findResIdsByAttr(@AttrRes vararg attrs: Int): IntArray {
    @SuppressLint("ResourceType")
    val array = obtainStyledAttributes(attrs)

    val values = IntArray(attrs.size)
    for (i in attrs.indices) {
        values[i] = array.getResourceId(i, 0)
    }
    array.recycle()

    return values
}

fun Context.getColorByAttr(@AttrRes attr: Int): Int = ContextCompat.getColor(this, findResIdByAttr(attr))

fun Context.getAttr(attr: Int, fallbackAttr: Int): Int {
    val value = TypedValue()
    theme.resolveAttribute(attr, value, true)
    return when {
        value.resourceId != 0 -> attr
        else -> fallbackAttr
    }
}

fun <I> ActivityResultLauncher<I>.resolve(context: Context, input: I): Boolean {
    val intent = contract.createIntent(context, input)
    val info = intent.resolveActivity(context.packageManager)
    return info != null
}

fun Context.resolve(intent: Intent): Boolean {
    return packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL).isNotEmpty()
}

fun Context.getMarketIntent() = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))

fun Resources.getString(error: NodeError, content: NodeContent? = null): String {
    return when (error) {
        is NodeError.NoSuchFile -> when (content) {
            is NodeContent.Directory -> getString(R.string.no_such_directory)
            is NodeContent.File -> getString(R.string.no_such_file)
            else -> getString(R.string.no_such_file_or_directory)
        }
        is NodeError.PermissionDenied -> getString(R.string.permission_denied)
        is NodeError.Unknown -> getString(R.string.unknown_error)
        is NodeError.Multiply -> getString(R.string.a_lot_of_errors)
        is NodeError.Message -> error.message
    }
}

const val DEFAULT_FREQUENCY = 60

@Suppress("DEPRECATION")
fun Context.getFrequency(): Int {
    val refreshRate = when {
        SDK_INT >= Build.VERSION_CODES.R -> display.refreshRate
        else -> {
            val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager?
            manager?.defaultDisplay?.refreshRate
        }
    }
    return refreshRate?.toInt() ?: DEFAULT_FREQUENCY
}

fun Drawable.updateState(enabled: Boolean? = null, checked: Boolean? = null, activated: Boolean? = null) {
    val flags = getStateMut(enabled, checked, activated)
    for (flag in state) {
        if (!flags.contains(flag) && !flags.contains(-flag)) {
            flags.add(flag)
        }
    }
    state = flags.toIntArray()
}

fun Drawable.setState(enabled: Boolean? = null, checked: Boolean? = null, activated: Boolean? = null) {
    state = getStateMut(enabled, checked, activated).toIntArray()
}

private fun getStateMut(
    enabled: Boolean? = null,
    checked: Boolean? = null,
    activated: Boolean? = null,
): MutableList<Int> {
    val flags = mutableListOf<Int>()
    enabled?.let { flags.add(android.R.attr.state_enabled * it.toInt()) }
    checked?.let { flags.add(android.R.attr.state_checked * it.toInt()) }
    activated?.let { flags.add(android.R.attr.state_activated * it.toInt()) }
    return flags
}

fun CoordinatorLayout.makeSnackbar(@StringRes message: Int, duration: Int) = makeSnackbar(resources.getText(message), duration)

fun CoordinatorLayout.makeSnackbar(message: CharSequence, duration: Int): Snackbar {
    return Snackbar.make(this, message, duration)
}

val View.isLayoutRtl: Boolean get() = layoutDirection == View.LAYOUT_DIRECTION_RTL

fun View.isRtl(): Boolean = resources.isRtl()

fun Resources.isRtl(): Boolean = configuration.layoutDirection == LayoutDirection.RTL

fun RecyclerView.scrollToTop(): Boolean {
    if (isEmpty()) return false
    val topChild = getChildAt(0)
    val topHolder = getChildViewHolder(topChild)
    if (topHolder.absoluteAdapterPosition == 0) {
        smoothScrollToPosition(0)
        return false
    }
    val spanCount = when (val manager = layoutManager) {
        is GridLayoutManager -> manager.spanCount
        is StaggeredGridLayoutManager -> manager.spanCount
        else -> 1
    }
    scrollToPosition(spanCount)
    post {
        smoothScrollToPosition(0)
    }
    return true
}

val ViewPager2.recyclerView: RecyclerView get() = getChildAt(0) as RecyclerView

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String, clazz: Class<T>): T? = when {
    SDK_INT >= TIRAMISU -> getParcelable(key, clazz)
    else -> getParcelable(key)
}

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Bundle.getSerializableCompat(key: String, clazz: Class<T>): T? = when {
    SDK_INT >= TIRAMISU -> getSerializable(key, clazz)
    else -> getSerializable(key) as T?
}

fun Context.canNotice(): Boolean {
    return SDK_INT < TIRAMISU || checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED
}

inline fun Context.ifCanNotice(action: () -> Unit): Boolean {
    return canNotice().also { if (it) action() }
}

fun Context.canForegroundService(): Boolean {
    return SDK_INT < P || checkSelfPermission(android.Manifest.permission.FOREGROUND_SERVICE) == PERMISSION_GRANTED
}

@Suppress("DEPRECATION")
fun Context.getDisplayCompat(): Display? = when {
    Android.R -> display
    else -> (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
}

fun Context.drawable(@DrawableRes resId: Int): Drawable = ContextCompat.getDrawable(this, resId)!!
// todo replace everywhere
fun Context.color(@ColorRes resId: Int): Int = ContextCompat.getColor(this, resId)

// it was sweaty...
fun ViewParent.disallowInterceptTouches() {
    when (this) {
        // NestedScrollView, RecyclerView...
        is ScrollingView -> requestDisallowInterceptTouchEvent(true)
        else -> parent?.disallowInterceptTouches()
    }
    // предотвращает перехват вертикального скроллинга при горизонтальном слайдинге,
    // но из-за этого временно ломается или скроллинг в MenuView или в NestedScrollView,
    // или в BottomSheetBehavior выше, но только при касании layout/item_explorer.xml
}

fun Data.Builder.putStringArray(key: String, value: Array<out String?>): Data.Builder {
    return putStringArray(key, value as Array<String?>)
}

fun Context.document(uri: Uri) = DocumentFile.fromSingleUri(this, uri)!!

fun Uri.name(context: Context): String? {
    val path = path ?: return null
    if (scheme == Const.SCHEME_CONTENT) {
        context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index != -1 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
    } else if (scheme == Const.SCHEME_FILE) {
        return File(path).name
    }
    return null
}

val View.marginLayoutParams: ViewGroup.MarginLayoutParams get() = layoutParams as ViewGroup.MarginLayoutParams

fun View.updateLayoutParams(width: Int = Int.MIN_VALUE, height: Int = Int.MIN_VALUE) {
    updateLayoutParams {
        if (width != Int.MIN_VALUE) this.width = width
        if (height != Int.MIN_VALUE) this.height = height
    }
}

