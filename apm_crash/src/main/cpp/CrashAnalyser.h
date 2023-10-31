//
// Created by cnting on 2023/10/30.
//

#ifndef APM_CRASHANALYSER_H
#define APM_CRASHANALYSER_H

#include "unistd.h"
#include "pthread.h"
#include "JNIBridge.h"
#include "Utils.h"

typedef struct native_crash_info_struct {
    int code;
    siginfo_t *si;
    void *sc;
    pid_t pid;
    pid_t tid;
    const char *processName;
    const char *threadName;
    int frame_size;
    uintptr_t frames[BACKTRACE_FRAMES_MAX];
    const char *desc;
} native_crash_info;

extern void initCondition();

void *threadCrashMonitor(void *args);

extern void waitForSignal();

extern void notifyCaughtSignal(int code, siginfo_t *si, void *sc);

extern void analysisNativeException();

extern void copyInfo(int code, siginfo_t *si, void *sc);

#endif //APM_CRASHANALYSER_H
