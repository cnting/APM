//
// Created by cnting on 2023/11/10.
//

#include <ctime>
#include "TimeUtil.h"

namespace apm {
    int64_t GetSysTimeMicros() {
        timeval tv;
        gettimeofday(&tv, 0);
        return (int64_t) tv.tv_sec * 1000000 + (int64_t) tv.tv_usec;
    }
}