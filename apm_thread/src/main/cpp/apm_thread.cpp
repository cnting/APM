#include <jni.h>
#include <string>
#include <android/log.h>
#include "dlopen.h"
#include "inlineHook.h"

void *get_contend_monitor;
void *get_lock_owner_thread_id;
void *thread_create_callback;

const char *get_lock_owner_symbol_name(int level) {
    if (level < 29) {
        return "_ZN3art7Monitor20GetLockOwnerThreadIdEPNS_6mirror6ObjectE";
    } else {
        return "_ZN3art7Monitor20GetLockOwnerThreadIdENS_6ObjPtrINS_6mirror6ObjectEEE";
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cnting_apm_1thread_NativeThreadMonitor_nativeInit(JNIEnv *env, jobject thiz,
                                                           jint sdk_version) {
    ndk_init(env);
    void *so_addr = ndk_dlopen("libart.so", RTLD_LAZY);
    if (so_addr == NULL) {
        return -1;
    }
    //GetContendedMonitor：获取当前线程在竞争的锁
    //这里可以查函数地址 http://androidxref.com/9.0.0_r3/xref/system/core/libbacktrace/testdata/arm/libart.so
    get_contend_monitor = ndk_dlsym(so_addr, "_ZN3art7Monitor19GetContendedMonitorEPNS_6ThreadE");
    if (get_contend_monitor == NULL) {
        return -2;
    }

    // GetLockOwnerThreadId：当前锁被哪个线程 id 持有了
    get_lock_owner_thread_id = ndk_dlsym(so_addr, get_lock_owner_symbol_name(sdk_version));
    if (get_lock_owner_thread_id == NULL) {
        return -3;
    }

    thread_create_callback = ndk_dlsym(so_addr, "_ZN3art6Thread14CreateCallbackEPv");
    if (thread_create_callback == NULL) {
        return -4;
    }
    return 0;
}


/**
 * 通过线程获取锁信息
 * ObjPtr<mirror::Object> Monitor::GetContendedMonitor(Thread* thread)
 *
 * 通过锁信息获取占有它的线程
 * uint32_t Monitor::GetLockOwnerThreadId(ObjPtr<mirror::Object> obj)
 */
extern "C"
JNIEXPORT jlong JNICALL
Java_com_cnting_apm_1thread_NativeThreadMonitor_getContendThreadId(JNIEnv *env, jobject thiz,
                                                                   jlong thread_native_address) {
    if (get_contend_monitor != nullptr && get_lock_owner_thread_id != nullptr) {
        //get_contend_monitor指针 转成 函数指针(int (*)(long)
        int monitorObj = ((int (*)(long)) get_contend_monitor)(thread_native_address);
        if (monitorObj != 0) {
            int monitorThreadId = ((int (*)(int)) get_lock_owner_thread_id)(monitorObj);
            return monitorThreadId;
        }
    }
    return 0;
}



extern "C"
JNIEXPORT jlong JNICALL
Java_com_cnting_apm_1thread_NativeThreadMonitor_getCurrentThreadId(JNIEnv *env, jobject thiz,
                                                                   jlong thread_native_address) {
    //要大于Android 5.0
    if (thread_native_address != 0) {
        int *pInt = reinterpret_cast<int *>(thread_native_address);
        pInt = pInt + 3;
        return *pInt;
    }
    return 0;
}

/**
 * https://cs.android.com/android/platform/superproject/main/+/main:art/runtime/thread.cc;drc=f3220ad34bc32447f8b4a26fc32f661aa4e96256;l=618?q=Thread.cc&ss=android%2Fplatform%2Fsuperproject%2Fmain
 * 思路：线程创建时会调用Thread.cc 中的createNativeThread方法，通过pthread_create创建线程，并执行CreateCallback
 * hook CreateCallback，在它前后做事
 */

//函数指针
void *(*old_create_callback)(void *) = NULL;

void *new_create_callback(void *arg) {
    long startTime = time(NULL);
    void *result = old_create_callback(arg);
    long aliveTime = time(NULL) - startTime;
    __android_log_print(ANDROID_LOG_ERROR, "TAG", "线程执行完毕，存活时间 -> %lds", aliveTime);
    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cnting_apm_1thread_NativeThreadMonitor_monitorThread(JNIEnv *env, jobject thiz) {
    //第一个参数是 Thread::CreateCallback的函数地址
    //第二个参数是 hook 函数的新地址
    //第三个参数其实也是Thread::CreateCallback的地址
    if (registerInlineHook((uint32_t) thread_create_callback, (uint32_t) new_create_callback,
                           (uint32_t **) &old_create_callback) != ELE7EN_OK) {
        return -1;
    }
    if (inlineHook((uint32_t) thread_create_callback) != ELE7EN_OK) {
        return -2;
    }
    return 0;
}