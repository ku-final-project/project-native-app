package com.example.myapplication.face_detection

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Base64
import android.util.Log
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
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.max

@ExperimentalGetImage class FaceContourDetectionProcessor(private val view: GraphicOverlay, private val usb: Usb) :
    BaseImageAnalyzer<List<Face>>() {

    private val apiService = ApiService()

    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .build()

    private val detector = FaceDetection.getClient(realTimeOpts)

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
        val results : MutableList<Face> = mutableListOf()
        nonFilterresults.forEach{
            if(it.boundingBox.width() > 100 && it.boundingBox.height() > 100){
                results.add(it)
            }
        }
        results.forEach {
            Log.i("Size_of_rect",it.boundingBox.height().toString());
            Log.i("Size_of_rect",it.boundingBox.width().toString());
            Log.i("FPro", it.toString())
            val faceGraphic = FaceContourGraphic(graphicOverlay, it, rect)
            graphicOverlay.add(faceGraphic)
        }
        if (results.isNotEmpty() && !sending){
            sending = true
            croppedDetectedFace(bitmap!!, results)
        }else if(results.isEmpty()){
            sending = false
        }

        graphicOverlay.postInvalidate()
    }

    private fun croppedDetectedFace(bitmap: Bitmap, results: List<Face>){
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
            if(x + width > bitmap.width) bitmap.width - x else width,
            if(y + height > bitmap.height) bitmap.height - y else height
        )

//        Log.i("CroppedBase64", encodeImage(croppedBitmap).toString())
//        Log.i("OriginalBase64", encodeImage(bitmap).toString())
        // Send API
        if(sending){
            scope.launch(Dispatchers.IO) {
                Log.i("ImageSend", "send image to api url")
                val response = apiService.webbPostImage("data:image/jpeg;base64," + encodeImage(croppedBitmap).toString(), "erk")
                Log.i("Response", response!!["status"].toString())
                if(response!!["status"] as Boolean){
                    usb.sendData("unlock")
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

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Face Detector failed.$e")
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
        private var sending = false
    }

}
