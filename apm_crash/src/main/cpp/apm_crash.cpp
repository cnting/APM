#include <jni.h>
#include "SignalHandler.h"
#include "CrashDefine.h"
#include "JNIBridge.h"
#include <pthread.h>
#include "CrashAnalyser.h"

extern "C"
JNIEXPORT void JNICALL
Java_com_cnting_apm_1crash_crash_NativeCrashMonitor_nativeInit(JNIEnv *env,
                                                               jobject nativeCrashMonitor,
                                                               jobject callback) {
    callback = env->NewGlobalRef(callback);
    JavaVM *javaVm;
    env->GetJavaVM(&javaVm);

    jclass nativeCrashMonitorClass = env->GetObjectClass(nativeCrashMonitor);
    nativeCrashMonitorClass = static_cast<jclass>(env->NewGlobalRef(nativeCrashMonitorClass));

    JNIBridge *jniBridge = new JNIBridge(javaVm, callback, nativeCrashMonitorClass);
    //创建一个线程去监听是否有异常
    initCondition();
    pthread_t pthread;
    int ret = pthread_create(&pthread, nullptr, threadCrashMonitor, jniBridge);
    if (ret) {
        LOGE("pthread_create error,return %d", ret);
    }

}
extern "C"
JNIEXPORT void JNICALL
Java_com_cnting_apm_1crash_crash_NativeCrashMonitor_nativeSetup(JNIEnv *env, jobject thiz) {
    //设置监听信号量回调处理
    installSignalHandlers();

    //设置额外的栈空间，让信号处理在单独的栈中处理
    installAlternateStack();
}