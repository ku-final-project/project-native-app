package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import kotlinx.android.synthetic.main.change_pin_page.*
import kotlinx.android.synthetic.main.pin_page.back_button

class ChangePinPage : AppCompatActivity() {
    private lateinit var pin: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_pin_page)
        // hide actionBar and statusBar
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        back_button.setOnClickListener() {
            val intent = Intent(this, PinPage::class.java)
            startActivity(intent)
            Animatoo.animateSlideRight(this)
        }

        var pinPref: SharedPreferences? = getSharedPreferences("pin", MODE_PRIVATE)
        var editor: SharedPreferences.Editor? = pinPref?.edit()

        var oldPin = pinPref?.getString("PIN", "").toString()

        save_button.setOnClickListener {
            if (oldPin != old_password.text.toString()) {
                Toast.makeText(this, "invalid old password", Toast.LENGTH_SHORT).show()
            } else if (new_password.text.toString() != confirm_password.text.toString()) {
                Toast.makeText(this, "new password and confirm password not equal", Toast.LENGTH_SHORT).show()
            } else if (old_password.text.toString() == new_password.text.toString()) {
                Toast.makeText(this, "can't use old password", Toast.LENGTH_SHORT).show()
            } else {
                editor?.putString("PIN", new_password.text.toString())
                val success = editor?.commit()
                if (success == true) {
                    Toast.makeText(this, "success save new pin", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "fail save new pin", Toast.LENGTH_SHORT).show()
                }
                val intent = Intent(this, PinPage::class.java)
                startActivity(intent)
                Animatoo.animateSlideLeft(this)
            }
        }
    }
}
