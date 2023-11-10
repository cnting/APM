//
// Created by cnting on 2023/11/8.
//

#ifndef APM_MYLOG_H
#define APM_MYLOG_H

#include "android/log.h"
#define TAG "APM_Native"

# define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
# define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
# define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

#endif //APM_MYLOG_H
