package com.mobbelldev.camx

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mobbelldev.camx.analyzer.CameraAnalyzer
import com.mobbelldev.camx.analyzer.EmotionListener
import com.mobbelldev.camx.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), EmotionListener {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var manageCamera: ManageCamera
    private val emotionStickers = mapOf(
        "Happy" to R.drawable.ic_happy,
        "Closed Eyes" to R.drawable.ic_closed_eyes,
        "Neutral" to R.drawable.ic_nope,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        manageCamera = ManageCamera(this, binding.viewCamera, binding.customOverlay, this, this)
        val cameraAnalyzer = CameraAnalyzer(binding.customOverlay)
        cameraAnalyzer.setEmotionListener(this)
        askCameraPermission()

        binding.btnStartCamera.setOnClickListener {
            manageCamera.cameraStart()
            buttonManager(true)
        }

        binding.btnStopCamera.setOnClickListener {
            manageCamera.cameraStop()
            buttonManager(false)
        }

        binding.btnTurnCamera.setOnClickListener {
            manageCamera.changeCamera()
        }
    }

    private fun askCameraPermission() {
        if (arrayOf(android.Manifest.permission.CAMERA).all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            // Start Camera
            manageCamera.cameraStart()
        } else {
            // Request Permission
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Start Camera
            manageCamera.cameraStart()
        } else {
            Toast.makeText(this, "CAMERA PERMISSION DENIED", Toast.LENGTH_SHORT).show()

            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 3000)
        }
    }

    private fun buttonManager(isStart: Boolean) {
        if (isStart) {
            with(binding) {
                btnStopCamera.visibility = View.VISIBLE
                btnStartCamera.visibility = View.GONE
            }
        } else {
            with(binding) {
                btnStopCamera.visibility = View.GONE
                btnStartCamera.visibility = View.VISIBLE
            }
        }
    }

    override fun onEmotionDetected(emotion: String) {
        runOnUiThread {
            val stickersResources = emotionStickers[emotion]
            if (stickersResources != null) {
                with(binding) {
                    ivEmotion.setImageResource(stickersResources)
                    ivEmotion.visibility = View.VISIBLE
                }
            } else {
                binding.viewCamera.visibility = View.GONE
            }
        }
    }
}