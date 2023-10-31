//
// Created by cnting on 2023/10/30.
//

#ifndef APM_JNIBRIDGE_H
#define APM_JNIBRIDGE_H

#include "jni.h"
#include "CrashDefine.h"
#include "CrashAnalyser.h"
#include "stringprintf.h"
#include <string>
#include <dlfcn.h>


class JNIBridge {
private:
    JavaVM *javaVm;
    jobject callbackObj;
    jclass nativeCrashMonitorClass;
public:
    JNIBridge(JavaVM *javaVm, jobject callbackObj, jclass nativeCrashMonitorClass);

    void throwExceptionToJava(struct native_crash_info_struct *nativeCrashInfo);

private:
};


#endif //APM_JNIBRIDGE_H
