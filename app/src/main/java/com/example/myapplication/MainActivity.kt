package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.example.myapplication.api.ApiService
import com.example.myapplication.camera.CameraManager
import com.example.myapplication.usb.Usb
import com.goebl.david.Webb
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


@ExperimentalGetImage class MainActivity : AppCompatActivity() {

    // Camera manager
    private lateinit var cameraManager: CameraManager
    // USB import class
    private lateinit var usb: Usb
    // API Service
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Camera --------------------------------------------------------------------------------------------
        createCameraManager()
        checkForPermission()

        // hide actionBar and statusBar--------------------------------------------------------------------------------------------
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        // Logout button
        logout_button.setOnClickListener{
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            Animatoo.animateSlideRight(this)
        }
        // Config button
        config_button.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://stackoverflow.com/questions/45535272/how-to-link-button-with-website-in-android-studio-using-kotlin"))
            startActivity(intent)
        }
        // USB import class assign --------------------------------------------------------------------------------------------
        createUsb()
        // USB
        usb.mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val filter = IntentFilter()
        filter.addAction(usb.ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(usb.broadcastReceiver, filter)
        // Start connecting usb
        usb.startUsbConnecting()
        // Send Serial
        serial_button.setOnClickListener{
            usb.sendData("unlock")
        }
        // API Service--------------------------------------------------------------------------------------------
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        createApiService()
//        apiService.webbGetHello()
//        apiService.webbPostImage(apiService.pic_base64(), "1")
    }

    private fun webbGetHello(){
        val web = Webb.create()
        val result = web.get("http://tawanchaiserver.ddns.net:8001/")
            .ensureSuccess()
            .asJsonObject()
            .body
        Log.i("Webb API", result.toString())
    }

    private fun webbPostImage(){
        val web = Webb.create()
        val result = web.post("http://tawanchaiserver.ddns.net:8001/upload")
            .header("Content-Type", "application/json")
            .body(
                JSONObject(
                    mapOf("pic" to "pic:base64","face_id" to "1")
                ).toString()
            )
            .ensureSuccess()
            .asJsonObject()
            .body
        Log.i("Webb API", result.toString())
    }

    private fun createUsb(){
        usb = Usb(this, applicationContext)
    }

    private fun createApiService(){
        apiService = ApiService()
    }

    private fun checkForPermission() {
        if (allPermissionsGranted()) {
            cameraManager.startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                cameraManager.startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private fun createCameraManager() {
        cameraManager = CameraManager(
            this,
            previewView_finder,
            this,
            graphicOverlay_finder
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}


//typealias LumaListener = (luma: Double) -> Unit

//private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {
//    private fun ByteBuffer.toByteArray(): ByteArray {
//        rewind()    // Rewind the buffer to zero
//        val data = ByteArray(remaining())
//        get(data)   // Copy the buffer into a byte array
//        return data // Return the byte array
//    }
//    override fun analyze(image: ImageProxy) {
//
//        val buffer = image.planes[0].buffer
//        val data = buffer.toByteArray()
//        val pixels = data.map { it.toInt() and 0xFF }
//        val luma = pixels.average()
//
//        listener(luma)
//
//        image.close()
//    }
//}

//class MainActivity : AppCompatActivity() {
//    private lateinit var viewBinding: ActivityMainBinding
//
//    private var imageCapture: ImageCapture? = null
//
//    private var videoCapture: VideoCapture<Recorder>? = null
//    private var recording: Recording? = null
//
//    private lateinit var cameraExecutor: ExecutorService
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        viewBinding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(viewBinding.root)
//        // hide actionBar and statusBar
//        supportActionBar?.hide()
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
//        // Request camera permissions
//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            ActivityCompat.requestPermissions(
//                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
//        }
//
//        // Set up the listeners for take photo and video capture buttons
//        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
//        viewBinding.videoCaptureButton.setOnClickListener { captureVideo() }
//
//        cameraExecutor = Executors.newSingleThreadExecutor()
//
//        // Logout button
//        logout_button.setOnClickListener{
//            val intent = Intent(this, MainActivity2::class.java)
//            startActivity(intent)
//        }
//        // Config button
//        config_button.setOnClickListener{
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://stackoverflow.com/questions/45535272/how-to-link-button-with-website-in-android-studio-using-kotlin"))
//            startActivity(intent)
//        }
//    }

//    private fun takePhoto() {
//        // Get a stable reference of the modifiable image capture use case
//        val imageCapture = imageCapture ?: return
//
//        // Create time stamped name and MediaStore entry.
//        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
//            .format(System.currentTimeMillis())
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
//            }
//        }
//
//        // Create output options object which contains file + metadata
//        val outputOptions = ImageCapture.OutputFileOptions
//            .Builder(contentResolver,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                contentValues)
//            .build()
//
//        // Set up image capture listener, which is triggered after photo has
//        // been taken
//        imageCapture.takePicture(
//            outputOptions,
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onError(exc: ImageCaptureException) {
//                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
//                }
//
//                override fun
//                        onImageSaved(output: ImageCapture.OutputFileResults){
//                    val msg = "Photo capture succeeded: ${output.savedUri}"
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                    Log.d(TAG, msg)
//                }
//            }
//        )
//    }
//
//    private fun captureVideo() {
//        val videoCapture = this.videoCapture ?: return
//
//        viewBinding.videoCaptureButton.isEnabled = false
//
//        val curRecording = recording
//        if (curRecording != null) {
//            // Stop the current recording session.
//            curRecording.stop()
//            recording = null
//            return
//        }
//
//        // create and start a new recording session
//        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
//            .format(System.currentTimeMillis())
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
//            }
//        }
//
//        val mediaStoreOutputOptions = MediaStoreOutputOptions
//            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
//            .setContentValues(contentValues)
//            .build()
//        recording = videoCapture.output
//            .prepareRecording(this, mediaStoreOutputOptions)
//            .apply {
//                if (PermissionChecker.checkSelfPermission(this@MainActivity,
//                        Manifest.permission.RECORD_AUDIO) ==
//                    PermissionChecker.PERMISSION_GRANTED)
//                {
//                    withAudioEnabled()
//                }
//            }
//            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
//                when(recordEvent) {
//                    is VideoRecordEvent.Start -> {
//                        viewBinding.videoCaptureButton.apply {
//                            text = getString(R.string.stop_capture)
//                            isEnabled = true
//                        }
//                    }
//                    is VideoRecordEvent.Finalize -> {
//                        if (!recordEvent.hasError()) {
//                            val msg = "Video capture succeeded: " +
//                                    "${recordEvent.outputResults.outputUri}"
//                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT)
//                                .show()
//                            Log.d(TAG, msg)
//                        } else {
//                            recording?.close()
//                            recording = null
//                            Log.e(TAG, "Video capture ends with error: " +
//                                    "${recordEvent.error}")
//                        }
//                        viewBinding.videoCaptureButton.apply {
//                            text = getString(R.string.start_capture)
//                            isEnabled = true
//                        }
//                    }
//                }
//            }
//    }
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//            // Used to bind the lifecycle of cameras to the lifecycle owner
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//
//            // Preview
//            val preview = Preview.Builder()
//                .build()
//                .also {
//                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
//                }
//            // Capture Image
//            imageCapture = ImageCapture.Builder().build()
//            // Image Analyzer
////            val imageAnalyzer = ImageAnalysis.Builder()
////                .build()
////                .also {
////                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
////                        Log.d(TAG, "Average luminosity: $luma")
////                    })
////                }
//
//            // Video Recorder
//            val recorder = Recorder.Builder()
//                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
//                .build()
//            videoCapture = VideoCapture.withOutput(recorder)
//
//            // Select front camera as a default
//            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
//
//            try {
//                // Unbind use cases before rebinding
//                cameraProvider.unbindAll()
//
//                // Bind use cases to camera (have ImageAnalyzer)
////                cameraProvider.bindToLifecycle(
////                    this, cameraSelector, preview, imageCapture, imageAnalyzer, videoCapture)
//                // Bind use cases to camera (don't have imageAnalyzer)
//                cameraProvider.bindToLifecycle(
//                    this, cameraSelector, preview, imageCapture, videoCapture)
//
//            } catch(exc: Exception) {
//                Log.e(TAG, "Use case binding failed", exc)
//            }
//
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(
//            baseContext, it) == PackageManager.PERMISSION_GRANTED
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        cameraExecutor.shutdown()
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>, grantResults:
//        IntArray) {
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (allPermissionsGranted()) {
//                startCamera()
//            } else {
//                Toast.makeText(this,
//                    "Permissions not granted by the user.",
//                    Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }
//    }
//
//    companion object {
//        private const val TAG = "CameraXApp"
//        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
//        private const val REQUEST_CODE_PERMISSIONS = 10
//        private val REQUIRED_PERMISSIONS =
//            mutableListOf (
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO
//            ).apply {
//                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
//                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                }
//            }.toTypedArray()
//    }
//

//}
