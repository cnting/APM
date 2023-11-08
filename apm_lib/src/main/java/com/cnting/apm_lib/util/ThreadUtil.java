package com.cnting.apm_lib.util;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Keep;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by cnting on 2023/11/7
 */
public class ThreadUtil {
    private static final String TAG = "ThreadUtil";

    public static String getStackInfoByThreadName(String threadName, ThreadGroup systemThreadGroup) {
        Thread thread = getThreadByName(threadName, systemThreadGroup);
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTraceElements = thread.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            sb.append(stackTraceElement.toString()).append("\r\n");
        }
        return sb.toString();
    }

    public static Thread getThreadByName(String threadName, ThreadGroup systemThreadGroup) {
        if (TextUtils.isEmpty(threadName)) {
            return null;
        }
        Thread theThread = null;
        if (threadName.equals("main")) {
            theThread = Looper.getMainLooper().getThread();
        } else {
            Thread[] threadArray = new Thread[]{};
            try {
                Set<Thread> threadSet = getAllStackTraces(systemThreadGroup).keySet();
                threadArray = threadSet.toArray(new Thread[threadSet.size()]);
            } catch (Exception e) {
                Log.e(TAG, "dump thread Traces", e);
            }

            for (Thread thread : threadArray) {
                if (thread.getName().equals(threadName)) {
                    theThread = thread;
                    Log.e(TAG, "find it." + threadName);
                }
            }
        }
        return theThread;
    }

    public static Set<Thread> getAllThreads() {
        return getAllStackTraces(null).keySet();
    }

    /**
     * 获取线程堆栈的map.
     *
     * @return 返回线程堆栈的map
     */
    public static Map<Thread, StackTraceElement[]> getAllStackTraces(ThreadGroup systemThreadGroup) {
        if (systemThreadGroup == null) {
            return Thread.getAllStackTraces();
        } else {
            Map<Thread, StackTraceElement[]> map = new HashMap<>();

            // Find out how many live threads we have. Allocate a bit more
            // space than needed, in case new ones are just being created.
            int count = systemThreadGroup.activeCount();
            Thread[] threads = new Thread[count + count / 2];

            // Enumerate the threads and collect the stacktraces.
            count = systemThreadGroup.enumerate(threads);
            for (int i = 0; i < count; i++) {
                try {
                    map.put(threads[i], threads[i].getStackTrace());
                } catch (Throwable e) {
                    Log.e(TAG, "fail threadName: " + threads[i].getName(), e);
                }
            }
            return map;
        }
    }

    public static ThreadGroup getSystemThreadGroup() {
        ThreadGroup systemThreadGroup = null;
        try {
            Class<?> threadGroupClass = Class.forName("java.lang.ThreadGroup");
            Field systemThreadGroupField = threadGroupClass.getDeclaredField("systemThreadGroup");
            systemThreadGroupField.setAccessible(true);
            systemThreadGroup = (ThreadGroup) systemThreadGroupField.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return systemThreadGroup;
    }
}
