package com.vhealth

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.health.connect.client.HealthConnectClient

class SplashActivity : AppCompatActivity() {
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()
        onProceed()
    }

    private fun onProceed() {
        if (HealthConnectClient.isAvailable(this)) {
            // Health Connect is available
            startActivity(Intent(this, PermissionsRationaleActivity::class.java))
            finish()
        } else {
            //health connect app is not installed
            val llError = findViewById<LinearLayout>(R.id.llError)
            llError.visibility = View.VISIBLE
        }
    }

    fun downloadHealthConnectApp(view: View) {
        val uriString =
            "market://details?id=com.google.android.apps.healthdata"
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.android.vending")
                data = Uri.parse(uriString)
                putExtra("overlay", true)
                putExtra("callerId", packageName)
            }
        )
    }
}