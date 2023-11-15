TODO：这块代码还没写

### 1. 内存泄漏监控

#### 1.1 监控Java内存泄漏
看 `LeakCanary`源码，将待销毁对象用`WeakReference`包一下，如果对象正常被回收，会放到`ReferenceQueue`队列中；如果gc后不在该队列中，说明存在内存泄漏

#### 1.2 监控native内存泄漏



### 2. OOM监控
间隔一段时间判断使用内存有没有到达一定阈值，比如到85%，然后dump文件

### 3. 怎么dump prof
1. 先 suspend 获取主进程中的线程拷贝
2. 再 fork 创建子进程，让子进程拥有父进程的拷贝
3. 在子进程中执行 Debug.dumpHprofData
4. 父进程 resume 虚拟机，再做 FileObserver 监听文件操作，异步等待

### 4. 怎么剪裁 prof
裁剪一般有两种方式：
* dump之后，对文件进行读取并裁剪的流派：比如Shark、微信的Matrix等
* dump时直接对数据进行实时裁剪，需要hook数据的写入过程：比如美团的Probe、快手的KOOM等
具体看这里：[剖析hprof文件的两种主要裁剪流派](https://blog.yorek.xyz/android/3rd-library/hprof-shrink/#3-matrix)


### 5. 怎么自动分析 prof
看 `LeakCanary` 源码



### 6. 怎么获取java和native占用内存

### 7. 怎么保证上传大文件的成功率
