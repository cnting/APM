//
// Created by cnting on 2023/11/10.
//

#include "main_thread_detector.h"
#include "ThreadUtil.h"
#include "../core/io_canary_env.h"
#include "mylog.h"

namespace iocanary {
    void FileIOMainThreadDetector::Detect(const iocanary::IOInfo &file_io_info) {
        if (apm::getMainThreadId() == file_io_info.java_context_.thread_id_) {
            int type = 0;
            if (file_io_info.max_once_rw_cost_time_us > kPossibleNegativeThreshold) {
                type = 1;
            }
            if (file_io_info.max_continual_rw_cost_time_us > kDefaultMainThreadTriggerThreshold) {
                type |= 2;
            }
            if (file_io_info.rw_cost_us_ > kDefaultMainThreadCostThreshold) {
                type |= 3;
            }
            if (type != 0) {
                Issue issue(kType, file_io_info);
                PublishIssue(issue);
            }
        }
    }
}
