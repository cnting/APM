package com.cnting.apm_trace_canary.core

import android.os.SystemClock
import android.util.Log
import android.view.Choreographer
import com.cnting.apm_lib.lifecycle.owners.ProcessUiLifecycleOwner
import com.cnting.apm_lib.util.ReflectUtils
import com.cnting.apm_trace_canary.constant.Constants
import com.cnting.apm_trace_canary.listener.LooperObserver
import java.lang.reflect.Method

/**
 * Created by cnting on 2023/7/24
 * 监听一帧的执行耗时
 */
object UiThreadMonitor {
    private const val TAG = "UiThreadMonitor"
    const val CALLBACK_INPUT = 0
    const val CALLBACK_ANIMATION = 1
    const val CALLBACK_TRAVERSAL = 2
    private const val CALLBACK_LAST = CALLBACK_TRAVERSAL
    private const val DO_QUEUE_BEGIN = 1
    private const val DO_QUEUE_END = 2
    var isInit = false
        private set
    private var isAlive = false
    private var isVsyncFrame = false
    private var frameIntervalNano: Long = Constants.DEFAULT_FRAME_DURATION
    private var choreographer: Choreographer? = null
    private var callbackQueueLock: Any? = null
    private var callbackQueue: Array<Any>? = null
    private var addTraversalQueue: Method? = null
    private var addInputQueue: Method? = null
    private var addAnimationQueue: Method? = null
    private var vsyncReceiver: Any? = null
    private var callbackExist: BooleanArray? = null
    private var queueStatus: IntArray? = null
    private var queueCost: LongArray? = null
    private val observers = mutableSetOf<LooperObserver>()
    private var token: Long = 0

    fun init() {
        if (isInit) return

        //监听消息执行的开始和结束
        LooperMonitor.register(object : LooperDispatchListener() {
            override fun isValid(): Boolean {
                return isAlive
            }

            override fun dispatchStart(s: String, beginNs: Long, cpuBeginMs: Long) {
                UiThreadMonitor.dispatchStart(s, beginNs, cpuBeginMs)
            }

            override fun dispatchEnd(
                s: String,
                beginNs: Long,
                cpuBeginMs: Long,
                endNs: Long,
                cpuEndMs: Long
            ) {
                UiThreadMonitor.dispatchEnd(s, beginNs, cpuBeginMs, endNs, cpuEndMs)
            }

        })
        isInit = true
        choreographer = Choreographer.getInstance()
        //一帧时间
        frameIntervalNano = ReflectUtils.reflectObject(
            choreographer,
            "mFrameIntervalNanos",
            Constants.DEFAULT_FRAME_DURATION
        )
        callbackQueueLock = ReflectUtils.reflectObject(choreographer, "mLock", Any())
        callbackQueue = ReflectUtils.reflectObject(choreographer, "mCallbackQueues", null)
        if (callbackQueue != null) {
            addInputQueue = ReflectUtils.reflectMethod(
                callbackQueue!![CALLBACK_INPUT],
                "addCallbackLocked",
                Long::class.java,
                Object::class.java,
                Object::class.java
            )
            addAnimationQueue = ReflectUtils.reflectMethod(
                callbackQueue!![CALLBACK_ANIMATION],
                "addCallbackLocked",
                Long::class.java,
                Object::class.java,
                Object::class.java
            )
            addTraversalQueue = ReflectUtils.reflectMethod(
                callbackQueue!![CALLBACK_TRAVERSAL],  // TODO: 这里有点疑问，在Choreographer中callbackType应该是3，这里是2
                "addCallbackLocked",
                Long::class.java,
                Object::class.java,
                Object::class.java
            )
        }
        vsyncReceiver =
            ReflectUtils.reflectObject(choreographer, "mDisplayEventReceiver", null)
    }

    fun addObserver(observer: LooperObserver) {
        if (!isAlive) {
            onStart()
        }
        observers.add(observer)
    }

    fun removeObserver(observer: LooperObserver) {
        observers.remove(observer)
        if (observers.isEmpty()) {
            onStop()
        }
    }


    @Synchronized
    fun onStart() {
        if (!isInit) return

        if (!isAlive) {
            isAlive = true
            callbackExist = BooleanArray(CALLBACK_LAST + 1)
            queueStatus = IntArray(CALLBACK_LAST + 1)
            queueCost = LongArray(CALLBACK_LAST + 1)
            addFrameCallback(CALLBACK_INPUT, inputCallback, true)
        }
    }

    private fun onStop() {
        if (!isInit) return

        if (isAlive) {
            isAlive = false
        }
    }

    @Synchronized
    private fun addFrameCallback(type: Int, callback: Runnable, isAddHeader: Boolean) {
        if (callbackExist!![type]) return

        if (!isAlive && type == CALLBACK_INPUT) return

        synchronized(callbackQueueLock!!) {
            val method = when (type) {
                CALLBACK_INPUT -> addInputQueue
                CALLBACK_ANIMATION -> addAnimationQueue
                CALLBACK_TRAVERSAL -> addTraversalQueue
                else -> null
            }
            if (method != null) {
                method.invoke(
                    callbackQueue!![type],
                    if (isAddHeader) -1 else SystemClock.uptimeMillis(),
                    callback,
                    null
                )
                callbackExist!![type] = true
            }
        }
    }

    private val inputCallback = Runnable {
        doFrameBegin()

        //计算一帧中 input列表、animation列表、traversal列表 各自执行时间
        doQueueBegin(CALLBACK_INPUT)
        addFrameCallback(CALLBACK_ANIMATION, {
            doQueueEnd(CALLBACK_INPUT)
            doQueueBegin(CALLBACK_ANIMATION)
        }, true)

        addFrameCallback(CALLBACK_TRAVERSAL, {
            doQueueEnd(CALLBACK_ANIMATION)
            doQueueBegin(CALLBACK_TRAVERSAL)
        }, true)
    }

    private fun doFrameBegin() {
        isVsyncFrame = true
    }

    private fun doFrameEnd() {
        doQueueEnd(CALLBACK_TRAVERSAL)
        //重置
        queueStatus!!.fill(0, 0, queueStatus!!.size)

        //计算新的一帧
        addFrameCallback(CALLBACK_INPUT, inputCallback, true)
    }

    private fun doQueueBegin(type: Int) {
        queueStatus!![type] = DO_QUEUE_BEGIN
        queueCost!![type] = System.nanoTime()
    }

    private fun doQueueEnd(type: Int) {
        queueStatus!![type] = DO_QUEUE_END
        queueCost!![type] = System.nanoTime() - queueCost!![type]
        callbackExist!![type] = false
    }

    private fun dispatchStart(s: String, beginNs: Long, cpuBeginMs: Long) {
        token = beginNs
        observers.forEach {
            if (!it.isDispatchBegin) {
                it.dispatchBegin(s, beginNs, cpuBeginMs, token)
            }
        }
    }

    private fun dispatchEnd(
        s: String,
        beginNs: Long,
        cpuBeginMs: Long,
        endNs: Long,
        cpuEndMs: Long
    ) {
        doFrameEnd()
        val startNs = token
        val intendedFrameTimeNs = getIntendedFrameTimeNs(startNs)
        observers.forEach {
            if (it.isDispatchBegin) {
                it.doFrame(
                    ProcessUiLifecycleOwner.visibleScene,
                    startNs,
                    endNs,
                    isVsyncFrame,
                    intendedFrameTimeNs,
                    queueCost!![CALLBACK_INPUT],
                    queueCost!![CALLBACK_ANIMATION],
                    queueCost!![CALLBACK_TRAVERSAL]
                )
            }
        }
        observers.forEach {
            if (it.isDispatchBegin) {
                it.dispatchEnd(
                    s, beginNs, cpuBeginMs, endNs, cpuEndMs, token, isVsyncFrame
                )
            }
        }
        isVsyncFrame = false
    }

    private fun getIntendedFrameTimeNs(startNs: Long): Long {
        return ReflectUtils.reflectObject(vsyncReceiver, "mTimestampNanos", startNs)
    }

    fun getQueueCost(type: Int, token: Long): Long {
        if (token != this.token) {
            return -1
        }
        return if (queueStatus!![type] == DO_QUEUE_END) queueCost!![type] else 0
    }
}