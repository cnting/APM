### 1. 卡顿 & ANR
卡顿产生的原因是 `doFrame` 的消息无法在 `vsync` 的时间间隔内完成执行，而 `ANR` 是因为关键的系统消息 或者 `Input` 事件无法在系统定义的超时阈值内完成执行。

### 2. 卡顿检测
#### 2.1 Looper printer

使用 `Looper printer`的方式监听每个消息执行耗时，具体实现看 `LooperMonitor`类。

> 这种方式会导致字符串频繁拼接，产生大量String临时对象，可能会加重GC频率。
>
> 在 Android api ≥ 28 时，`Looper` 中新增了一个 `Observer` 的接口，在消息被调度前后会回调其对应方法，并将消息对象作为参数传入了。但该类是一个 Hidden API。具体信息看[Android 卡顿与 ANR 的分析实践](https://juejin.cn/post/7136008620658917407#heading-3)



#### 2.2  监听一帧的执行耗时

通过反射 `Choreographer.mCallbackQueues`，在`input`、`animation`、`traversal`前添加 runnable，计算一帧耗时及这三个队列执行耗时，具体看 `UiThreadMonitor.kt`



#### 2.3 IdleHandler、TouchEvent、SyncBarrier检测

[Idle、TouchEvent、SyncBarrier 检测](https://mp.weixin.qq.com/s/3dubi2GVW_rVFZZztCpsKg)



#### 2.4 记录消息队列调度历史 [TODO]

* [shopee团队 LooperMonitor](https://juejin.cn/post/7136008620658917407#heading-8)
* [头条Raster](https://juejin.cn/post/6942665216781975582)
* [别人实现了头条方案](https://juejin.cn/post/7031834640034103304)



##### 2.4.1 [消息分类](https://juejin.cn/post/7136008620658917407#heading-6)

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/568d367ff3b54e3591049f4819286397~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.awebp)



##### 2.4.2 [消息聚合](https://juejin.cn/post/6942665216781975582#heading-5)

一次消息调度，它的耗时可以从两个维度进行统计，即 `Wall Duration`(Message执行耗时) 和 `Cpu Duration`(CPU分配执行的时间，是真正的执行时间)

聚合规则：

1. 累计耗时超过阈值(比如300ms)，将这些消息合并成一条记录
2. 单条消息耗时严重时单独记录
3. 四大组件消息单独记录
4. idle状态间隔较长的也要单独记录



##### 2.4.3 获取耗时消息的堆栈

方案：

1. 每个函数插桩，会影响包体积
2. 在每个消息开始执行时，触发子线程的超时监控，如果在超时之后本次消息还没执行结束，则抓取主线程堆栈，并继续对该消息设置下一次超时监控，直到该消息执行结束并取消本轮监控。但是因为大部分消息耗时都很少，如果每次都频繁设置和取消，将会带来性能影响
3. 以消息开始时间加上超时时长为目标超时时间，每次超时时间到了之后，检查当前时间是否大于或等于目标时间，如果满足，则说明目标时间没有更新，也就是说本次消息没结束，则抓取堆栈。如果每次超时之后，检查当前时间小于目标时间，则说明上次消息执行结束，新的消息开始执行并更新了目标超时时间，这时异步监控需对齐目标超时，再次设置超时监控，如此往复。



##### 2.4.4 获取pending消息

使用 `Looper#dump()`



### 3. ANR [TODO]
* [监听SIGQUIT信号](https://mp.weixin.qq.com/s/fWoXprt2TFL1tTapt7esYg)
* [死锁检测](https://mp.weixin.qq.com/s/8hN9A5EpeRrTl4oHS8JV2A)