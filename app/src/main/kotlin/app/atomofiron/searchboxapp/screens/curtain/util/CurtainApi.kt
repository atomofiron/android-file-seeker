package app.atomofiron.searchboxapp.screens.curtain.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.Equality
import app.atomofiron.common.util.Unique
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainId
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainKey
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference

object CurtainApi {

    interface Controller {
        val requestFrom: Int
        val requestId: CurtainId

        fun setAdapter(adapter: Adapter<*>)
        fun showNext(key: CurtainKey)
        fun showPrev()
        fun close(immediately: Boolean = false, irrevocably: Boolean = false)
        fun showSnackbar(alert: Alert.Uni, duration: Int = Snackbar.LENGTH_SHORT)
        fun setCancelable(value: Boolean)
    }

    abstract class Adapter<H : ViewHolder> : Equality by Unique(Unit) {
        private companion object Empty

        private val holderList = HashMap<CurtainId, H>()
        private var controllerReference = WeakReference<Controller>(null)
        val holders: Map<CurtainId, H> = holderList
        val controller: Controller? get() = controllerReference.get()
        open val data: Any? = Empty

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

        fun drop(id: CurtainId) {
            holderList.remove(id)
        }

        fun clear() = holderList.clear()

        protected abstract fun getHolder(inflater: LayoutInflater, id: CurtainId): H?

        fun getViewHolderOrNull(key: CurtainKey): ViewHolder? = holderList[key.id]

        fun getViewHolder(context: Context, id: CurtainId): ViewHolder? {
            val inflater = LayoutInflater.from(context)
            val holder = holderList[id] ?: getHolder(inflater, id)?.apply {
                holderList[id] = this
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
            unsureScrollable: Boolean = true,
            isCancelable: Boolean = true,
        ) : this(isCancelable, if (unsureScrollable) view.makeScrollable() else view)
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