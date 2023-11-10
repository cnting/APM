//
// Created by cnting on 2023/11/9.
//

#include "io_info_collector.h"
#include "FileUtil.h"
#include "mylog.h"

namespace iocanary {
    void IOInfoCollector::OnOpen(const char *pathname, int flags, mode_t mode, int open_ret,
                                 const iocanary::JavaContext &java_context) {
        if (open_ret == -1) {
            return;
        }
        //open_ret是fd，判断是否已经在map中
        if (info_map_.find(open_ret) != info_map_.end()) {
            return;
        }
        std::shared_ptr<IOInfo> info = std::make_shared<IOInfo>(pathname, java_context);
        info_map_.insert(std::make_pair(open_ret, info));
    }

    std::shared_ptr<IOInfo> IOInfoCollector::OnClose(int fd, int close_ret) {
        if (info_map_.find(fd) == info_map_.end()) {
            return nullptr;
        }
        info_map_[fd]->total_cost_us_ = apm::GetSysTimeMicros() - info_map_[fd]->start_time_us_;
        info_map_[fd]->file_size_ = apm::GetFileSize(info_map_[fd]->path_.c_str());
        std::shared_ptr<IOInfo> info = info_map_[fd];
        info_map_.erase(fd);
        return info;
    }

    void IOInfoCollector::OnWrite(int fd, const void *buf, size_t size, ssize_t write_ret,
                                  long write_cost) {
        if (write_ret == -1 || write_cost < 0) {
            return;
        }
        if (info_map_.find(fd) == info_map_.end()) {
            return;
        }
        CountRWInfo(fd, FileOpType::kWrite, size, write_cost);
    }

    void IOInfoCollector::OnRead(int fd, const void *buf, size_t size, ssize_t read_ret,
                                 long read_cost) {
        if (read_ret == -1 || read_cost < 0) {
            return;
        }
        if (info_map_.find(fd) == info_map_.end()) {
            return;
        }
        CountRWInfo(fd, FileOpType::kRead, size, read_cost);
    }

    void
    IOInfoCollector::CountRWInfo(int fd, const iocanary::FileOpType &file_op_type, long op_size,
                                 long rw_cost) {
        if (info_map_.find(fd) == info_map_.end()) {
            return;
        }
        const int64_t now = apm::GetSysTimeMicros();
        info_map_[fd]->op_cnt_++;
        info_map_[fd]->op_size_ += op_size;
        info_map_[fd]->rw_cost_us_ += rw_cost;
        if (rw_cost > info_map_[fd]->max_once_rw_cost_time_us) {
            info_map_[fd]->max_once_rw_cost_time_us = rw_cost;
        }
        //8000us = 8ms 内都算连续
        if (info_map_[fd]->last_rw_time_us > 0 &&
            (now - info_map_[fd]->last_rw_time_us) < kContinualThreshold) {
            info_map_[fd]->current_continual_rw_time_us += rw_cost;
        } else {
            info_map_[fd]->current_continual_rw_time_us = rw_cost;
        }
        if (info_map_[fd]->current_continual_rw_time_us >
            info_map_[fd]->max_continual_rw_cost_time_us) {
            info_map_[fd]->max_continual_rw_cost_time_us = info_map_[fd]->current_continual_rw_time_us;
        }
        info_map_[fd]->last_rw_time_us = now;

        if (info_map_[fd]->buffer_size_ < op_size) {
            info_map_[fd]->buffer_size_ = op_size;
        }
        if (info_map_[fd]->op_type_ == FileOpType::kInit) {
            info_map_[fd]->op_type_ = file_op_type;
        }

    }
}
