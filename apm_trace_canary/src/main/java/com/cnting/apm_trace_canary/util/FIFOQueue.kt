package com.cnting.apm_trace_canary.util

import java.util.LinkedList

/**
 * Created by cnting on 2023/7/28
 * 先进先出，队列满了以后移除老的。
 * TODO：不知道有没有现成容器，暂时先用这个
 */
class FIFOQueue<T>(private val capacity: Int) : LinkedList<T>() {

    override fun add(element: T): Boolean {
        while (size >= capacity) {
            removeFirst()
        }
        return super.add(element)
    }
}