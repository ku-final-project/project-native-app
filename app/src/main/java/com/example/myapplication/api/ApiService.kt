package com.example.myapplication.api

import android.util.Log
import com.goebl.david.Webb
import org.json.JSONObject

class ApiService {
    fun webbGetHello() {
        val web = Webb.create()
        val result = web.get("https://mock-ku-api.tawanchai-champ.workers.dev/hello")
            .ensureSuccess()
            .asJsonObject()
            .body
        Log.i("Webb API", result.toString())
    }

    fun webbPostImage(pic: String, face_id: String): JSONObject? {
        val web = Webb.create()
        val result = web.post("https://mock-ku-api.tawanchai-champ.workers.dev/upload")
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

    fun authTerminal(payload: JSONObject): JSONObject? {
        val httpClient = Webb.create()
        return httpClient.post("https://mock-ku-api.tawanchai-champ.workers.dev/login")
            .header("Content-Type", "application/json")
            .body(
                payload.toString()
            )
            .ensureSuccess()
            .asJsonObject()
            .body
    }
}
