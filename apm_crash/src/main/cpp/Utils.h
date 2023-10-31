//
// Created by cnting on 2023/10/30.
//

#ifndef APM_UTILS_H
#define APM_UTILS_H

#include <signal.h>
#include <stdlib.h>
#include "CrashDefine.h"

extern const char *desc_sig(int sig, int code);

extern const char *getProcessName(pid_t pid);

extern const char *getThreadName(pid_t tid);

bool is_dll(const char *name);

#endif //APM_UTILS_H
