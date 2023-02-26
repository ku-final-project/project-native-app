package com.example.myapplication

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.example.myapplication.camera.CameraManager
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import kotlinx.android.synthetic.main.activity_main.*

@ExperimentalGetImage class MainActivity : AppCompatActivity() {

    // Camera manager
    private lateinit var cameraManager: CameraManager

    // USB device and Manager
    lateinit var m_usbManager: UsbManager
    var m_device: UsbDevice? = null
    var m_serial: UsbSerialDevice? = null
    var m_connection: UsbDeviceConnection? = null

    val ACTION_USB_PERMISSION = "permission"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createCameraManager()
        checkForPermission()
        // hide actionBar and statusBar
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        // Logout button
        logout_button.setOnClickListener{
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            Animatoo.animateSlideRight(this);
        }
        // Config button
        config_button.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://stackoverflow.com/questions/45535272/how-to-link-button-with-website-in-android-studio-using-kotlin"))
            startActivity(intent)
        }

        // USB
        m_usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(broadcastReceiver, filter)
        // Send Serial
        startUsbConnecting()
        serial_button.setOnClickListener{
            sendData("unlock")
        }
    }

    private fun startUsbConnecting(){
        val usbDevices: HashMap<String, UsbDevice>? = m_usbManager.deviceList
        if(!usbDevices?.isEmpty()!!){ // return true when not null and not empty
            var keep = true
            usbDevices.forEach{ entry ->
                m_device = entry.value
                val deviceVendorId: Int? = m_device?.vendorId
                Log.i("Serial", "vendorId: $deviceVendorId")
                if (deviceVendorId == 4292){ // https://github.com/mik3y/usb-serial-for-android/blob/master/usbSerialExamples/src/main/res/xml/device_filter.xml
                    val intent: PendingIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION),0)
                    m_usbManager.requestPermission(m_device, intent)
                    keep = false
                    Log.i("Serial", "connection successful")
                } else{
                    m_connection = null
                    m_device = null
                    Log.i("Serial", "unable to connect")
                }
                if (!keep){
                    return
                }
            }
        }
    }

    private fun sendData(input:String){
        m_serial?.write(input.toByteArray())
        Toast.makeText(applicationContext, "sending $input via usb", Toast.LENGTH_SHORT).show()
        Log.i("Serial", "sending data" + input.toByteArray())
    }

    private fun disconnect(){
        m_serial?.close()
    }

    private val broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action!! == ACTION_USB_PERMISSION){
                val granted: Boolean = intent.extras!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
                if(granted){
                    m_connection = m_usbManager.openDevice(m_device)
                    m_serial = UsbSerialDevice.createUsbSerialDevice(m_device, m_connection)
                    if (m_serial != null){
                        if(m_serial!!.open()){
                            m_serial!!.setBaudRate(9600)
                            m_serial!!.setDataBits(UsbSerialInterface.DATA_BITS_8)
                            m_serial!!.setStopBits(UsbSerialInterface.STOP_BITS_1)
                            m_serial!!.setParity(UsbSerialInterface.PARITY_NONE)
                            m_serial!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                        }else{
                            Log.i("Serial", "port not open")
                        }
                    } else{
                        Log.i("Serial", "port is null")
                    }
                } else{
                    Log.i("Serial", "permission not granted")
                }
            } else if(intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED){
                startUsbConnecting()
            } else if(intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED){
                disconnect()
            }
        }
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
