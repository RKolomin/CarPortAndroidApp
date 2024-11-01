package ru.carport.app.viewComponents

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class CarPortWebView : WebViewClient() {
//    override fun onPageFinished(view: WebView?, url: String?) {
//        super.onPageFinished(view, url)
//    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//        return super.shouldOverrideUrlLoading(view, request)
        view?.loadUrl(request?.url.toString())
        return true
    }

//    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//        return super.shouldOverrideUrlLoading(view, url)
//        //view?.loadUrl(url);
//        //return true;
//    }

//    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
//        super.onReceivedSslError(view, handler, error)
//    }


}