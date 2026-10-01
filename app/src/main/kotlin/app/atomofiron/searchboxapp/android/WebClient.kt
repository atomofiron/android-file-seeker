package app.atomofiron.searchboxapp.android

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebClient @Inject constructor()  : WebViewClient() {

    private var onFinished: (() -> Unit)? = null

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = false

    override fun onPageFinished(view: WebView, url: String) {
        onFinished?.invoke()
        onFinished = null
    }

    fun onFinished(action: () -> Unit) {
        onFinished = action
    }
}