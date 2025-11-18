package com.jeong.web2app.capture

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.jeong.web2app.core.util.OcrResultBus
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class CaptureActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: Button
    private var imageCapture: ImageCapture? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("CaptureActivity", "onCreate started")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        Log.d("CaptureActivity", "layout inflated successfully")

        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btnCapture)

        Log.d(
            "CaptureActivity",
            "CAMERA checkSelfPermission = ${
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                )
            }"
        )

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("CaptureActivity", "Permission granted. Starting camera.")
            startCamera()
        } else {
            Log.d("CaptureActivity", "Permission NOT granted. Launching permission request.")
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        btnCapture.setOnClickListener {
            capturePhoto()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("CaptureActivity", "startCamera failed", exc)
                Toast.makeText(this, R.string.camera_start_failed, Toast.LENGTH_SHORT).show()
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CaptureActivity,
                        R.string.capture_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("Camera", "Capture failed", exception)
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.toBitmapCompat()
                    image.close()
                    processImage(bitmap)
                    Log.d("Capture", "onCaptureSuccess")
                }
            }
        )
    }

    private fun processImage(bitmap: Bitmap?) {
        if (bitmap == null) {
            Toast.makeText(this, R.string.capture_failed, Toast.LENGTH_SHORT).show()
            return
        }

        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val now = System.currentTimeMillis()
                val json = JSONObject().apply {
                    put("id", "req_${System.currentTimeMillis()}")
                    put("rawText", visionText.text)
                    put("createdAt", now.toString())
                }.toString()

                lifecycleScope.launch {
                    OcrResultBus.post(json)
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.ocr_failed, Toast.LENGTH_SHORT).show()
            }
    }
}

private fun ImageProxy.toBitmapCompat(): Bitmap? {
    return try {
        when (format) {
            ImageFormat.JPEG -> {
                // JPEG 포맷이면 그대로 bitmap 디코딩
                val buffer = planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }

            ImageFormat.YUV_420_888 -> {
                // 3-plane YUV → NV21 변환
                val nv21 = toNv21Safe()
                val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
                val out = ByteArrayOutputStream()
                yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
                val imageBytes = out.toByteArray()
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            }

            else -> null
        }
    } catch (_: Exception) {
        null
    }
}


private fun ImageProxy.toNv21Safe(): ByteArray {
    // JPEG → plane 1개
    if (planes.size == 1 && format == ImageFormat.JPEG) {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return bytes
    }

    // YUV_420_888 → plane 3개
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    return nv21
}

