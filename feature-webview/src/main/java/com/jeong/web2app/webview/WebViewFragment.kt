package com.jeong.web2app.webview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.jeong.web2app.core.navigation.CameraLauncher

class WebViewFragment : Fragment() {

    companion object {
        const val PWA_URL = "https://example.com"
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

        // 호스트 Activity가 CameraLauncher 를 구현한다고 가정
        val launcher = activity as? CameraLauncher
            ?: error("Host Activity must implement CameraLauncher")

        wv.addJavascriptInterface(
            AndroidJsBridge(launcher),
            "Android"
        )

        wv.webChromeClient = WebChromeClient()
        wv.webViewClient = WebViewClient()
        wv.loadUrl(PWA_URL)
    }

    override fun onDestroyView() {
        webView = null
        super.onDestroyView()
    }
}
