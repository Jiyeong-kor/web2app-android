package com.jeong.web2app.capture

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class CaptureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        val btnCapture = findViewById<Button>(R.id.btnCapture)
        btnCapture.setOnClickListener {
            // TODO: 나중에 CameraX + OCR 넣을 예정
            finish()
        }
    }
}
