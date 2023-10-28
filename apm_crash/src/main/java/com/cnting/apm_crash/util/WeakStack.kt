package com.cnting.apm_crash.util

import android.util.Log
import java.lang.ref.WeakReference
import java.util.EmptyStackException

/**
 * Created by cnting on 2023/10/28
 *
 */
class WeakStack<T> : AbstractCollection<T>() {

    private val list = mutableListOf<WeakReference<T>>()

    override val size: Int
        get() {
            cleanup()
            return list.size
        }

    override fun iterator(): Iterator<T> {
        return WeakIterator(list.reversed().iterator())
    }

    private fun cleanup() {
        list.removeIf { it.get() == null }
    }

    override fun contains(element: T): Boolean {
        return list.find { it.get() == element } != null
    }

    fun add(t: T): Boolean {
        return list.add(WeakReference(t))
    }

    fun remove(t: T): Boolean {
        return list.removeIf { it.get() == t }
    }

    /**
     * 从集合的尾部取出弱引用对象
     */
    fun peek(): T {
        val t = list.reversed().firstOrNull { it.get() != null }?.run { get() }
        return t ?: throw EmptyStackException()
    }

    fun pop(): T {
        val t = peek()
        remove(t)
        return t
    }

    fun clear() {
        list.clear()
    }

    inner class WeakIterator<T>(private val iterator: Iterator<WeakReference<T>>) : Iterator<T> {
        private var next: T? = null
        override fun hasNext(): Boolean {
            if (next != null) {
                return true;
            }
            while (iterator.hasNext()) {
                val t = iterator.next().get()
                if (t != null) {
                    next = t
                    return true
                }
            }
            return false
        }

        override fun next(): T {
            var result = next
            next = null
            while (result == null) {
                result = iterator.next().get()
            }
            return result
        }

    }
}