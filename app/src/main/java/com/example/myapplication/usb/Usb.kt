package com.example.myapplication.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import android.widget.Toast
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import java.io.UnsupportedEncodingException


class Usb (private val context: Context, private val app: Context){
    lateinit var mUsbManager: UsbManager
    var mDevice: UsbDevice? = null
    var mSerial: UsbSerialDevice? = null
    var mConnection: UsbDeviceConnection? = null

    val ACTION_USB_PERMISSION = "permission"

    fun startUsbConnecting(){
        val usbDevices: HashMap<String, UsbDevice>? = mUsbManager.deviceList
        if(!usbDevices?.isEmpty()!!){ // return true when not null and not empty
            var keep = true
            usbDevices.forEach{ entry ->
                mDevice = entry.value
                val deviceVendorId: Int? = mDevice?.vendorId
                Log.i("Serial", "vendorId: $deviceVendorId")
                if (true){ // https://github.com/mik3y/usb-serial-for-android/blob/master/usbSerialExamples/src/main/res/xml/device_filter.xml
                    val intent: PendingIntent = PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION),0)
                    mUsbManager.requestPermission(mDevice, intent)
                    keep = false
                    Log.i("Serial", "connection successful")
                } else{
                    mConnection = null
                    mDevice = null
                    Log.i("Serial", "unable to connect")
                }
                if (!keep){
                    return
                }
            }
        }
    }

    fun sendData(input:String){
        mSerial?.write(input.toByteArray())
        Toast.makeText(app, "sending $input via usb", Toast.LENGTH_SHORT).show()
        Log.i("Serial", "sending data" + input.toByteArray())
    }

    fun disconnect(){
        mSerial?.close()
    }

    val mCallback: UsbSerialInterface.UsbReadCallback =
        UsbSerialInterface.UsbReadCallback { arg0 ->
            //Defining a Callback which triggers whenever data is read.
            var data: String? = null
            try {
                data = String(arg0, Charsets.UTF_8)
                println(data)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }

    val broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action!! == ACTION_USB_PERMISSION){
                val granted: Boolean = intent.extras!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
                if(granted){
                    Toast.makeText(app, "usb permission granted", Toast.LENGTH_SHORT).show()
                    mConnection = mUsbManager.openDevice(mDevice)
                    mSerial = UsbSerialDevice.createUsbSerialDevice(mDevice, mConnection)
                    if (mSerial != null){
                        if(mSerial!!.open()){
                            mSerial!!.setBaudRate(115200)
                            mSerial!!.setDataBits(UsbSerialInterface.DATA_BITS_8)
                            mSerial!!.setStopBits(UsbSerialInterface.STOP_BITS_1)
                            mSerial!!.setParity(UsbSerialInterface.PARITY_NONE)
                            mSerial!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                            mSerial!!.read(mCallback)
                        }else{
                            Log.i("Serial", "port not open")
                        }
                    } else{
                        Log.i("Serial", "port is null")
                    }
                } else{
                    Log.i("Serial", "permission not granted")
                    Toast.makeText(app, "permission not granted", Toast.LENGTH_SHORT).show()
                }
            } else if(intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED){
                startUsbConnecting()
            } else if(intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED){
                disconnect()
            }
        }
    }
}
