package com.example.apm.ui.crash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.apm.R

/**
 * Created by cnting on 2023/10/28
 *
 */
class CrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)
        findViewById<Button>(R.id.toNext).setOnClickListener {
            startActivity(Intent(this, CrashActivity2::class.java))
        }
    }
}