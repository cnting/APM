### IO监控维度
1. 主线程不允许读写IO，或者不允许读写大文件
2. 读写的缓存不能太小，PAGE_SIZE >= 4096
3. 资源要释放，File close

### 监控1、2两个维度
主要是监控这几个方法：
```c
int open(const char *pathname, int flags, mode_t mode);
ssize_t read(int fd, void *buf, size_t size);
ssize_t write(int fd, const void *buf, size_t size); write_cuk
int close(int fd);
```
![监控内容](resources/io1.png)

监控这几个方法要用到`xhook`，具体看 `apm_io.cpp`

### 监控第3个维度
原理是 `FileInputStream` 内部有个 `CloseGuard`对象，当 `FileInputStream` 没有正常关闭时，会调用 `guard.warnIfOpen()`
```java
//FileInputStream
private final CloseGuard guard = CloseGuard.get();

@Override
public void close() throws IOException {
  guard.close();
  ...
}

@Override protected void finalize() throws IOException {
    if (guard != null) {
         guard.warnIfOpen();
    }
}

//CloseGuard
public void warnIfOpen() {
  if (closerNameOrAllocationInfo != null) {
      if (closerNameOrAllocationInfo instanceof Throwable) {
          reporter.report(MESSAGE, (Throwable) closerNameOrAllocationInfo);
      } else if (stackAndTrackingEnabled) {
          reporter.report(MESSAGE + " Callsite: " + closerNameOrAllocationInfo);
      } else {
          System.logW("A resource failed to call "
                  + (String) closerNameOrAllocationInfo + ". ");
      }
  }
}
```
要做的就是 hook `CloseGuard.reporter`对象，这里要用到动态代理。具体看 `CloseGuardHooker.java`

