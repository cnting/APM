//
// Created by cnting on 2023/10/30.
//

#include <cstdlib>
#include <unwind.h>
#include <dlfcn.h>
#include "CrashAnalyser.h"

//锁的条件变量
pthread_cond_t signalCond;
pthread_mutex_t signalLock;
pthread_cond_t exceptionCond;
pthread_mutex_t exceptionLock;

//记录信息
native_crash_info *nativeCrashInfo;

void initCondition() {
    nativeCrashInfo = static_cast<native_crash_info *>(malloc(sizeof(native_crash_info)));
    pthread_mutex_init(&signalLock, NULL);
    pthread_mutex_init(&exceptionLock, NULL);
    pthread_cond_init(&signalCond, NULL);
    pthread_cond_init(&exceptionCond, NULL);
}

/**
 * 在子线程监听异常信号
 */
void *threadCrashMonitor(void *args) {
    JNIBridge *jniBridge = static_cast<JNIBridge *>(args);
    while (true) {
        //等待被唤醒
        waitForSignal();
        //解析异常信息堆栈
        analysisNativeException();
        //抛给Java层
        jniBridge->throwExceptionToJava(nativeCrashInfo);
    }
    int status = 1;
    return &status;
}

/**
 * 子线程调用
 * 等待异常信息
 */
void waitForSignal() {
    pthread_mutex_lock(&signalLock);
    pthread_cond_wait(&signalCond, &signalLock);
    pthread_mutex_unlock(&signalLock);
}

/**
 * 主线程调用
 * 异常信号来了，唤醒等待
 */
void notifyCaughtSignal(int code, siginfo_t *si, void *sc) {
    copyInfo(code, si, sc);
    pthread_mutex_lock(&signalLock);
    pthread_cond_signal(&signalCond);
    pthread_mutex_unlock(&signalLock);
}

_Unwind_Reason_Code unwind_callback(struct _Unwind_Context *context, void *arg) {
    native_crash_info *const crashInfo = static_cast<native_crash_info *const>(arg);
    //pc是程序加载到内存中的绝对地址
    const uintptr_t pc = _Unwind_GetIP(context);
    //0x0表示空指针
    if (pc != 0x0) {
        //把pc值保存到native_crash_info
        crashInfo->frames[crashInfo->frame_size++] = pc;
    }
    //限制收集的栈大小
    if (crashInfo->frame_size == BACKTRACE_FRAMES_MAX) {
        return _URC_END_OF_STACK;
    } else {
        return _URC_NO_REASON;
    }
}

void copyInfo(int code, siginfo_t *si, void *sc) {
    nativeCrashInfo->code = code;
    nativeCrashInfo->si = si;
    nativeCrashInfo->sc = sc;
    nativeCrashInfo->pid = getpid();
    nativeCrashInfo->tid = gettid();
    nativeCrashInfo->processName = getProcessName(nativeCrashInfo->pid);
    if (nativeCrashInfo->pid == nativeCrashInfo->tid) {
        nativeCrashInfo->threadName = "main";
    } else {
        nativeCrashInfo->threadName = getThreadName(nativeCrashInfo->tid);
    }
    nativeCrashInfo->frame_size = 0;
    nativeCrashInfo->desc = desc_sig(nativeCrashInfo->si->si_signo, nativeCrashInfo->si->si_code);
    _Unwind_Backtrace(unwind_callback, nativeCrashInfo);
}

void analysisNativeException() {

}