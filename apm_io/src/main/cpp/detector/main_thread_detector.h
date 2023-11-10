//
// Created by cnting on 2023/11/10.
//

#ifndef APM_MAIN_THREAD_DETECTOR_H
#define APM_MAIN_THREAD_DETECTOR_H

#include "detector.h"

namespace iocanary {
    class FileIOMainThreadDetector : public FileIODetector {
    public:
        virtual void Detect(const iocanary::IOInfo &file_io_info) override;

        constexpr static const IssueType kType = IssueType::kIssueMainThreadIO;
    };
}


#endif //APM_MAIN_THREAD_DETECTOR_H
