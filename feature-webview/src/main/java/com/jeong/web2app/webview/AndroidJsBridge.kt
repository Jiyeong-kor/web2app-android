package com.jeong.web2app.webview

import android.webkit.JavascriptInterface
import com.jeong.web2app.core.navigation.CameraLauncher

class AndroidJsBridge(
    private val cameraLauncher: CameraLauncher
) {

    @JavascriptInterface
    fun startCamera() {
        cameraLauncher.launchCamera()
    }
}
