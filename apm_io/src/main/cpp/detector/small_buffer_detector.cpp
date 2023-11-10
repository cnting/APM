//
// Created by cnting on 2023/11/10.
//

#include "small_buffer_detector.h"
#include "../core/io_canary_env.h"

namespace iocanary {
    void FileIOSmallBufferDetector::Detect(const iocanary::IOInfo &file_io_info) {
        if (file_io_info.op_cnt_ > kSmallBufferOpTimesThreshold &&
            (file_io_info.op_size_ / file_io_info.op_cnt_) < kDefaultBufferSmallThreshold &&
            file_io_info.max_continual_rw_cost_time_us > kPossibleNegativeThreshold
                ) {
            PublishIssue(Issue(kType, file_io_info));
        }
    }
}