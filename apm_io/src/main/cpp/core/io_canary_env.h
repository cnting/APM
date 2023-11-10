//
// Created by cnting on 2023/11/10.
//

#ifndef APM_IO_CANARY_ENV_H
#define APM_IO_CANARY_ENV_H
namespace iocanary {
    constexpr static const int kPossibleNegativeThreshold = 13 * 1000;
    constexpr static const int kSmallBufferOpTimesThreshold = 20;
    //in μs
    constexpr static const int kDefaultMainThreadTriggerThreshold = 500 * 1000;
    constexpr static const int kDefaultMainThreadCostThreshold = 2 * 1000 * 1000;
    //We take 4096B(4KB) as a small size of the buffer
    constexpr static const int kDefaultBufferSmallThreshold = 4096;
    constexpr static const int kDefaultRepeatReadThreshold = 5;
}
#endif //APM_IO_CANARY_ENV_H
