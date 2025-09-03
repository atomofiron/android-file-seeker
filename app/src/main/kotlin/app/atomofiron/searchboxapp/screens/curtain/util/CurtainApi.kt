package app.atomofiron.searchboxapp.screens.curtain.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.Unique
import app.atomofiron.common.util.Equality
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference
import java.util.*

object CurtainApi {

    interface Controller {
        val requestFrom: Int
        val requestId: Int

        fun setAdapter(adapter: Adapter<*>)
        fun showNext(layoutId: Int)
        fun showPrev()
        fun close(immediately: Boolean = false, irrevocably: Boolean = false)
        fun showSnackbar(string: String, duration: Int)
        fun showSnackbar(stringId: Int, duration: Int)
        fun showSnackbar(provider: SnackbarProvider)
        fun setCancelable(value: Boolean)
    }

    fun interface SnackbarProvider {
        fun getSnackbar(container: ViewGroup): Snackbar
    }

    abstract class Adapter<H : ViewHolder> : Equality by Unique(Unit) {
        companion object {
            private val unused = Any()
        }
        private val holderList = HashMap<Int, H>()
        private var controllerReference = WeakReference<Controller>(null)
        val holders: Map<Int, H> = holderList
        val controller: Controller? get() = controllerReference.get()
        open val data: Any? = unused

        inline fun <reified B : H> holder(): B? = holders.values.find { it is B } as B?

        inline fun <reified B : H> holder(crossinline action: B.() -> Unit) {
            holder<B>()?.run(action)
        }

        inline fun <R> controller(crossinline action: Controller.() -> R): R? = controller?.run(action)

        inline fun <reified B : H> getHolderProvider(): (action: B.() -> Unit) -> Unit = { action -> holder(action) }

        fun setController(controller: Controller?) {
            when (data) {
                null -> controller?.close(immediately = true)
                else -> {
                    this.controllerReference = WeakReference(controller)
                    controller?.setAdapter(this)
                }
            }
        }

        fun drop(layoutId: Int) {
            holderList.remove(layoutId)
        }

        fun clear() = holderList.clear()

        protected abstract fun getHolder(inflater: LayoutInflater, layoutId: Int): H?

        fun getViewHolderOrNull(layoutId: Int): ViewHolder? = holderList[layoutId]

        fun getViewHolder(context: Context, layoutId: Int): ViewHolder? {
            val inflater = LayoutInflater.from(context)
            val holder = holderList[layoutId] ?: getHolder(inflater, layoutId)?.apply {
                holderList[layoutId] = this
            }
            return holder
        }
    }

    open class ViewHolder private constructor(
        val isCancelable: Boolean,
        val view: View,
    ) {
        constructor(
            view: View,
            isCancelable: Boolean = true,
        ) : this(isCancelable, view.makeScrollable())
    }
}

// make the large content scrollable
private fun View.makeScrollable(): View {
    val scrollView = when (this) {
        is NestedScrollView -> this
        is RecyclerView -> this
        else -> NestedScrollView(context).apply {
            this@makeScrollable.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            addView(this@makeScrollable)
        }
    }
    // WRAP_CONTENT is necessary to the horizontal transitions in curtain
    scrollView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    return scrollView
}