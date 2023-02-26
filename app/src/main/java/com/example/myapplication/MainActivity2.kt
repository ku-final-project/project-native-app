package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import kotlinx.android.synthetic.main.activity_main2.*

@ExperimentalGetImage class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        // hide actionBar and statusBar
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        login_button.setOnClickListener{
            if(username.text.toString()=="1" && password.text.toString()=="1"){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                Animatoo.animateSlideLeft(this);
            }else{
                Toast.makeText(applicationContext, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
