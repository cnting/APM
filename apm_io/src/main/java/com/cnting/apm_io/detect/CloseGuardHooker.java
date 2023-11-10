package com.cnting.apm_io.detect;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by cnting on 2023/11/10
 */
public class CloseGuardHooker {
    private boolean isTryHook = false;

    public void hook() {
        if (!isTryHook) {
            tryHook();
            isTryHook = true;
        }
    }

    private void tryHook() {
        try {
            Class<?> guardClass = Class.forName("dalvik.system.CloseGuard");
            Class<?> reporterClass = Class.forName("dalvik.system.CloseGuard$Reporter");

            Method setEnableMethod = guardClass.getDeclaredMethod("setEnabled", boolean.class);
            setEnableMethod.setAccessible(true);
            setEnableMethod.invoke(null, true);

            for(Constructor f:guardClass.getConstructors()){
                Log.e("===>","构造:"+f.getName());
            }

            Log.d("===>", "getDeclaredFields()"+guardClass.getDeclaredFields().length);
            for (Field f : guardClass.getDeclaredFields()) {
                Log.e("===>", "变量:" + f.getName());
            }
            Log.d("===>", "getFields()");
            for (Field f : guardClass.getFields()) {
                Log.e("===>", "变量:" + f.getName());
            }
            Log.d("===>", "getDeclaredMethods()");
            for (Method m : guardClass.getDeclaredMethods()) {
                Log.e("===>", "方法:" + m.getName());
            }
            Log.d("===>", "getMethods()");
            for (Method m : guardClass.getMethods()) {
                Log.e("===>", "方法:" + m.getName());
            }
            // TODO: 这里有问题，没有 getReporter()方法了，还没找到对应版本的源码
            Method getReporterMethod = guardClass.getDeclaredMethod("getReporter", null);
            getReporterMethod.setAccessible(true);
            Object originReporter = getReporterMethod.invoke(null);

            Method setReporterMethod = guardClass.getDeclaredMethod("setReporter", reporterClass);
            setReporterMethod.setAccessible(true);
            setReporterMethod.invoke(null, proxyReporter(originReporter, reporterClass));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object proxyReporter(Object originReporter, Class<?> reportClass) {
        return Proxy.newProxyInstance(originReporter.getClass().getClassLoader(), new Class[]{reportClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("report".equals(method.getName())) {
                    Throwable throwable = (Throwable) args[1];
                    throwable.printStackTrace();
                }
                return method.invoke(originReporter, args);
            }
        });
    }


}
