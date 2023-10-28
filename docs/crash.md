### 如何让APP永不崩溃？
思路：调用 `Thread#setDefaultUncaughtExceptionHandler` 设置 exceptionHandler，移除首页以上的所有Activity