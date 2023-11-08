//
// Created by cnting on 2023/11/8.
//

#ifndef APM_THREADUTIL_H
#define APM_THREADUTIL_H

#include <stdlib.h>

extern const char *getProcessName(pid_t pid);

extern const char *getThreadName(pid_t tid);

extern const char *getThreadRunInfo(pid_t pid, pid_t tid);


#endif //APM_THREADUTIL_H
