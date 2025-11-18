package com.jeong.web2app

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
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

        onBackPressedDispatcher.addCallback(this) {
            val currentFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? WebViewFragment

            val handled = currentFragment?.handleOnBackPressed() ?: false
            if (!handled) {
                finish()
            }
        }
    }

    override fun launchCamera() {
        val intent = Intent(this, CaptureActivity::class.java)
        startActivity(intent)
    }
}
