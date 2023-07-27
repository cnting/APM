package com.cnting.apm_trace_canary

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    @Test
    fun testParse(){
        val regex = Regex(">>>>> Dispatching to (?<target>Handler.*}) (?<callback>.*): (?<what>\\d)")
        val msg = ">>>>> Dispatching to Handler (android.view.ViewRootImpl) {3346d43} com.example.test.MainActivity$1@7250fab: 0"
        val result = regex.find(msg)

        println("===>target: ${result?.groups?.get("target")?.value}")
        println("===>callback: ${result?.groups?.get("callback")?.value}")
        println("===>what: ${result?.groups?.get("what")?.value}")

        result?.groups?.forEach {
            println("===>${it?.value}")
        }
    }
}