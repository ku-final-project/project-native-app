package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import kotlinx.android.synthetic.main.face_detection.*
import com.example.myapplication.api.ApiService
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

@ExperimentalGetImage class MainActivity2 : AppCompatActivity() {
    private lateinit var apiService: ApiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.face_detection)
        createApiService()
        // hide actionBar and statusBar
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        login_button.setOnClickListener{
            val scope = CoroutineScope(Dispatchers.IO + CoroutineName("MyScope"))
            var response: JSONObject? = null;
            scope.launch(Dispatchers.IO) {
                response = apiService.authTerminal(JSONObject(
                    mapOf("baseUrlAPI" to baseurlapi.text.toString(),
                        "eventId" to eventid.text.toString(),
                        "token" to token.text.toString() )
                ))
                Log.i("Response", response.toString())
                if (response != null) {
                    val intent = Intent(this@MainActivity2, MainActivity::class.java)
                    startActivity(intent)
                    Animatoo.animateSlideLeft(this@MainActivity2)
                }else{
                    Toast.makeText(applicationContext, "response is null", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun createApiService(){
        apiService = ApiService()
    }
}
