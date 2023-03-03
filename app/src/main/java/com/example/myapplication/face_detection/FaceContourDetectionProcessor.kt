package com.example.myapplication.face_detection

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Base64
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import com.example.myapplication.api.ApiService
import com.example.myapplication.camera.BaseImageAnalyzer
import com.example.myapplication.camera.GraphicOverlay
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.max

@ExperimentalGetImage class FaceContourDetectionProcessor(private val view: GraphicOverlay) :
    BaseImageAnalyzer<List<Face>>() {

    val apiService = ApiService()

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
        results: List<Face>,
        graphicOverlay: GraphicOverlay,
        rect: Rect,
        bitmap: Bitmap?
    ) {
        graphicOverlay.clear()
        results.forEach {
            val faceGraphic = FaceContourGraphic(graphicOverlay, it, rect)
            graphicOverlay.add(faceGraphic)
        }
        for(result in results){
            Log.i("FPro", result.toString())
        }
        if (results.isNotEmpty()){
            croppedDetectedFace(bitmap!!, results)
        }
        graphicOverlay.postInvalidate()
    }

    private fun croppedDetectedFace(bitmap: Bitmap, results: List<Face>){
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

        Log.i("CroppedBase64", encodeImage(croppedBitmap).toString())
        Log.i("OriginalBase64", encodeImage(bitmap).toString())

        // Send API
        apiService.webbPostImage("data:image/jpeg;base64," + encodeImage(croppedBitmap).toString(), "erk")
    }

    private fun encodeImage(bm: Bitmap): String? {
        val byteStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 50, byteStream)
        val b = byteStream.toByteArray()
        return Base64.encodeToString(b, Base64.NO_WRAP)
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Face Detector failed.$e")
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
    }

}
