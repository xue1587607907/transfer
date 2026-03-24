package com.guiji.apiautomationfinal.utils;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadLocalUtils {
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = ThreadLocal.withInitial(ConcurrentHashMap::new);

    public static void set(String key, Object value) {
        THREAD_LOCAL.get().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) THREAD_LOCAL.get().get(key);
    }

    public static void setMap(Map<String, Object> params) {
        THREAD_LOCAL.get().putAll(params);
    }

    public static void clear() {
        THREAD_LOCAL.remove();
    }
}