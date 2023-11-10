//
// Created by cnting on 2023/11/10.
//

#include "detector.h"
#include "mylog.h"

namespace iocanary {
    void FileIODetector::PublishIssue(const Issue &issue) {
        //todo 提交错误
        LOGE("错误类型:%d,开始时间:%lld,读写大小:%ld,buffer大小:%ld,读写耗时:%ld，文件大小:%ld，总耗时:%ld,最大连续读写时间:%ld,读写操作次数:%d\n线程:%s\n%s",
             issue.type_,
             issue.ioinfo_.start_time_us_,
             issue.ioinfo_.op_size_,
             issue.ioinfo_.buffer_size_,
             issue.ioinfo_.rw_cost_us_,
             issue.ioinfo_.file_size_,
             issue.ioinfo_.total_cost_us_,
             issue.ioinfo_.max_continual_rw_cost_time_us,
             issue.ioinfo_.op_cnt_,
             issue.ioinfo_.java_context_.thread_name_.c_str(),
             issue.ioinfo_.java_context_.stack_.c_str()
        );
    }
}