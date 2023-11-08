#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_cnting_apm_1lib_1native_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}