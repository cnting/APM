//
// Created by cnting on 2023/10/30.
//
#include "JNIBridge.h"
#include "stringprintf.h"
#include "mylog.h"


JNIBridge::JNIBridge(JavaVM *javaVm, jobject callbackObj, jclass nativeCrashMonitorClass) {
    this->javaVm = javaVm;
    this->callbackObj = callbackObj;
    this->nativeCrashMonitorClass = nativeCrashMonitorClass;
}


void JNIBridge::throwExceptionToJava(struct native_crash_info_struct *nativeCrashInfo) {
    std::string result;
    result += "-----------------\n";
    result += nativeCrashInfo->desc;
    result += "\n";
    result += android::base::StringPrintf("pid:%d，processName:%s，tid:%d，threadName:%s\n",
                                          nativeCrashInfo->pid, nativeCrashInfo->processName,
                                          nativeCrashInfo->tid, nativeCrashInfo->threadName);

    //获取java层的堆栈信息
    JNIEnv *env = NULL;
    if (this->javaVm->AttachCurrentThread(&env, NULL) != JNI_OK) {
        LOGE("AttachCurrentThread failed!");
    }
    jmethodID getStackInfoByThreadNameMid = env->GetStaticMethodID(this->nativeCrashMonitorClass,
                                                                   "getStackInfoByThreadName",
                                                                   "(Ljava/lang/String;)Ljava/lang/String;");
    jstring jThreadName = env->NewStringUTF(nativeCrashInfo->threadName);
    jobject javaStackInfo = env->CallStaticObjectMethod(this->nativeCrashMonitorClass,
                                                        getStackInfoByThreadNameMid, jThreadName);
    const char *javaExceptionStackInfo = env->GetStringUTFChars(static_cast<jstring>(javaStackInfo),
                                                                JNI_FALSE);

    result += javaExceptionStackInfo;
    result += "\n";

    //获取native堆栈
    // pc值是程序加载到内存中的绝对地址，绝对地址不能直接使用，
    // 因为每次程序运行创建的内存肯定都不是固定区域的内存，所以绝对地址肯定每次运行都不一致。
    // 我们需要拿到崩溃代码相对于共享库的相对偏移地址，才能使用addr2line分析出是哪一行代码。
    int frame_size = nativeCrashInfo->frame_size;
    for (int index = 0; index < frame_size; index++) {
        uintptr_t pc = nativeCrashInfo->frames[index];
        Dl_info info;
        void *const addr = reinterpret_cast<void *const>(pc);
        if (dladdr(addr, &info) != 0 && info.dli_fname != NULL) {
            //dli_fbase：so库在该进程中的起始地址
            //dli_fname：so库在系统库的绝对路径名
            //dli_saddr：获取到pc值对应最接近的函数地址，如果无法获取到则为null
            //dli_sname：获取到pc值对应最接近的函数名，如果无法获取到则为null

            //得到跟pc值最接近的函数地址
            const uintptr_t near = reinterpret_cast<const uintptr_t>(info.dli_saddr);
            //得到这个函数地址的偏移
            const uintptr_t offs = pc - near;
            //得到so库的偏移值
            const uintptr_t addr_rel = pc - (uintptr_t) info.dli_fbase;
            //如果是so库，用偏移值，否则用pc值
            const uintptr_t addr_to_use = is_dll(info.dli_fname) ? addr_rel : pc;

            result += android::base::StringPrintf("native crash #%02x pc 0x%016x %s (%s+0x%x)",
                                                  index,
                                                  addr_to_use,
                                                  info.dli_fname, info.dli_sname, offs);
            result += "\n";
        }
    }

    jclass crashClass = env->GetObjectClass(this->callbackObj);
    jmethodID crashMethod = env->GetMethodID(crashClass, "onCrash",
                                             "(Ljava/lang/String;Ljava/lang/Error;)V");
    jclass jErrorClass = env->FindClass("java/lang/Error");
    jmethodID jErrorInitMethod = env->GetMethodID(jErrorClass, "<init>", "(Ljava/lang/String;)V");
    jstring errorMessage = env->NewStringUTF(result.c_str());
    //错误信息给Error
    jobject errorObject = env->NewObject(jErrorClass, jErrorInitMethod, errorMessage);
    env->CallVoidMethod(this->callbackObj, crashMethod, jThreadName, errorObject);


    if (this->javaVm->DetachCurrentThread() != JNI_OK) {
        LOGE("AttachCurrentThread failed!");
    }
}



