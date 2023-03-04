package com.example.myapplication.api

import android.util.Log
import com.goebl.david.Webb
import org.json.JSONObject

class ApiService {
    fun webbGetHello(){
        val web = Webb.create()
        val result = web.get("http://tawanchaiserver.ddns.net:8001/")
            .ensureSuccess()
            .asJsonObject()
            .body
        Log.i("Webb API", result.toString())
    }

    fun webbPostImage(pic:String, face_id:String){
        val web = Webb.create()
        val result = web.post("http://tawanchaiserver.ddns.net:8001/upload")
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
    }
}
