//
// Created by cnting on 2023/11/10.
//

#ifndef APM_SMALL_BUFFER_DETECTOR_H
#define APM_SMALL_BUFFER_DETECTOR_H

#include "detector.h"

namespace iocanary {
    class FileIOSmallBufferDetector : public FileIODetector {
    public:
        virtual void Detect(const iocanary::IOInfo &file_io_info) override;

        constexpr static const IssueType
        kType = IssueType::kIssueSmallBuffer;

    };
}

#endif //APM_SMALL_BUFFER_DETECTOR_H
