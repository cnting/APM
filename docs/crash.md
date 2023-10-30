### 如何让APP永不崩溃？
思路：调用 `Thread#setDefaultUncaughtExceptionHandler` 设置 exceptionHandler，移除首页以上的所有Activity

### Native Crash处理
* [Android 平台 Native 代码的崩溃捕获机制及实现](https://mp.weixin.qq.com/s/g-WzYF3wWAljok1XjPoo7w?)