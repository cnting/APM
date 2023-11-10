#include <jni.h>
#include <string>
#include "xhook_ext.h"
#include "mylog.h"
#include "ThreadUtil.h"
#include "core/io_info_collector.h"
#include "core/io_canary.h"

namespace iocanary {
    static JavaVM *kJvm;
    static bool kInitSuc = false;

    static jclass kJavaBridgeClass;
    static jclass kJavaContextClass;
    static jfieldID kStackFid;
    static jfieldID kThreadNameFid;
    static jmethodID kGetJavaContextMid;


    static int (*original_open)(const char *pathname, int flags, mode_t modes);

    static int (*original_open64)(const char *pathname, int flags, mode_t modes);

    static ssize_t (*origin_read)(int fd, void *const buf, size_t count);

    static ssize_t (*origin_read_chk)(int fd, void *buf, size_t count, size_t buf_size);

    static ssize_t (*origin_write)(int fd, const void *buf, size_t count);

    static ssize_t (*origin_write_chk)(int fd, const void *buf, size_t count, size_t buf_size);

    static int (*original_android_fdsan_close_with_tag)(int fd, uint64_t expected_tag);

    static int (*origin_close)(int fd);

    const static char *TARGET_MODULES[] = {
            "libopenjdkjvm.so",
            "libjavacore.so",
            "libopenjdk.so"
    };
    const static size_t TARGET_MODULE_COUNT = sizeof(TARGET_MODULES) / sizeof(char *);

    extern "C" {

    char *jstringToChars(JNIEnv *env, jstring jstr) {
        if (jstr == nullptr) {
            return nullptr;
        }
        jboolean isCopy = JNI_FALSE;
        const char *str = env->GetStringUTFChars(jstr, &isCopy);
        //复制一份，因为下面要release
        char *ret = strdup(str);
        env->ReleaseStringUTFChars(jstr, str);
        return ret;
    }

    bool InitJniEnv(JavaVM *pVm) {
        kJvm = pVm;
        JNIEnv *env = NULL;
        if (kJvm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6)) {
            return false;
        }
        jclass temp_cls = env->FindClass("com/cnting/apm_io/core/IOCanaryJniBridge");
        kJavaBridgeClass = static_cast<jclass>(env->NewGlobalRef(temp_cls));

        jclass temp_java_context_cls = env->FindClass(
                "com/cnting/apm_io/core/IOCanaryJniBridge$JavaContext");
        kJavaContextClass = static_cast<jclass>(env->NewGlobalRef(temp_java_context_cls));
        kStackFid = env->GetFieldID(kJavaContextClass, "stack", "Ljava/lang/String;");
        kThreadNameFid = env->GetFieldID(kJavaContextClass, "threadName", "Ljava/lang/String;");
        kGetJavaContextMid = env->GetStaticMethodID(kJavaBridgeClass, "getJavaContext",
                                                    "()Lcom/cnting/apm_io/core/IOCanaryJniBridge$JavaContext;");

        return true;
    }

    JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
        kInitSuc = false;
        if (!InitJniEnv(vm)) {
            return -1;
        }
        kInitSuc = true;
        return JNI_VERSION_1_6;
    }

    JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
        JNIEnv *env;
        kJvm->GetEnv((void **) &env, JNI_VERSION_1_6);
        if (env != NULL) {
            if (kJavaBridgeClass) {
                env->DeleteGlobalRef(kJavaBridgeClass);
            }
            if (kJavaContextClass) {
                env->DeleteGlobalRef(kJavaContextClass);
            }
        }
    }

    void DoProxyOpenLogic(const char *pathname, int flags, mode_t mode, int ret) {
        JNIEnv *env = NULL;
        kJvm->GetEnv((void **) &env, JNI_VERSION_1_6);
        if (env == NULL || !kInitSuc) {
            LOGE("proxy open failed");
            return;
        }
        jobject java_context_obj = env->CallStaticObjectMethod(kJavaBridgeClass,
                                                               kGetJavaContextMid);
        if (java_context_obj == NULL) {
            return;
        }
        jstring j_stack = static_cast<jstring>(env->GetObjectField(java_context_obj, kStackFid));
        jstring j_thread_name = static_cast<jstring>(env->GetObjectField(java_context_obj,
                                                                         kThreadNameFid));
        char *stack = jstringToChars(env, j_stack);
        char *thread_name = jstringToChars(env, j_thread_name);
        JavaContext java_context(apm::getCurrentThreadId(), thread_name == NULL ? "" : thread_name,
                                 stack == NULL ? "" : stack);
        //记得释放
        free(stack);
        free(thread_name);

        iocanary::IOCanary::Get().OnOpen(pathname, flags, mode, ret, java_context);

        env->DeleteLocalRef(java_context_obj);
        env->DeleteLocalRef(j_stack);
        env->DeleteLocalRef(j_thread_name);
    }

    /**
     * 函数定义看 libcore_io_Linux.cpp
     * https://cs.android.com/android/platform/superproject/main/+/main:libcore/luni/src/main/native/libcore_io_Linux.cpp;bpv=1;bpt=1?q=libcore_io_Linux.cpp&ss=android%2Fplatform%2Fsuperproject%2Fmain
     */
    int ProxyOpen(const char *pathname, int flags, mode_t modes) {
        if (!apm::isMainThread()) {
            return original_open(pathname, flags, modes);
        }
        int ret = original_open(pathname, flags, modes);
        if (ret != -1) {
            DoProxyOpenLogic(pathname, flags, modes, ret);
        }
        return ret;
    }
    int ProxyOpen64(const char *pathname, int flags, mode_t modes) {
        if (!apm::isMainThread()) {
            return original_open64(pathname, flags, modes);
        }
        int ret = original_open64(pathname, flags, modes);
        if (ret != -1) {
            DoProxyOpenLogic(pathname, flags, modes, ret);
        }
        return ret;
    }

    ssize_t ProxyRead(int fd, void *const buf, size_t count) {
        if (!apm::isMainThread()) {
            return origin_read(fd, buf, count);
        }
        int64_t start = apm::GetSysTimeMicros();
        ssize_t ret = origin_read(fd, buf, count);
        long read_cost_us = apm::GetSysTimeMicros() - start;
        iocanary::IOCanary::Get().OnRead(fd, buf, count, ret, read_cost_us);
        return ret;
    }

    ssize_t ProxyReadChk(int fd, void *buf, size_t count, size_t buf_size) {
        if (!apm::isMainThread()) {
            return origin_read_chk(fd, buf, count, buf_size);
        }
        int64_t start = apm::GetSysTimeMicros();
        ssize_t ret = origin_read_chk(fd, buf, count, buf_size);
        long read_cost_us = apm::GetSysTimeMicros() - start;

        iocanary::IOCanary::Get().OnRead(fd, buf, count, ret, read_cost_us);

        return ret;
    }

    ssize_t ProxyWrite(int fd, const void *buf, size_t count) {
        if (!apm::isMainThread()) {
            return origin_write(fd, buf, count);
        }
        int64_t start = apm::GetSysTimeMicros();

        size_t ret = origin_write(fd, buf, count);

        long write_cost_us = apm::GetSysTimeMicros() - start;

        iocanary::IOCanary::Get().OnWrite(fd, buf, count, ret, write_cost_us);


        return ret;
    }

    ssize_t ProxyWriteChk(int fd, const void *buf, size_t count, size_t buf_size) {
        if (!apm::isMainThread()) {
            return origin_write_chk(fd, buf, count, buf_size);
        }
        int64_t start = apm::GetSysTimeMicros();
        size_t ret = origin_write_chk(fd, buf, count, buf_size);
        long write_cost_us = apm::GetSysTimeMicros() - start;

        iocanary::IOCanary::Get().OnWrite(fd, buf, count, ret, write_cost_us);

        return ret;
    }

    int ProxyClose(int fd) {
        if (!apm::isMainThread()) {
            return origin_close(fd);
        }
        int ret = origin_close(fd);

        iocanary::IOCanary::Get().OnClose(fd, ret);
        return ret;
    }

    int Proxy_android_fdsan_close_with_tag(int fd, uint64_t expected_tag) {
        if (!apm::isMainThread()) {
            return original_android_fdsan_close_with_tag(fd, expected_tag);
        }
        int ret = original_android_fdsan_close_with_tag(fd, expected_tag);
        iocanary::IOCanary::Get().OnClose(fd, ret);
        return ret;
    }

    JNIEXPORT
    jint JNICALL
    Java_com_cnting_apm_1io_core_IOCanaryJniBridge_doHook(JNIEnv *env, jobject thiz) {
        for (int i = 0; i < TARGET_MODULE_COUNT; i++) {
            const char *so_name = TARGET_MODULES[i];
            void *soinfo = xhook_elf_open(so_name);
            if (!soinfo) {
                LOGE("Failure to open %s, try next.", so_name);
                continue;
            }

            xhook_got_hook_symbol(soinfo, "open", (void *) ProxyOpen, (void **) &original_open);
            xhook_got_hook_symbol(soinfo, "open64", (void *) ProxyOpen64,
                                  (void **) &original_open64);

            bool is_libjavacore = (strstr(so_name, "libjavacore.so") != nullptr);
            if (is_libjavacore) {
                if (xhook_got_hook_symbol(soinfo, "read", (void *) ProxyRead,
                                          (void **) &origin_read) != 0) {
                    LOGE("doHook hook read failed, try __read_chk");
                    if (xhook_got_hook_symbol(soinfo, "__read_chk", (void *) ProxyReadChk,
                                              (void **) &origin_read_chk) != 0) {
                        LOGE("doHook hook failed: __read_chk");
                        xhook_elf_close(soinfo);
                        return JNI_FALSE;
                    }
                }
                if (xhook_got_hook_symbol(soinfo, "write", (void *) ProxyWrite,
                                          (void **) &origin_write) != 0) {
                    LOGE("doHook hook write failed, try __write_chk");
                    if (xhook_got_hook_symbol(soinfo, "__write_chk", (void *) ProxyWriteChk,
                                              (void **) &origin_write_chk) != 0) {
                        LOGE("doHook hook failed: __write_chk");
                        xhook_elf_close(soinfo);
                        return JNI_FALSE;
                    }
                }
            }
            xhook_got_hook_symbol(soinfo, "close", (void *) ProxyClose, (void **) &origin_close);
            xhook_got_hook_symbol(soinfo, "android_fdsan_close_with_tag",
                                  (void *) Proxy_android_fdsan_close_with_tag,
                                  (void **) &original_android_fdsan_close_with_tag);
            xhook_elf_close(soinfo);
        }
        return JNI_TRUE;
    }

    JNIEXPORT jint JNICALL
    Java_com_cnting_apm_1io_core_IOCanaryJniBridge_doUnHook(JNIEnv *env, jobject thiz) {
        return JNI_TRUE;
    }


    }


}


