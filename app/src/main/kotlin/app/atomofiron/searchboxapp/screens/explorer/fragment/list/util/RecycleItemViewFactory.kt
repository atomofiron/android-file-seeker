package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.asynclayoutinflater.view.AsyncLayoutInflater

class RecycleItemViewFactory(
    context: Context,
    private val layoutId: Int,
) : AsyncLayoutInflater.OnInflateFinishedListener {
    private val asyncInflater = AsyncLayoutInflater(context)
    private val inflater = LayoutInflater.from(context)
    private val views = mutableListOf<View>()

    private var inProgress = 0
    private var limit = 8

    fun setLimit(value: Int) {
        limit = value
    }

    fun getOrCreate(layoutId: Int, parent: ViewGroup): View {
        if (layoutId == this.layoutId) {
            val view = views.removeLastOrNull()
            generate(layoutId, parent)
            if (view != null) return view
        }
        return inflater.inflate(layoutId, parent, false)
    }

    fun generate(layoutId: Int, parent: ViewGroup) {
        val already = views.size + inProgress
        if (already >= limit) return
        for (i in already until limit) {
            inProgress++
            asyncInflater.inflate(layoutId, parent, this)
        }
    }

    override fun onInflateFinished(view: View, resid: Int, parent: ViewGroup?) {
        inProgress--
        views.add(view)
    }
}