//
// Created by cnting on 2023/10/30.
//

#ifndef APM_UTILS_H
#define APM_UTILS_H

#include <signal.h>
#include <stdlib.h>
#include "CrashDefine.h"

extern const char *desc_sig(int sig, int code);


bool is_dll(const char *name);

#endif //APM_UTILS_H
