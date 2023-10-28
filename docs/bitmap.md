### 0. 预备知识
* [深入理解 图片内存优化的常见方案和 AndroidBitmapMonitor 的原理](https://juejin.cn/post/7214800241245880379)
* [Android 源码 —— Bitmap 位图内存的演进流程](https://sharrychoo.github.io/blog/android-source/bitmap-memory-evolution)

### 1. 大图告警

#### 1.1 如何拦截bitmap的创建?
![拦截bitmap的创建](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/68b58745d9ef41388e8583fbb535d0ea~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)


#### 1.2 告警时机：
1. bitmap占用内存过大，比如超过100M
2. bitmap尺寸宽高超过ImageView宽高的两倍

### 2. 重复图片加载

### 3. 图片泄漏检测