package com.mobbelldev.camx.analyzer

import android.graphics.Rect
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.mobbelldev.camx.custom.GraphicOverlay
import com.mobbelldev.camx.custom.RectangleOverlay

class CameraAnalyzer(
    private val overlay: GraphicOverlay<*>
) : BaseCameraAnalyzer<List<Face>>() {

    override val graphicOverlay: GraphicOverlay<*>
        get() = overlay

    private val cameraOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.15F)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(cameraOptions)

    private var emotionListener: EmotionListener? = null

    fun setEmotionListener(listener: EmotionListener) {
        emotionListener = listener
    }

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
            .addOnSuccessListener { faces ->
                val emotion = faces.firstOrNull()?.let { getEmotion(it) }
                if (emotion != null) {
                    emotionListener?.onEmotionDetected(emotion)
                } // Notify the listener
                onSuccess(faces, overlay, /* replace with actual rect value */ Rect())
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    private fun getEmotion(face: Face): String {
        val smilingProbability = face.smilingProbability
        val leftEyeOpenProbability = face.leftEyeOpenProbability
        val rightEyeOpenProbability = face.rightEyeOpenProbability

        return when {
            smilingProbability != null && smilingProbability > 0.5 -> "Happy"
            leftEyeOpenProbability != null && leftEyeOpenProbability < 0.2 &&
                    rightEyeOpenProbability != null && rightEyeOpenProbability < 0.2 -> "Closed Eyes"
            // Add more conditions for other emotions
            else -> "Neutral"
        }
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: Exception) {
            Log.e(TAG, "stop : $e")
        }
    }

    override fun onSuccess(results: List<Face>, graphicOverlay: GraphicOverlay<*>, rect: Rect) {
        graphicOverlay.clear()
        results.forEach {
            val faceGraphic = RectangleOverlay(graphicOverlay, it, rect)
            graphicOverlay.add(faceGraphic)
        }
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "onFailure : $e")
    }

    companion object {
        private const val TAG = "Camera Analyzer"
    }
}

interface EmotionListener {
    fun onEmotionDetected(emotion: String)
}