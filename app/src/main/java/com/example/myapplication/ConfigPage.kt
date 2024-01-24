package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import kotlinx.android.synthetic.main.config_page.apiUrlInput
import kotlinx.android.synthetic.main.config_page.delaySleepTimeDropDown
import kotlinx.android.synthetic.main.config_page.ipMCUInput
import kotlinx.android.synthetic.main.config_page.save_config_button
import kotlinx.android.synthetic.main.config_page.tokenInput
import kotlinx.android.synthetic.main.config_page.shakeTimesDropDown
import kotlinx.android.synthetic.main.pin_page.back_button
import android.widget.ArrayAdapter
import android.widget.Spinner
import kotlinx.android.synthetic.main.config_page.view.apiUrlInput

class ConfigPage : AppCompatActivity() {
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
        val shakeTimes = config?.getString("SHAKE_TIMES", "0")
        val delaySleepTime = config?.getString("DELAY_SLEEP_TIME", "0")
        val ipMCU = config?.getString("IP_MCU", "")

        apiUrlInput.text = apiUrl?.toEditable()
        tokenInput.text = token?.toEditable()
        ipMCUInput.text = ipMCU?.toEditable()

        val shakeTimesList = listOf("0", "1", "2", "3", "4", "5")
        var newShakeTimes = ""
        val shakeTimesadapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, shakeTimesList)
        shakeTimesadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        shakeTimesDropDown.adapter = shakeTimesadapter
        shakeTimesDropDown.setSelection(shakeTimes!!.toInt())
        shakeTimesDropDown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                newShakeTimes = parent?.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val delaySleepTimeList = listOf("10", "20", "30", "40", "50", "60")
        var newDelaySleepTime = ""
        val delaySleepTimeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, delaySleepTimeList)
        delaySleepTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        delaySleepTimeDropDown.adapter = delaySleepTimeAdapter
        delaySleepTimeDropDown.setSelection(delaySleepTime!!.toInt()/10 - 1)
        delaySleepTimeDropDown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                newDelaySleepTime = parent?.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        save_config_button.setOnClickListener{
            editor?.putString("API_URL", apiUrlInput.text.toString())
            editor?.putString("TOKEN", tokenInput.text.toString())
            editor?.putString("SHAKE_TIMES", newShakeTimes)
            editor?.putString("DELAY_SLEEP_TIME", newDelaySleepTime)
            editor?.putString("IP_MCU", ipMCUInput.text.toString())
            val success = editor?.commit()
            if (success == true) {
                Toast.makeText(this, "success save config", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "fail save config", Toast.LENGTH_SHORT).show()
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