#include <jni.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_example_apm_ui_crash_CrashActivity2_nativeCrash(JNIEnv *env, jobject thiz) {
    int *num = NULL;
    *num = 100;
}