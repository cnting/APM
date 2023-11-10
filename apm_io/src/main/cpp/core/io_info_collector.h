//
// Created by cnting on 2023/11/9.
//

#ifndef APM_IO_INFO_COLLECTOR_H
#define APM_IO_INFO_COLLECTOR_H

#include <unistd.h>
#include <string>
#include "unordered_map"
#include "TimeUtil.h"

namespace iocanary {
    typedef enum {
        kInit = 0,
        kRead,
        kWrite
    } FileOpType;

    class JavaContext {

    public:
        JavaContext(intmax_t thread_id, const std::string &thread_name, const std::string &stack) :
                thread_id_(thread_id),
                thread_name_(thread_name),
                stack_(stack) {}

        const intmax_t thread_id_;
        const std::string thread_name_;
        const std::string stack_;

    };

    class IOInfo {
    public:
        IOInfo(const std::string path, const JavaContext javaContext) :
                path_(path),
                java_context_(javaContext),
                start_time_us_(apm::GetSysTimeMicros()),
                op_type_(kInit) {}

        const std::string path_;
        const JavaContext java_context_;

        FileOpType op_type_ = kInit;
        //开始时间
        int64_t start_time_us_;
        //读写大小
        long op_size_;
        //设置的buffer大小
        long buffer_size_ = 0;
        //读写耗时
        long rw_cost_us_ = 0;
        //文件大小
        long file_size_ = 0;
        //总耗时
        long total_cost_us_ = 0;
        //最大连续读写时间（8ms内都算连续）
        long max_continual_rw_cost_time_us = 0;
        //本次连续读写时间
        long current_continual_rw_time_us = 0;
        //最大一次读写时间
        long max_once_rw_cost_time_us = 0;
        //上次读写时间
        long last_rw_time_us = 0;
        //读写操作次数
        int op_cnt_ = 0;
    };

    class IOInfoCollector {
    public:
        void OnOpen(const char *pathname, int flags, mode_t mode, int open_ret,
                    const JavaContext &java_context);

        void OnRead(int fd, const void *buf, size_t size, ssize_t read_ret, long read_cost);

        void OnWrite(int fd, const void *buf, size_t size, ssize_t write_ret, long write_cost);

        std::shared_ptr<IOInfo> OnClose(int fd, int close_ret);

    private:
        std::unordered_map<int, std::shared_ptr<IOInfo>> info_map_;

        void CountRWInfo(int fd, const FileOpType &file_op_type, long op_size, long rw_cost);

        constexpr static const int kContinualThreshold = 8*1000;//in μs， half of 16.6667
    };
}


#endif //APM_IO_INFO_COLLECTOR_H
