package app.atomofiron.searchboxapp.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.LayoutDirection
import android.util.TypedValue
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ScrollingView
import androidx.core.view.forEachIndexed
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import androidx.work.Data
import app.atomofiron.common.util.Android
import app.atomofiron.searchboxapp.MaterialId
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.drawable.BallsDrawable.Companion.setBallsDrawable
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeError
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import java.io.Serializable
import java.util.Locale
import kotlin.math.max

fun Resources.getLocale(): Locale = configuration.locales.get(0)

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

fun Context.getPackageUri(): Uri = Uri.parse("package:$packageName")

fun Context.getMarketUrl(): String = "https://play.google.com/store/apps/details?id=$packageName"

fun <T> List<T>.subListLastReversedWhile(predicate: (T) -> Boolean): List<T> {
    val list = ArrayList<T>()
    loop@ for (i in 0 until size) {
        val item = this[i]
        when {
            predicate(item) -> list.add(item)
            else -> break@loop
        }
    }
    return list
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

fun Context.fixChannel(id: String, name: String, importance: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = NotificationManagerCompat.from(this)
        var channel = manager.getNotificationChannel(id)
        if (channel == null) {
            channel = NotificationChannel(id, name, importance)
            manager.createNotificationChannel(channel)
        }
    }
}

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

fun NavigationBarView.updateItem(itemId: Int, iconId: Int, title: String?, enabled: Boolean? = null) {
    updateItem(itemId, iconId, null, title, enabled)
}

fun NavigationBarView.updateItem(itemId: Int, icon: Drawable, title: String?, enabled: Boolean? = null) {
    updateItem(itemId, 0, icon, title, enabled)
}

private fun NavigationBarView.updateItem(itemId: Int, iconId: Int, icon: Drawable?, title: String?, enabled: Boolean?) {
    val menuView = getChildAt(0) as ViewGroup
    menu.forEachIndexed { index, item ->
        if (item.itemId != itemId) return@forEachIndexed
        val itemView = menuView.getChildAt(index)
        val drawable = when {
            icon != null -> icon
            iconId == R.drawable.progress_loop -> {
                val iv = itemView.findViewById<ImageView>(MaterialId.navigation_bar_item_icon_view)
                iv.setBallsDrawable()
            }
            else -> ContextCompat.getDrawable(context, iconId)
        }
        enabled?.let { item.isEnabled = enabled }
        item.icon = drawable
        item.title = title
    }
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

fun View.setContentMaxWidthRes(resId: Int) = setContentMaxWidth(resources.getDimensionPixelSize(resId))

fun View.setContentMaxWidth(value: Int) {
    var currentInset = 0
    addOnLayoutChangeListener { view, left, _, right, _, _, _, _, _ ->
        val width = right - left
        val inset = max(0, width - value) / 2
        val paddingLeft = paddingLeft - currentInset + inset
        val paddingRight = paddingRight - currentInset + inset
        currentInset = inset
        if (this.paddingLeft != paddingLeft || this.paddingRight != paddingRight) {
            view.updatePadding(left = paddingLeft, right = paddingRight)
        }
    }
}

inline fun Context.obtainStyledAttributes(
    attrs: AttributeSet?,
    @StyleableRes styleable: IntArray,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
    action: TypedArray.() -> Unit,
) {
    val styled = obtainStyledAttributes(attrs, styleable, defStyleAttr, defStyleRes)
    action(styled)
    styled.recycle()
}

val View.isLayoutRtl: Boolean get() = layoutDirection == View.LAYOUT_DIRECTION_RTL

fun View.isRtl(): Boolean = resources.isRtl()

fun Resources.isRtl(): Boolean = configuration.layoutDirection == LayoutDirection.RTL


fun RecyclerView.scrollToTop() {
    if (childCount == 0) return
    val topChild = getChildAt(0)
    val topHolder = getChildViewHolder(topChild)
    if (topHolder.absoluteAdapterPosition == 0) {
        smoothScrollToPosition(0)
        return
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
}

val ViewPager2.recyclerView: RecyclerView get() = getChildAt(0) as RecyclerView

inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String, clazz: Class<T>): T? = when {
    SDK_INT >= TIRAMISU -> getParcelable(key, clazz)
    else -> getParcelable(key)
}

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

fun Context.getDisplayCompat(): Display? = when {
    Android.R -> display
    else -> (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
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
