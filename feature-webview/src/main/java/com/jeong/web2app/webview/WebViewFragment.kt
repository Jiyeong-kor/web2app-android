package com.jeong.web2app.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jeong.web2app.core.navigation.CameraLauncher
import com.jeong.web2app.core.util.OcrResultBus
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
class WebViewFragment : Fragment() {

    companion object {
        const val PWA_URL = "https://example.com"
        private const val TAG = "WebViewFragment"
    }

    private var webView: WebView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_webview, container, false)
        webView = view.findViewById(R.id.webView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val wv = webView ?: return

        wv.settings.javaScriptEnabled = true
        wv.settings.domStorageEnabled = true

        val launcher = activity as? CameraLauncher
            ?: error("Host Activity must implement CameraLauncher")

        wv.addJavascriptInterface(
            AndroidJsBridge(launcher),
            "Android"
        )

        wv.webChromeClient = WebChromeClient()
        wv.webViewClient = WebViewClient()
        wv.loadUrl(PWA_URL)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                OcrResultBus.resultFlow.collect { json ->
                    val script = "window.onOcrResult($json)"
                    runCatching {
                        wv.evaluateJavascript(script, null)
                    }.onFailure { throwable ->
                        Log.w(TAG, "Failed to deliver OCR result to web view", throwable)
                    }
                }
            }
        }
    }

    fun handleOnBackPressed(): Boolean {
        val wv = webView ?: return false
        return if (wv.canGoBack()) {
            wv.goBack()
            true
        } else {
            false
        }
    }

    override fun onDestroyView() {
        webView = null
        super.onDestroyView()
    }
}
