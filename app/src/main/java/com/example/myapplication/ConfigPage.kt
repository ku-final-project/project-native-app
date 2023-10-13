package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.api.ApiService
import kotlinx.android.synthetic.main.config_page.save_config_button

class ConfigPage : AppCompatActivity(){
    // API Service
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.config_page)
        // hide actionBar and statusBar
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        save_config_button.setOnClickListener{
//            apiService.
        }
    }

    private fun createApiService(){
        apiService = ApiService()
    }
}