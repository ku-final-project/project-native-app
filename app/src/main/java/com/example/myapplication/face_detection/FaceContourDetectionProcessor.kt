package com.example.myapplication.face_detection

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import com.example.myapplication.R
import com.example.myapplication.api.ApiService
import com.example.myapplication.camera.BaseImageAnalyzer
import com.example.myapplication.camera.GraphicOverlay
import com.example.myapplication.usb.Usb
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.android.synthetic.main.face_detection_page.action_request
import kotlinx.android.synthetic.main.face_detection_page.arrow
import kotlinx.android.synthetic.main.face_detection_page.auth_info
import kotlinx.android.synthetic.main.face_detection_page.auth_status
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.random.Random
import android.media.MediaPlayer
import android.net.Uri

@ExperimentalGetImage
class FaceContourDetectionProcessor(
    context: Context,
    private val view: GraphicOverlay,
    private val usb: Usb,
    private val api: ApiService
) :
    BaseImageAnalyzer<List<Face>>() {
    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .build()

    private val detector = FaceDetection.getClient(realTimeOpts)

    private val ctx = context as Activity
    private val window = ctx.window
    private val authStatusTextView: TextView = ctx.auth_status
    private val authInfoTextView: TextView = ctx.auth_info
    private val actionRequest: TextView = ctx.action_request
    private val arrowImage: ImageView = ctx.arrow

    private var lastFaceDetectedTime: Long = 0

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
        val config: SharedPreferences? = ctx.getSharedPreferences("config", AppCompatActivity.MODE_PRIVATE)
        val delaySleepTime = config?.getString("DELAY_SLEEP_TIME", "0")
        if (results.isEmpty()) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastFaceDetectedTime > delaySleepTime?.toInt()!! *1000) {
                adjustScreenBrightness(0f)
            }
        } else {
            lastFaceDetectedTime = System.currentTimeMillis()
            adjustScreenBrightness(1.0f)
        }

        // normal face detect
        if (results.isNotEmpty() && !sending && (results[0].smilingProbability ?: 0f) < 0.9f) {
            isNormal = true
        }

        val shakeTimes = config?.getString("SHAKE_TIMES", "0")
        Log.i("config", shakeTimes.toString())
        // normal face detected and will request shake for SHAKE_TIMES
        Log.i("shake times", isShake.toString())
        Log.i("random", shouldRandom.toString())

        if (results.isNotEmpty() && !sending && isNormal) {
            Log.i("checkFaceShake", checkFaceShake(results[0]).toString())
            if (checkFaceShake(results[0]) && (isShake <= shakeTimes!!.toInt())) {
                isShake++
                authStatusTextView.setTextColor(Color.parseColor("#000000"))
                authStatusTextView.text = "ส่ายหน้าไปแล้ว $isShake / $shakeTimes"
                shouldRandom = true
                hasShake = false
                hasStraight = false
            } else {
                shouldRandom = false
            }
        }

        Log.i("Action1", sending.toString())
        Log.i("Action2", isNormal.toString())
        Log.i("Action3", isShake.toString())

        if (results.isNotEmpty() && !sending && isNormal && (isShake == shakeTimes!!.toInt())) {
            sending = true
            isNormal = false
            isShake = 0
            hasStraight = false
            hasShake = false
            shouldRandom = true
            arrowImage.visibility = View.INVISIBLE
            croppedDetectedFaceAndVerified(bitmap!!, results)
        } else if (results.isEmpty()) {
            sending = false
            isNormal = false
            isShake = 0
            hasStraight = false
            hasShake = false
            shouldRandom = true
            actionRequest.text = ""
            authInfoTextView.text = ""
            authStatusTextView.text = ""
            arrowImage.visibility = View.INVISIBLE
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
        if (sending) {
            scope.launch(Dispatchers.IO) {
                Log.i("ImageSend", "send image to api url")
                val response = api.webbPostImage(
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
                        api.unlockDoor()
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun checkFaceShake(face: Face): Boolean {
        val horizontalAngle = face.headEulerAngleY
        val verticalAngle = face.headEulerAngleX
        if (shouldRandom) {
            while(true){
                val randomInt = Random.nextInt(1, 5)
                if (randomInt != shakeDirection){
                    shakeDirection = randomInt
                    Log.i("random", randomInt.toString())
                    break
                }
            }
            shouldRandom = false
        }
        actionRequest.text = "กรุณาส่ายหน้าตามลูกศร"
        arrowImage.visibility = View.VISIBLE
        when(shakeDirection) {
            1 -> {
                arrowImage.setImageDrawable(ctx.getDrawable(R.drawable.arrow_up))
                checkFaceStraight(horizontalAngle, verticalAngle)
                if (hasStraight && verticalAngle > verticalShakeThreshold) {
                    playSound(true)
                    authStatusTextView.text = ""
                    hasShake = true
                }
                else if(hasStraight && (verticalAngle < -verticalShakeThreshold || horizontalAngle < -horizontalShakeThreshold || horizontalAngle > horizontalShakeThreshold)) {
                    Log.i("invalid_shake", "invalid_shake")
                    playSound(false)
                    authStatusTextView.text = "หันผิดทาง กรุณาเริ่มใหม่"
                    authStatusTextView.setTextColor(Color.parseColor("#FF0000"))
                    shouldRandom = true
                    hasStraight = false
                    isShake = 0
                }
            }
            2 -> {
                arrowImage.setImageDrawable(ctx.getDrawable(R.drawable.arrow_down))
                checkFaceStraight(horizontalAngle, verticalAngle)
                if (hasStraight && verticalAngle < -verticalShakeThreshold) {
                    playSound(true)
                    authStatusTextView.text = ""
                    hasShake = true
                }
                else if(hasStraight && (verticalAngle > verticalShakeThreshold || horizontalAngle < -horizontalShakeThreshold || horizontalAngle > horizontalShakeThreshold)) {
                    Log.i("invalid_shake", "invalid_shake")
                    playSound(false)
                    authStatusTextView.text = "หันผิดทาง กรุณาเริ่มใหม่"
                    authStatusTextView.setTextColor(Color.parseColor("#FF0000"))
                    shouldRandom = true
                    hasStraight = false
                    isShake = 0
                }
            }
            3 -> {
                arrowImage.setImageDrawable(ctx.getDrawable(R.drawable.arrow_right))
                checkFaceStraight(horizontalAngle, verticalAngle)
                if (hasStraight && horizontalAngle < -horizontalShakeThreshold) {
                    playSound(true)
                    authStatusTextView.text = ""
                    hasShake = true
                }
                else if(hasStraight && (verticalAngle < -verticalShakeThreshold || verticalAngle > verticalShakeThreshold || horizontalAngle > horizontalShakeThreshold)) {
                    Log.i("invalid_shake", "invalid_shake")
                    playSound(false)
                    authStatusTextView.text = "หันผิดทาง กรุณาเริ่มใหม่"
                    authStatusTextView.setTextColor(Color.parseColor("#FF0000"))
                    shouldRandom = true
                    hasStraight = false
                    isShake = 0
                }
            }
            4 -> {
                arrowImage.setImageDrawable(ctx.getDrawable(R.drawable.arrow_left))
                checkFaceStraight(horizontalAngle, verticalAngle)
                if (hasStraight && horizontalAngle > horizontalShakeThreshold) {
                    playSound(true)
                    authStatusTextView.text = ""
                    hasShake = true
                }
                else if(hasStraight && (verticalAngle < -verticalShakeThreshold || verticalAngle > verticalShakeThreshold || horizontalAngle < -horizontalShakeThreshold)) {
                    Log.i("invalid_shake", "invalid_shake")
                    playSound(false)
                    authStatusTextView.text = "หันผิดทาง กรุณาเริ่มใหม่"
                    authStatusTextView.setTextColor(Color.parseColor("#FF0000"))
                    shouldRandom = true
                    hasStraight = false
                    isShake = 0
                }
            }
        }

        Log.i("horizontalAngle", horizontalAngle.toString())
        Log.i("verticalAngle", verticalAngle.toString())
        return hasShake && hasStraight
    }

    private fun checkFaceStraight(horizontalAngle: Float, verticalAngle: Float) {
        if (-straightThreshold <= verticalAngle && verticalAngle <= straightThreshold && -straightThreshold <= horizontalAngle && horizontalAngle <= straightThreshold) {
            hasStraight = true
        }
    }

    private fun adjustScreenBrightness(brightness: Float) {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
    }

    private fun playSound(correct: Boolean) {
        val sound = if (correct) R.raw.correct_sound_effect else R.raw.wrong_sound_effect

        CoroutineScope(Dispatchers.IO + CoroutineName("MyScope")).launch {
            val mediaPlayer = MediaPlayer()

            mediaPlayer.setDataSource(ctx, Uri.parse("android.resource://" + ctx.packageName + "/" + sound))
            mediaPlayer.prepare()
            mediaPlayer.start()
            Thread.sleep(500)
        }
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Face Detector failed.$e")
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
        private var sending = false
        private var isNormal = false
        private var isShake = 0
        private var shouldRandom = true
        private var shakeDirection = 1
        private const val horizontalShakeThreshold = 20f
        private const val verticalShakeThreshold = 10f
        private const val straightThreshold = 10f
        private var hasStraight = false
        private var hasShake = false
    }

}
