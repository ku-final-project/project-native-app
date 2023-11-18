package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.example.myapplication.api.ApiService
import com.example.myapplication.camera.CameraManager
import com.example.myapplication.usb.Usb
import kotlinx.android.synthetic.main.face_detection_page.config_button
import kotlinx.android.synthetic.main.face_detection_page.graphicOverlay_finder
import kotlinx.android.synthetic.main.face_detection_page.previewView_finder
import kotlinx.android.synthetic.main.face_detection_page.serial_button

class FaceDetectionPage : AppCompatActivity() {

    // Camera manager
    private lateinit var cameraManager: CameraManager
    // USB import class
    private lateinit var usb: Usb
    // API Service
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.face_detection_page)
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
        createApiService()
        // Camera --------------------------------------------------------------------------------------------
        createCameraManager()
        checkForPermission()

        // hide actionBar and statusBar--------------------------------------------------------------------------------------------
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        // Config button
        config_button.setOnClickListener{
            val intent = Intent(this, PinPage::class.java)
            startActivity(intent)
            Animatoo.animateSlideLeft(this)
        }

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    private fun createUsb(){
        usb = Usb(this, applicationContext)
    }

    private fun createApiService(){
        apiService = ApiService(this)
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
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun createCameraManager() {
        cameraManager = CameraManager(
            this,
            previewView_finder,
            this,
            graphicOverlay_finder,
            usb,
            apiService
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
