package com.cnting.apm_trace_canary.constant

/**
 * Created by cnting on 2023/7/24
 *
 */
object Constants {
    const val DEFAULT_FRAME_DURATION = 16666667L
    const val TIME_MILLIS_TO_NANO = 1000000
    const val DEFAULT_ANR = 5 * 1000
    const val DEFAULT_NORMAL_LAG = 2 * 1000
    const val DEFAULT_IDLE_HANDLER_LAG: Long = 2 * 1000
    const val DEFAULT_TOUCH_EVENT_LAG: Long = 2 * 1000

    enum class Type {
        NORMAL, ANR, STARTUP, LAG, SIGNAL_ANR, SIGNAL_ANR_NATIVE_BACKTRACE, LAG_IDLE_HANDLER, LAG_TOUCH, PRIORITY_MODIFIED, TIMERSLACK_MODIFIED
    }
}

object HistoryConstants {
    /**
     * 超过这个时间输出警告 超过这个时间消息单独罗列出来
     */
    const val WARN_TIME = 300

    /**
     * 两条消息时间间隔超过这个值，生成一条消息
     */
    const val GAP_TIME = 50

    /**
     * 超过这个时间可直接判定为anr
     */
    const val ANR_TIME: Long = 3000
}