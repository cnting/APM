package com.cnting.apm_thread

import android.os.Build
import android.util.Log
import com.cnting.apm_lib.util.ReflectUtils
import com.cnting.apm_lib.util.ThreadUtil
import com.cnting.apm_thread.bean.DeadLockThread

class NativeThreadMonitor {

    companion object {
        init {
            System.loadLibrary("apm_thread")
        }
    }

    fun init() {
        nativeInit(Build.VERSION.SDK_INT)
    }

    private val deadLocks = mutableMapOf<Long, DeadLockThread>()

    /**
     * 监控线程存活时间
     */
    fun monitorThreadAlive() {
        monitorThread()
    }

    /**
     * 死锁检测
     */
    fun checkDeadLock() {
        //1.获取所有线程
        val threads = ThreadUtil.getAllThreads()

        deadLocks.clear()
        //2.对BLOCKED的线程获取锁信息
        threads.forEach {
            if (it.state == Thread.State.BLOCKED) {
                // 当前线程在竞争锁，拿到native thread地址
                val threadNativeAddress = ReflectUtils.get<Long>(it.javaClass, "nativePeer", it)
                if (threadNativeAddress == 0L) {
                    return
                }
                //当前线程在等待锁，锁被另外一个线程占用，要获取该线程
                val blockThreadId = getContendThreadId(threadNativeAddress)
                //native线程获取线程id
                val currentThreadId = getCurrentThreadId(threadNativeAddress)
                deadLocks[currentThreadId] = DeadLockThread(currentThreadId, blockThreadId, it)
            }
        }
        //3.分析死锁信息
        val deadLockThreadGroup = deadLockThreadGroup()
        deadLockThreadGroup.forEach { group ->
            group.keys.forEach {
                val deadLockThread = deadLocks[it] ?: return
                val blockThread = group[deadLockThread.blockThreadId] ?: return
                val waitThread = group[deadLockThread.curThreadId] ?: return
                Log.e("===>", "blockThread = ${blockThread.name}，waitThread = ${waitThread.name}")

                val stackTraceElements: Array<StackTraceElement> = waitThread.stackTrace
                for (stackTraceElement in stackTraceElements) {
                    Log.e("===>", stackTraceElement.toString())
                }
            }
        }
    }

    /**
     * 给死锁线程分组，一个环是一个组
     */
    private fun deadLockThreadGroup(): List<Map<Long, Thread>> {
        val list = mutableListOf<Map<Long, Thread>>()
        val traversalThreadId = mutableSetOf<Long>()
        deadLocks.keys.forEach {
            if (!traversalThreadId.contains(it)) {
                val group = findDeadLockLink(it, mutableMapOf())
                traversalThreadId.addAll(group.keys)
                list.add(group)
            }
        }
        return list
    }

    private fun findDeadLockLink(
        curThreadId: Long,
        map: MutableMap<Long, Thread>
    ): Map<Long, Thread> {
        val thread = deadLocks[curThreadId] ?: return mutableMapOf()
        if (map.contains(curThreadId)) {
            return map
        }
        map[curThreadId] = thread.thread
        return findDeadLockLink(thread.blockThreadId, map)
    }

    /**
     * 获取竞争到锁的线程
     */
    private external fun getContendThreadId(threadNativeAddress: Long): Long

    /**
     * 获取线程id
     */
    private external fun getCurrentThreadId(threadNativeAddress: Long): Long
    private external fun nativeInit(sdkVersion: Int): Int

    /**
     * 监控线程存活
     */
    private external fun monitorThread():Int
}