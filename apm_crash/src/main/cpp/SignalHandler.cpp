//
// Created by cnting on 2023/10/29.
//

#include <string.h>
#include <unistd.h>
#include <algorithm>
#include "SignalHandler.h"
#include "CrashAnalyser.h"

void signalPass(int code, siginfo_t *si, void *sc) {
    /**
     * 这里要考虑非信号方式防止死锁
     * SIG_DFL是默认的处理函数，将code和SIGALRM都设置为默认的处理函数，就不会再调用signalPass()了，保证这个方法只调用一次
     */
    signal(code, SIG_DFL);
    signal(SIGALRM, SIG_DFL);
    //设置8秒后收到SIGALRM信号
    (void) alarm(8);

    //解析栈信息
    notifyCaughtSignal(code, si, sc);

    //给系统原来默认的处理，否则就会进入死循环
    oldHandlers[code].sa_sigaction(code, si, sc);
}

bool installSignalHandlers() {
    //保存原来的处理
    for (int i = 0; i < exceptionSignalsNumber; i++) {
        //-1表示失败
        if (sigaction(exceptionSignals[i], NULL, &oldHandlers[exceptionSignals[i]]) == -1) {
            return false;
        }
    }
    //初始化赋值
    struct sigaction sa;
    //将sa指针的前size个位置都设为0
    memset(&sa, 0, sizeof(sa));
    //sa.sa_mask是一个信号集合，这个函数是初始化信号集合为空
    sigemptyset(&sa.sa_mask);
    //指定信号处理的回调函数
    sa.sa_sigaction = signalPass;
    //SA_ONSTACK：使用一个代替栈
    //sa_flags包含SA_SIGINFO时就会调用 sa_sigaction
    sa.sa_flags = SA_ONSTACK | SA_SIGINFO;

    for (int i = 0; i < exceptionSignalsNumber; i++) {
        //将 exceptionSignals[i] 信号加到 &sa.sa_mask信号集中
        sigaddset(&sa.sa_mask, exceptionSignals[i]);
    }

    //调用sigaction来处理信号回调
    for (int i = 0; i < exceptionSignalsNumber; i++) {
        if (sigaction(exceptionSignals[i], &sa, NULL) == -1) {
            //可以输出一个警告
        }
    }

    return true;
}

/**
 * 如果异常是由于栈溢出引起的，系统在同一个已经满了的栈上调用SIGSEGV的信号处理函数，会再一次引起同样的信号。
 * 这里设置额外的栈空间，保留一下在紧急情况下使用的空间。（系统会在危险情况下把栈指针指向这个地方，使得可以在一个新的栈上运行信号处理函数）
 */
void installAlternateStack() {
    stack_t newStack;
    stack_t oldStack;
    memset(&newStack, 0, sizeof(newStack));
    memset(&oldStack, 0, sizeof(oldStack));
    static const unsigned signalStackSize = std::max(16384, SIGSTKSZ);
    //要先把原来的拿出来，可能会有一些其他框架早已设置好了
    if (sigaltstack(NULL, &oldStack) == -1
        || !oldStack.ss_sp
        || oldStack.ss_size < signalStackSize) {
        newStack.ss_sp = calloc(1, signalStackSize);
        newStack.ss_size = signalStackSize;
        if (sigaltstack(&newStack, NULL) == -1) {
            free(newStack.ss_sp);
        }
    }

}