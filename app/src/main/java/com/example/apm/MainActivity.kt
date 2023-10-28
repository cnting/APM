package com.example.apm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import com.example.apm.ui.crash.CrashActivity
import com.example.apm.ui.trace.TraceActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.toTrace).setOnClickListener {
            startActivity(Intent(this, TraceActivity::class.java))
        }
        findViewById<View>(R.id.toCrash).setOnClickListener {
            startActivity(Intent(this, CrashActivity::class.java))
        }

    }

}
