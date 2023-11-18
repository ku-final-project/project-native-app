package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import kotlinx.android.synthetic.main.config_page.apiUrlInput
import kotlinx.android.synthetic.main.config_page.eventIdInput
import kotlinx.android.synthetic.main.config_page.save_config_button
import kotlinx.android.synthetic.main.config_page.tokenInput
import kotlinx.android.synthetic.main.pin_page.back_button

class ConfigPage : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.config_page)
        // hide actionBar and statusBar
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        back_button.setOnClickListener() {
            val intent = Intent(this, FaceDetectionPage::class.java)
            startActivity(intent)
            Animatoo.animateSlideRight(this)
        }

        var config: SharedPreferences? = getSharedPreferences("config", MODE_PRIVATE)
        var editor: SharedPreferences.Editor? = config?.edit()

        var apiUrl = config?.getString("API_URL", "")
        var token = config?.getString("TOKEN", "")
        var eventId = config?.getString("EVENT_ID", "")

        apiUrlInput.text = apiUrl?.toEditable()
        tokenInput.text = token?.toEditable()
        eventIdInput.text = eventId?.toEditable()

        save_config_button.setOnClickListener{
            editor?.putString("API_URL", apiUrlInput.text.toString())
            editor?.putString("TOKEN", tokenInput.text.toString())
            editor?.putString("EVENT_ID", eventIdInput.text.toString())
            val success = editor?.commit()
            if (success == true) {
                Toast.makeText(this, "success save ${apiUrlInput.text} ${tokenInput.text} ${eventIdInput.text}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "fail save ${apiUrlInput.text} ${tokenInput.text} ${eventIdInput.text}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun String.toEditable(): Editable {
        return Editable.Factory.getInstance().newEditable(this)
    }

    override fun onBackPressed() {
        val intent = Intent(this, FaceDetectionPage::class.java)
        startActivity(intent)
        Animatoo.animateSlideRight(this)
    }
}