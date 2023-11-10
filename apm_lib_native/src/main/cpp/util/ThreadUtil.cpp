//
// Created by cnting on 2023/11/8.
//

#include <cstring>
#include <unistd.h>
#include <sys/stat.h>
#include "ThreadUtil.h"

#define PROCESS_NAME_LENGTH 512
#define THREAD_NAME_LENGTH 512
#define THREAD_RUN_INFO_LENGTH 512
namespace apm {
    const char *getProcessName(pid_t pid) {
        if (pid <= 1) {
            return NULL;
        }
        char *path = (char *) calloc(1, PATH_MAX);
        char *line = (char *) calloc(1, PROCESS_NAME_LENGTH);
        snprintf(path, PATH_MAX, "proc/%d/cmdline", pid);
        FILE *cmdFile = NULL;
        if ((cmdFile = fopen(path, "r"))) {
            fgets(line, PROCESS_NAME_LENGTH, cmdFile);
            fclose(cmdFile);
        }
        if (line) {
            int length = strlen(line);
            if (line[length - 1] == '\n') {
                line[length - 1] = '\0';
            }
        }
        free(path);
        return line;
    }

    const char *getThreadName(pid_t tid) {
        if (tid <= 1) {
            return NULL;
        }
        char *path = (char *) calloc(1, PATH_MAX);
        char *line = (char *) calloc(1, THREAD_NAME_LENGTH);
        snprintf(path, THREAD_NAME_LENGTH, "proc/%d/comm", tid);
        FILE *commFile = NULL;
        if ((commFile = fopen(path, "r"))) {
            fgets(line, THREAD_NAME_LENGTH, commFile);
            fclose(commFile);
        }
        if (line) {
            int length = strlen(line);
            if (line[length - 1] == '\n') {
                line[length - 1] = '\0';
            }
        }
        free(path);
        return line;
    }

/**
 * 获取线程的CPU信息
 */
    const char *getThreadRunInfo(pid_t pid, pid_t tid) {
        char *path = static_cast<char *>(calloc(1, PATH_MAX));
        char *thread_info = static_cast<char *>(calloc(1, THREAD_RUN_INFO_LENGTH));
        snprintf(path, PATH_MAX, "/proc/%d/task/%d/stat", pid, tid);
        FILE *commFile = NULL;
        if ((commFile = fopen(path, "r"))) {
            fgets(thread_info, THREAD_RUN_INFO_LENGTH, commFile);
            fclose(commFile);
        }
        if (thread_info) {
            int length = strlen(thread_info);
            if (thread_info[length - 1] == '\n') {
                thread_info[length - 1] = '\0';
            }
        }
        free(path);
        return thread_info;
    }

    bool isMainThread() {
        return getMainThreadId() == getCurrentThreadId();
    }

    int getMainThreadId() {
        static intmax_t pid = getpid();
        return pid;
    }

    int getCurrentThreadId() {
        return gettid();
    }

}