package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import kotlinx.android.synthetic.main.pin_page.*

class PinPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pin_page)
        // hide actionBar and statusBar
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        back_button.setOnClickListener() {
            val intent = Intent(this, FaceDetectionPage::class.java)
            startActivity(intent)
            Animatoo.animateSlideRight(this)
        }

        var pinPref: SharedPreferences? = getSharedPreferences("pin", MODE_PRIVATE)
        var editor: SharedPreferences.Editor? = pinPref?.edit()
        var alreadyChangeFirstPin = false
        if (pinPref?.getString("PIN", "").toString() != "") {
            alreadyChangeFirstPin = true
        }

        if (!alreadyChangeFirstPin!!) {
            confirm_password.visibility = View.VISIBLE
            first_time_password.visibility = View.VISIBLE
            reset_password.visibility = View.INVISIBLE
            login_button.text = "Save"
            login_button.setOnClickListener {
                if (password.text.toString() == confirm_password.text.toString()) {
                    editor?.putString("PIN", password.text.toString())
                    editor?.apply()
                    Toast.makeText(this, "success save pin", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ConfigPage::class.java)
                    startActivity(intent)
                    Animatoo.animateSlideLeft(this)
                } else {
                    Toast.makeText(this, "fail save pin", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            var pin = pinPref?.getString("PIN", "").toString()
            login_button.setOnClickListener {
                if (password.text.toString() == pin) {
                    val intent = Intent(this, ConfigPage::class.java)
                    startActivity(intent)
                    Animatoo.animateSlideLeft(this)
                } else {
                    Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onResetPasswordClick(view: View) {
        val intent = Intent(this, ChangePinPage::class.java)
        startActivity(intent)
        Animatoo.animateSlideLeft(this)
    }
}
