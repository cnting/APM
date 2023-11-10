package com.example.apm.ui.io

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import com.example.apm.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


/**
 * Created by cnting on 2023/11/9
 *
 */
class IOActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_io)
        findViewById<View>(R.id.writeBigFileBtn).setOnClickListener {
            writeBigFile()
        }
        findViewById<View>(R.id.readFileBtn).setOnClickListener {
            readSmallBuffer()
        }
        findViewById<View>(R.id.notCloseBtn).setOnClickListener {
            notCloseFile()
        }

    }

    private fun writeBigFile() {
        val file = File(externalCacheDir, "test.txt")
        val fileOutputStream = FileOutputStream(file)
        val data = ByteArray(512)
        (data.indices).forEach { data[it] = it.toByte() }
        repeat(1000000) {
            fileOutputStream.write(data)
        }
        fileOutputStream.flush()
        fileOutputStream.close()
        Log.d("===>", "写大文件结束")
    }

    private fun readSmallBuffer() {
        val file = File(externalCacheDir, "test.txt")
        val inputStream = FileInputStream(file)
        val buffer = ByteArray(200)
        while (inputStream.read(buffer) != -1) {
            Log.d("===>", "读")
        }
        inputStream.close();
        Log.d("===>", "读大文件结束")
    }

    private fun notCloseFile() {

    }
}