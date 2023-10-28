package com.example.apm.ui.crash

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.apm.R

/**
 * Created by cnting on 2023/10/28
 *
 */
class CrashActivity2 : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash2)
        findViewById<Button>(R.id.toCrash).setOnClickListener {
            Integer.parseInt("0x01")
        }
    }
}