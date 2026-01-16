package app.atomofiron.searchboxapp.utils

import android.Manifest.permission.FOREGROUND_SERVICE
import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.provider.OpenableColumns
import android.util.LayoutDirection
import android.util.TypedValue
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewParent
import android.view.WindowManager
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.core.view.ScrollingView
import androidx.core.view.isEmpty
import androidx.core.view.updateLayoutParams
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import androidx.work.Data
import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.extension.debugFail
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.common.util.extension.unit
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.other.LabeledAction
import app.atomofiron.searchboxapp.model.other.UniText
import app.atomofiron.searchboxapp.model.other.get
import app.atomofiron.searchboxapp.model.other.toUni
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import java.io.File

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

fun Context.showShortToast(text: UniText) = showToast(text, Toast.LENGTH_SHORT)

fun Context.showLongToast(text: UniText) = showToast(text, Toast.LENGTH_LONG)

private fun Context.showToast(text: UniText, duration: Int) = Toast.makeText(this, resources[text], duration).show()

fun UniText.toAlert(
    error: Boolean = false,
    important: Boolean = false,
) = Alert(this, error, important)

fun NodeError.toAlert(
    content: NodeContent? = null,
    important: Boolean = false,
) = toUni(content).toAlert(error = true, important)

fun NodeError.toUni(content: NodeContent? = null): UniText {
    return when (this) {
        is NodeError.NoSuchFileOrDir -> when (content) {
            is NodeContent.Directory -> UniText(R.string.no_such_directory)
            is NodeContent.File -> UniText(R.string.no_such_file)
            else -> UniText(R.string.no_such_file_or_directory)
        }
        is NodeError.PermissionDenied -> UniText(R.string.permission_denied)
        is NodeError.ResourceBusy -> UniText(R.string.resource_busy)
        is NodeError.Unknown -> UniText(R.string.unknown_error)
        is NodeError.Multiply -> UniText(R.string.a_lot_of_errors)
        is NodeError.Message -> message.toUni()
        is NodeError.FileWasChanged -> UniText(R.string.error_file_was_changed)
    }
}

inline fun <reified T : Any> CoordinatorLayout.showSnackbar(
    alert: Alert?,
    action: LabeledAction? = null,
    other: T.() -> UniText,
) = makeSnackbar(alert, action, other)?.show()

fun CoordinatorLayout.showSnackbar(
    alert: Alert?,
    action: LabeledAction? = null,
) = when (alert) {
    is Alert.Uni -> makeSnackbar(alert, action).show()
    else -> debugFail { alert.toString() }
}

inline fun <reified T : Any> CoordinatorLayout.makeSnackbar(
    alert: Alert?,
    action: LabeledAction? = null,
    other: T.() -> UniText,
): Snackbar? {
    val alert = when (alert) {
        is Alert.Uni -> alert
        is T -> Alert.Uni(other(alert), alert.error, alert.important)
        else -> return null
            .also { debugFail { alert.toString() } }
    }
    return makeSnackbar(alert, action)
}

fun CoordinatorLayout.makeSnackbar(
    alert: Alert.Uni,
    action: LabeledAction? = null,
) = makeSnackbar(alert.text, alert.error, alert.important, action)

private fun CoordinatorLayout.makeSnackbar(
    text: UniText,
    error: Boolean,
    important: Boolean,
    action: LabeledAction?,
): Snackbar {
    val duration = if (important) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
    return Snackbar.make(this, resources[text], duration).apply {
        if (error) {
            setBackgroundTint(view.context.colorAttr(MaterialAttr.colorError))
            setTextColor(view.context.colorAttr(MaterialAttr.colorOnError))
            setActionTextColor(view.context.colorAttr(MaterialAttr.colorErrorContainer))
        }
        when {
            action != null -> setAction(resources[action.label]) { action.action?.invoke() }
            important -> setAction(R.string.got_it) { }
        }
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

val View.isLayoutRtl: Boolean get() = layoutDirection == View.LAYOUT_DIRECTION_RTL

fun View.isRtl(): Boolean = resources.isRtl()

fun View.inflater(): LayoutInflater = LayoutInflater.from(context)

inline operator fun <reified P : LayoutParams> View.invoke() = layoutParams as P

inline operator fun <reified P : LayoutParams> View.invoke(action: P.() -> Unit) = action(this<P>())

fun Resources.isRtl(): Boolean = configuration.layoutDirection == LayoutDirection.RTL

val TextView.drawableStart: Drawable? get() = compoundDrawablesRelative[0]

val TextView.drawableTop: Drawable? get() = compoundDrawablesRelative[1]

val TextView.drawableEnd: Drawable? get() = compoundDrawablesRelative[2]

val TextView.drawableBottom: Drawable? get() = compoundDrawablesRelative[3]

var TextView.drawableTintList: ColorStateList? get() = compoundDrawableTintList
    @SuppressLint("UseCompatTextViewDrawableApis") // SDK_INT >= 24: textView.setCompoundDrawableTintList(tint);
    set(value) { compoundDrawableTintList = value }

fun TextView.updateDrawables(
    start: Drawable? = compoundDrawablesRelative[0],
    top: Drawable? = compoundDrawablesRelative[1],
    end: Drawable? = compoundDrawablesRelative[2],
    bottom: Drawable? = compoundDrawablesRelative[3],
) = setCompoundDrawablesRelative(start, top, end, bottom)

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

fun RecyclerView.postToPosition(index: Int) = post {
    scrollToPosition(index)
}.unit()

val ViewPager2.recyclerView: RecyclerView get() = getChildAt(0) as RecyclerView

fun Context.granted(permission: String) = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

fun Context.canPostNotifications() = Android.Below.T || granted(POST_NOTIFICATIONS)

fun Context.canManageFiles() = when {
    Android.R -> Environment.isExternalStorageManager()
    else -> granted(WRITE_EXTERNAL_STORAGE)
}

inline fun Context.ifCanNotice(action: () -> Unit): Boolean {
    return canPostNotifications().also { if (it) action() }
}

fun Context.canForegroundService(): Boolean {
    return (Android.Below.O || granted(FOREGROUND_SERVICE)) && (Android.Below.P || granted(FOREGROUND_SERVICE))
}

@Suppress("DEPRECATION")
fun Context.getDisplayCompat(): Display? = when {
    Android.R -> display
    else -> (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
}

fun Context.drawable(@DrawableRes resId: Int): Drawable = ContextCompat.getDrawable(this, resId)!!
// todo replace everywhere
fun Context.color(@ColorRes resId: Int): Int = ContextCompat.getColor(this, resId)

fun Resources.pxs(@DimenRes resId: Int): Int = getDimensionPixelSize(resId)

fun Resources.pxf(@DimenRes resId: Int): Float = getDimension(resId)

fun Context.colorAttr(@AttrRes attrId: Int): Int {
    val typedValue = TypedValue()
    debugRequire(theme.resolveAttribute(attrId, typedValue, true)) {
        return Color.MAGENTA
    }
    return when (typedValue.resourceId) {
        0 -> typedValue.data
        else -> ContextCompat.getColor(this, typedValue.resourceId)
    }
}

private val hsl = FloatArray(3)

fun Int.inverseColor(): Int {
    ColorUtils.colorToHSL(this, hsl)
    hsl[2] = 1f - hsl[2]
    return ColorUtils.HSLToColor(hsl)
}

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
        if (width == this.width && height == this.height) {
            return
        }
        if (width != Int.MIN_VALUE) this.width = width
        if (height != Int.MIN_VALUE) this.height = height
    }
}

var Slider.intValue: Int
    get() = value.toInt()
    set(value) { this.value = value.toFloat() }

fun View.addOnAttachListener(
    oneTime: Boolean = false,
    onDetach: (() -> Unit)? = null,
    onAttach: (() -> Unit)? = null,
) {
    if (onAttach == null && onDetach == null) {
        return
    }
    if (onAttach != null && isAttachedToWindow) {
        onAttach()
        if (oneTime) return
    }
    val listener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            onAttach?.invoke()
            if (oneTime && onAttach != null) {
                removeOnAttachStateChangeListener(this)
            }
        }
        override fun onViewDetachedFromWindow(v: View) {
            onDetach?.invoke()
            if (oneTime && onDetach != null) {
                removeOnAttachStateChangeListener(this)
            }
        }
    }
    addOnAttachStateChangeListener(listener)
}

fun WebView.secureLoad(url: String) {
    val uri = url.toUri()
    val url = when (uri.scheme) {
        "http" -> uri.buildUpon()
            .scheme("https")
            .build()
            .toString()
        else -> url
    }
    loadUrl(url)
}

fun ValueAnimator.setFloatValues(start: Float, vararg stepsToPoint: Pair<Int, Float>) {
    buildList(stepsToPoint.sumOf { it.first }.inc()) {
        add(start)
        var prev = start
        for (period in stepsToPoint) {
            val dif = period.second - prev
            for (i in 1..period.first) {
                add(prev + dif / period.first * i)
            }
            prev = period.second
        }
        setFloatValues(*toFloatArray())
    }
}
