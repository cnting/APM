#include <jni.h>
#include "SignalHandler.h"
#include "CrashDefine.h"

extern "C"
JNIEXPORT void JNICALL
Java_com_cnting_apm_1crash_crash_NativeCrashMonitor_nativeInit(JNIEnv *env, jobject thiz,
                                                               jobject callback) {
}
extern "C"
JNIEXPORT void JNICALL
Java_com_cnting_apm_1crash_crash_NativeCrashMonitor_nativeSetup(JNIEnv *env, jobject thiz) {
    //设置监听信号量回调处理
    installSignalHandlers();

    //设置额外的栈空间，让信号处理在单独的栈中处理
    installAlternateStack();
}