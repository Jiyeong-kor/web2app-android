package com.jeong.web2app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.jeong.web2app.core.navigation.CameraLauncher
import com.jeong.web2app.capture.CaptureActivity
import com.jeong.web2app.webview.WebViewFragment

class MainActivity : AppCompatActivity(), CameraLauncher {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragmentContainer, WebViewFragment())
            }
        }
    }

    override fun launchCamera() {
        val intent = Intent(this, CaptureActivity::class.java)
        startActivity(intent)
    }
}
