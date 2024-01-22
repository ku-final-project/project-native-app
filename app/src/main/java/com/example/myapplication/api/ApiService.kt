package com.example.myapplication.api

import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goebl.david.Webb
import org.json.JSONObject

class ApiService(private val context: Context){
    fun webbGetHello(){
        val web = Webb.create()
        val result = web.get("http://tawanchaiserver.ddns.net:8001/")
            .ensureSuccess()
            .asJsonObject()
            .body
        Log.i("Webb API", result.toString())
    }
    fun unlockDoor(){
        val web = Webb.create()
        val config: SharedPreferences? = context.getSharedPreferences("config", AppCompatActivity.MODE_PRIVATE)
        val ipMCU = config?.getString("IP_MCU", "")
        try {
            val result = web.get("http://${ipMCU}/open-door")
                .ensureSuccess()
                .asString()
            Log.i("Door Web server: ", result.toString())
        } catch (e: com.goebl.david.WebbException) {
            if (Looper.myLooper() == null){
                Looper.prepare()
            }
            Toast.makeText(context, "can't unlock door", Toast.LENGTH_SHORT).show()
            Looper.loop()
        }

    }
    fun webbPostImage(pic:String, face_id:String): JSONObject? {
        val config: SharedPreferences? = context.getSharedPreferences("config", AppCompatActivity.MODE_PRIVATE)
        val apiUrl = config?.getString("API_URL", "")
        val token = config?.getString("TOKEN", "")
        val eventId = config?.getString("EVENT_ID", "")
        Log.i("config", apiUrl!!)
        Log.i("config", token!!)
        Log.i("config", eventId!!)
        val web = Webb.create()
        try {
            val result = web.post(apiUrl)
                .header("Content-Type", "application/json")
                .header("X-Api-Key", token)
                .body(
                    JSONObject(
                        mapOf("pic" to pic, "face_id" to face_id)
                    ).toString()
                )
                .ensureSuccess()
                .asJsonObject()
            Log.i("Webb", result.statusCode.toString())
            return result.body
        } catch (e: com.goebl.david.WebbException) {
            val myMap: MutableMap<String, Boolean> = mutableMapOf()
            myMap["status"] = false
            if (Looper.myLooper() == null){
                Looper.prepare()
            }
            Toast.makeText(context, "can't recognize your face", Toast.LENGTH_SHORT).show()
            Looper.loop()
            return JSONObject((myMap as Map<*, *>?)!!)
        }
    }
}
