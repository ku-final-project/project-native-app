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
import kotlinx.android.synthetic.main.config_page.delaySleepTimeInput
import kotlinx.android.synthetic.main.config_page.ipMCUInput
import kotlinx.android.synthetic.main.config_page.save_config_button
import kotlinx.android.synthetic.main.config_page.shakeTimesInput
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

        val config: SharedPreferences? = getSharedPreferences("config", MODE_PRIVATE)
        val editor: SharedPreferences.Editor? = config?.edit()

        val apiUrl = config?.getString("API_URL", "")
        val token = config?.getString("TOKEN", "")
        val eventId = config?.getString("EVENT_ID", "")
        val shakeTimes = config?.getString("SHAKE_TIMES", "0")
        val delaySleepTime = config?.getString("DELAY_SLEEP_TIME", "0")
        val ipMCU = config?.getString("IP_MCU", "")

        apiUrlInput.text = apiUrl?.toEditable()
        tokenInput.text = token?.toEditable()
        shakeTimesInput.text = shakeTimes?.toEditable()
        delaySleepTimeInput.text = delaySleepTime?.toEditable()
        ipMCUInput.text = ipMCU?.toEditable()

        save_config_button.setOnClickListener{
            editor?.putString("API_URL", apiUrlInput.text.toString())
            editor?.putString("TOKEN", tokenInput.text.toString())
            editor?.putString("SHAKE_TIMES", shakeTimesInput.text.toString())
            editor?.putString("DELAY_SLEEP_TIME", delaySleepTimeInput.text.toString())
            editor?.putString("IP_MCU", ipMCUInput.text.toString())
            val success = editor?.commit()
            if (success == true) {
                Toast.makeText(this, "success save ${apiUrlInput.text} ${tokenInput.text} ${shakeTimesInput.text} ${delaySleepTimeInput.text} ${ipMCUInput.text}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "fail save ${apiUrlInput.text} ${tokenInput.text} ${shakeTimesInput.text} ${delaySleepTimeInput.text} ${ipMCUInput.text}", Toast.LENGTH_SHORT).show()
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