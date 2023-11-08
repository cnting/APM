package com.cnting.apm_thread.bean

/**
 * Created by cnting on 2023/11/8
 *
 */
data class DeadLockThread(val curThreadId: Long, val blockThreadId: Long, val thread: Thread)
