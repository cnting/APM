//
// Created by cnting on 2023/11/10.
//

#include "FileUtil.h"
#include <sys/stat.h>

namespace apm {
    int GetFileSize(const char *file_path) {
        struct stat stat_buf;
        if (-1 == stat(file_path, &stat_buf)) {
            return -1;
        }
        return stat_buf.st_size;
    }
}