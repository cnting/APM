package com.cnting.apm_battery.hooker;

import android.os.IBinder;
import android.os.IInterface;
import android.text.TextUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Created by cnting on 2023/11/15
 * hook 系统服务
 */
public class SystemServiceBinderHooker {


    private String serviceName;
    private String serviceClassName;
    private HookCallback hookCallback;
    private Object originBinder;

    public SystemServiceBinderHooker(String serviceName, String serviceClassName, HookCallback hookCallback) {
        this.serviceName = serviceName;
        this.serviceClassName = serviceClassName;
        this.hookCallback = hookCallback;
    }

    public boolean hook() {
        try {
            //1.获取 IBinder 对象 hook 住后塞到 sCache中
            //(1) 调用 ServiceManager.getService()方法，获取IBinder对象
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            Method getServiceMethod = serviceManager.getDeclaredMethod("getService", String.class);
            getServiceMethod.setAccessible(true);
            originBinder = getServiceMethod.invoke(null, serviceName);

            //(2) hook这个IBinder对象，得到代理对象
            Object proxyBinder = Proxy.newProxyInstance(originBinder.getClass().getClassLoader(), new Class[]{IBinder.class}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (TextUtils.equals(method.getName(), "queryLocalInterface")) {
                        return createServiceProxy(originBinder);
                    }
                    return method.invoke(originBinder, args);
                }
            });

            //(3) 将代理对象塞回 sCache
            Field sCacheField = serviceManager.getDeclaredField("sCache");
            sCacheField.setAccessible(true);
            Map<String, Object> sCache = (Map<String, Object>) sCacheField.get(null);
            sCache.put(serviceName, proxyBinder);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //hook queryLocalInterface，生成一个IWifiManager.Stub.Proxy(obj)，然后hook这个对象
    private Object createServiceProxy(Object originBinder) {
        try {
            //1.创建一个IWifiManager.Stub.Proxy(obj)
            Class<?> proxyClass = Class.forName(serviceClassName + "$Stub$Proxy");
            Constructor<?> proxyConstructor = proxyClass.getDeclaredConstructor(IBinder.class);
            proxyConstructor.setAccessible(true);
            Object originProxy = proxyConstructor.newInstance(originBinder);

            //2.hook这个对象
            return Proxy.newProxyInstance(originProxy.getClass().getClassLoader(), new Class[]{
                    IBinder.class, IInterface.class, Class.forName(serviceClassName)
            }, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (hookCallback != null) {
                        hookCallback.onServiceMethodInvoke(method, args);
                        Object result = hookCallback.onServiceMethodIntercept(originProxy, method, args);
                        if (result != null) {
                            return result;
                        }
                    }
                    return method.invoke(originProxy, args);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean unhook() {
        if (originBinder == null) return false;

        try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            Field sCacheField = serviceManager.getDeclaredField("sCache");
            sCacheField.setAccessible(true);
            Map<String, Object> sCache = (Map<String, Object>) sCacheField.get(null);
            sCache.put(serviceName, originBinder);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public interface HookCallback {
        void onServiceMethodInvoke(Method method, Object[] args);

        Object onServiceMethodIntercept(Object receiver, Method method, Object[] args);
    }
}
