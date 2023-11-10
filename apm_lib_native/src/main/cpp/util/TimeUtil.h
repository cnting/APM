//
// Created by cnting on 2023/11/10.
//

#ifndef APM_TIMEUTIL_H
#define APM_TIMEUTIL_H

#include <cstdint>

namespace apm {
    /**
     * 获取系统当前时间，单位微秒(us)
     */
    extern int64_t GetSysTimeMicros();
}


#endif //APM_TIMEUTIL_H
