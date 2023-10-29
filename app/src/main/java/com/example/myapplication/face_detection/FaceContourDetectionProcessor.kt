package com.example.myapplication.face_detection

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.util.Base64
import android.util.Log
import android.widget.TextView
import androidx.camera.core.ExperimentalGetImage
import com.example.myapplication.api.ApiService
import com.example.myapplication.camera.BaseImageAnalyzer
import com.example.myapplication.camera.GraphicOverlay
import com.example.myapplication.usb.Usb
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.android.synthetic.main.face_detection_page.auth_info
import kotlinx.android.synthetic.main.face_detection_page.auth_status
import kotlinx.android.synthetic.main.face_detection_page.action_request
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.max


@ExperimentalGetImage
class FaceContourDetectionProcessor(
    private val context: Context,
    private val view: GraphicOverlay,
    private val usb: Usb
) :
    BaseImageAnalyzer<List<Face>>() {
    private val apiService = ApiService()

    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .build()

    private val detector = FaceDetection.getClient(realTimeOpts)

    private val ctx = context as Activity
    private val authStatusTextView: TextView = ctx.auth_status
    private val authInfoTextView: TextView = ctx.auth_info
    private val actionRequest: TextView = ctx.action_request

    override val graphicOverlay: GraphicOverlay
        get() = view

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

    override fun onSuccess(
        nonFilterresults: List<Face>,
        graphicOverlay: GraphicOverlay,
        rect: Rect,
        bitmap: Bitmap?
    ) {
        graphicOverlay.clear()
        val results: MutableList<Face> = mutableListOf()
        nonFilterresults.forEach {
            if (it.boundingBox.width() > 100 && it.boundingBox.height() > 100) {
                results.add(it)
            }
        }
        results.forEach {
            Log.i("Size_of_rect", it.boundingBox.height().toString())
            Log.i("Size_of_rect", it.boundingBox.width().toString())
            Log.i("FPro", it.toString())
            val faceGraphic = FaceContourGraphic(graphicOverlay, it, rect)
            graphicOverlay.add(faceGraphic)
        }
        Log.i("FaceResults:", results.toString())
        Log.i("FaceResults:", results.size.toString())

        // normal face detect
        if (results.isNotEmpty() && !sending && (results[0].smilingProbability ?: 0f) < 0.9f) {
            isNormal = true
        }

        // normal face detected and will request shake
        if (results.isNotEmpty() && !sending && isNormal) {
            actionRequest.text = "กรุณาส่ายหน้า"
            isShake = checkFaceShake(results[0])
        }
        Log.i("isNormal", isNormal.toString())

        // normal face detect and shake detected and will request smile
        if (results.isNotEmpty() && !sending && isNormal && isShake) {
            actionRequest.text = "กรุณายิ้มอ่อน"
            isSmile = (results[0].smilingProbability ?: 0f) >= 0.9f
        }

        Log.i("Action1", sending.toString())
        Log.i("Action2", isNormal.toString())
        Log.i("Action3", isShake.toString())
        Log.i("Action4", isSmile.toString())

        if (results.isNotEmpty() && !sending && isNormal && isShake && isSmile) {
            sending = true
            isNormal = false
            isShake = false
            isSmile = false
            hasShakeToLeft = false
            hasShakeToRight = false
            croppedDetectedFaceAndVerified(bitmap!!, results)
        } else if (results.isEmpty()) {
            sending = false
            isNormal = false
            isShake = false
            isSmile = false
            hasShakeToLeft = false
            hasShakeToRight = false
            actionRequest.text = ""
            authInfoTextView.text = ""
            authStatusTextView.text = ""
        }

        graphicOverlay.postInvalidate()
    }

    private fun croppedDetectedFaceAndVerified(bitmap: Bitmap, results: List<Face>) {
        val scope = CoroutineScope(Dispatchers.IO + CoroutineName("MyScope"))
        val rect = results[0].boundingBox
        val x = max(rect.left, 0)
        val y = max(rect.top, 0)
        val width = rect.width()
        val height = rect.height()
        val croppedBitmap = Bitmap.createBitmap(
            bitmap,
            x,
            y,
            if (x + width > bitmap.width) bitmap.width - x else width,
            if (y + height > bitmap.height) bitmap.height - y else height
        )

//        Log.i("CroppedBase64", encodeImage(croppedBitmap).toString())
//        Log.i("OriginalBase64", encodeImage(bitmap).toString())
        // Send API
        Log.i("Smile", results[0].smilingProbability.toString())
        if (sending) {
            scope.launch(Dispatchers.IO) {
                Log.i("ImageSend", "send image to api url")
                val response = apiService.webbPostImage(
                    "data:image/jpeg;base64," + encodeImage(croppedBitmap).toString(),
                    "erk"
                )
                Log.i("Response", response!!["status"].toString())
                withContext(Dispatchers.Main) {
                    authInfoTextView.text = "นายภราดร วัชรเสมากุล"
                    if (response["status"] as Boolean) {
                        authStatusTextView.text = response["status"].toString()
                        authStatusTextView.setTextColor(Color.parseColor("#008000"))
                        authInfoTextView.setTextColor(Color.parseColor("#008000"))
                        usb.sendData("unlock")
                    } else {
                        authStatusTextView.text = response["status"].toString()
                        authStatusTextView.setTextColor(Color.parseColor("#FF0000"))
                        authInfoTextView.setTextColor(Color.parseColor("#FF0000"))
                    }
                }
            }
        }
    }

    private fun encodeImage(bm: Bitmap): String? {
        val byteStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, byteStream)
        val b = byteStream.toByteArray()
        return Base64.encodeToString(b, Base64.NO_WRAP)
    }

    private fun checkFaceShake(face: Face): Boolean {
        val angle = face.headEulerAngleY
        if (angle > shakeThreshold && !hasShakeToLeft) {
            hasShakeToLeft = true
        } else if (angle < -shakeThreshold && !hasShakeToRight) {
            hasShakeToRight = true
        }
        Log.i("Shake", angle.toString())
        return hasShakeToLeft || hasShakeToRight
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Face Detector failed.$e")
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
        private var sending = false
        private var isNormal = false
        private var isShake = false
        private var isSmile = false
        private const val shakeThreshold = 35f
        private var hasShakeToLeft = false
        private var hasShakeToRight = false
    }

}
