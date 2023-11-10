//
// Created by cnting on 2023/11/9.
//

#ifndef APM_IO_CANARY_H
#define APM_IO_CANARY_H

#include <__mutex_base>
#include "io_info_collector.h"
#include <deque>
#include <vector>
#include "../detector/detector.h"

namespace iocanary {
    class IOCanary {
    public:
        //禁止拷贝构造
        IOCanary(const IOCanary &) = delete;

        IOCanary &operator=(IOCanary const &) = delete;

        //单例
        static IOCanary &Get();

        void OnOpen(const char *pathname, int flags, mode_t mode, int open_ret,
                    const JavaContext &java_context);

        void OnRead(int fd, const void *buf, size_t size, ssize_t read_ret, long read_cost);

        void OnWrite(int fd, const void *buf, size_t size, ssize_t write_ret, long write_cost);

        void OnClose(int fd, int close_ret);

    private:
        IOCanary();

        ~IOCanary();

        void Detect();

        int TakeFileIOInfo(std::shared_ptr<IOInfo> &file_io_info);

        void OfferFileIOInfo(std::shared_ptr<IOInfo> &file_io_info);

        std::vector<FileIODetector *> detectors_;
        IOInfoCollector collector_;
        std::deque<std::shared_ptr<IOInfo>> queue_;
        bool exit_;
        std::mutex queue_mutex_;
        std::condition_variable queue_cv_;
    };
}


#endif //APM_IO_CANARY_H
