package app.atomofiron.searchboxapp.android

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class WebClient : WebViewClient() {

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