//
// Created by cnting on 2023/11/10.
//

#ifndef APM_DETECTOR_H
#define APM_DETECTOR_H


#include <string>
#include "../core/io_info_collector.h"

namespace iocanary {
    typedef enum {
        kIssueMainThreadIO = 1,
        kIssueSmallBuffer,
        kIssueRepeatRead
    } IssueType;

    class Issue {
    public:
        Issue(IssueType type, IOInfo ioInfo) : type_(type), ioinfo_(ioInfo) {}

        const IssueType type_;
        const IOInfo ioinfo_;
    };

    class FileIODetector {
    public:
        virtual void Detect(const IOInfo &file_io_info) = 0;

    protected:
        void PublishIssue(const Issue &issue);

    };


}

#endif //APM_DETECTOR_H
