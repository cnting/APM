//
// Created by cnting on 2023/10/29.
//

#ifndef APM_CRASHDEFINE_H
#define APM_CRASHDEFINE_H

#include "signal.h"
#include "android/log.h"

#define TAG "JNI_TAG"
# define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
# define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

//异常信号量
const int exceptionSignals[] = {
        SIGSEGV, //段错误，访问无效内存或无权访问内存
        SIGABRT, //主动abort()后触发
        SIGFPE,  //算数异常，包括溢出、除以0
        SIGILL,  //非法指令
        SIGBUS,  //硬件或对齐错误
        SIGTRAP  //由断点指令或其他陷阱指令产生，由debugger使用
};
const int exceptionSignalsNumber = sizeof(exceptionSignals) / sizeof(exceptionSignals[0]);

//NSIG：信号最大数值
static struct sigaction oldHandlers[NSIG];
#endif //APM_CRASHDEFINE_H
