package com.example.myapplication.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
    fun webbPostImage(pic:String, face_id:String): JSONObject? {
        var config: SharedPreferences? = context.getSharedPreferences("config", AppCompatActivity.MODE_PRIVATE)
        var apiUrl = config?.getString("API_URL", "")
        var token = config?.getString("TOKEN", "")
        var eventId = config?.getString("EVENT_ID", "")
        Log.i("config", apiUrl!!)
        Log.i("config", token!!)
        Log.i("config", eventId!!)
        val web = Webb.create()
        val result = web.post(apiUrl)
            .header("Content-Type", "application/json")
            .body(
                JSONObject(
                    mapOf("pic" to pic, "face_id" to face_id)
                ).toString()
            )
            .ensureSuccess()
            .asJsonObject()
            .body
        Log.i("Webb API", result.toString())
        return result
    }
}
