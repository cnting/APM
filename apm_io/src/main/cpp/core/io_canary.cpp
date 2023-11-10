//
// Created by cnting on 2023/11/9.
//

#include "io_canary.h"
#include "thread"
#include "../detector/main_thread_detector.h"
#include "../detector/small_buffer_detector.h"
#include "mylog.h"

namespace iocanary {
    IOCanary &IOCanary::Get() {
        static IOCanary kInstance;
        return kInstance;
    }

    IOCanary::IOCanary() {
        exit_ = false;

        detectors_.push_back(new FileIOMainThreadDetector());
        detectors_.push_back(new FileIOSmallBufferDetector());

        std::thread detect_thread(&IOCanary::Detect, this);
        detect_thread.detach();
    }

    IOCanary::~IOCanary() {

    }

    void IOCanary::OfferFileIOInfo(std::shared_ptr<IOInfo> &file_io_info) {
        std::unique_lock<std::mutex> lock(queue_mutex_);
        queue_.push_back(file_io_info);
        queue_cv_.notify_one();
        lock.unlock();
    }

    int IOCanary::TakeFileIOInfo(std::shared_ptr<IOInfo> &file_io_info) {
        std::unique_lock<std::mutex> lock(queue_mutex_);
        while (queue_.empty()) {
            queue_cv_.wait(lock);
            if (exit_) {
                return -1;
            }
        }
        file_io_info = queue_.front();
        queue_.pop_front();
        return 0;
    }

    void IOCanary::Detect() {
        //shared_ptr 智能指针：https://www.cnblogs.com/jiayayao/p/6128877.html
        std::shared_ptr<IOInfo> file_io_info;
        while (true) {
            int ret = TakeFileIOInfo(file_io_info);
            if (ret != 0) {
                break;
            }
            for (auto detector: detectors_) {
                detector->Detect(*file_io_info);
            }
        }
    }

    void IOCanary::OnOpen(const char *pathname, int flags, mode_t mode, int open_ret,
                          const iocanary::JavaContext &java_context) {
        collector_.OnOpen(pathname, flags, mode, open_ret, java_context);
    }

    void IOCanary::OnClose(int fd, int close_ret) {
        std::shared_ptr<IOInfo> info = collector_.OnClose(fd, close_ret);
        if (info == nullptr) {
            return;
        }
        //文件关闭时检测
        OfferFileIOInfo(info);
    }

    void IOCanary::OnWrite(int fd, const void *buf, size_t size, ssize_t write_ret,
                           long write_cost) {
        collector_.OnWrite(fd, buf, size, write_ret, write_cost);
    }

    void IOCanary::OnRead(int fd, const void *buf, size_t size, ssize_t read_ret, long read_cost) {
        collector_.OnRead(fd, buf, size, read_ret, read_cost);
    }
}