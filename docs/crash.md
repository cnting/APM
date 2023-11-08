### Java Crash处理
思路：调用 `Thread#setDefaultUncaughtExceptionHandler` 设置 exceptionHandler，移除首页以上的所有Activity
具体实现看 `JavaCrashMonitor.kt`


### Native Crash处理
* [Android 平台 Native 代码的崩溃捕获机制及实现](https://mp.weixin.qq.com/s/g-WzYF3wWAljok1XjPoo7w?)
* [为什么说获取堆栈从来就不是一件简单的事情](https://juejin.cn/post/7118609781832548383)

思路：
1. 自定义信号处理器
2. 开子线程循环获取异常信息

具体实现看 `NativeCrashMonitor.kt`

TODO：
* [看xCrash源码](https://juejin.cn/post/6898938662214434830?searchId=2023103023025873DB4B114105053E0D5E)